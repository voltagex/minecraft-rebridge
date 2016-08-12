package org.voltagex.rebridge;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.iki.elonen.NanoHTTPD;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.voltagex.rebridge.api.annotations.Controller;
import org.voltagex.rebridge.api.annotations.Parameters;
import org.voltagex.rebridge.api.entities.*;
import org.voltagex.rebridge.providers.IMinecraftProvider;
import org.voltagex.rebridge.serializers.PositionResponseSerializer;
import org.voltagex.rebridge.serializers.SimpleResponseSerializer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class Router
{
    private static GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson;
    private final static String MIMEType = "application/json";
    private static HashSet<Class<?>> avaliableControllers = new HashSet<Class<?>>();
    private static ListMultimap<String, Class<?>> registeredControllers = ArrayListMultimap.create();

    private static IMinecraftProvider provider;

    private Router()
    {

    }

    public void addRoute(String modid, List<Class<?>> controllers)
    {
        System.out.println("Called addRoute from " + modid);
        for (Class<?> controller : controllers)
        {
            registeredControllers.put(modid, controller);
        }
    }

    public Router(IMinecraftProvider provider)
    {
        Router.provider = provider;
        gsonBuilder.registerTypeAdapter(Simple.class, new SimpleResponseSerializer());
        gsonBuilder.registerTypeAdapter(Position.class, new PositionResponseSerializer());
        gsonBuilder = provider.registerExtraTypeAdapters(gsonBuilder);
        gson = gsonBuilder.create();

        Reflections reflections;

        reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("org.voltagex.rebridge.controllers"))
                .setScanners(
                        new SubTypesScanner(true),
                        new TypeAnnotationsScanner(),
                        new FieldAnnotationsScanner(),
                        new MethodAnnotationsScanner(),
                        new MethodParameterScanner(),
                        new MethodParameterNamesScanner(),
                        new MemberUsageScanner()));
        avaliableControllers = (HashSet) reflections.getTypesAnnotatedWith(Controller.class);
    }

    public NanoHTTPD.Response route(NanoHTTPD.IHTTPSession session)
    {
        // /someMod/someAction/parameter/anotherParameter
        // /Player/Position

        boolean needsMinecraftProvider = true;
        Action actionToBeRouted;
        String httpMethod = session.getMethod().name();

        String uri = session.getUri();
        if (uri.equals("/") && httpMethod.equals("GET"))
        {
            return RouteToHTML(session);
        }
        List<String> segments = new LinkedList<String>(Arrays.asList(uri.split("/")));
        // 0: /
        // 1: Player
        // 2: Position

        if (segments.size() < 3)
        {
            return processBadRequest(session);
        }

        segments.remove(0);
        // 0: Player
        // 1: Position

        String action = segments.get(0);
        segments.remove(0);

        String method;

        Class<?> selectedController;
        List<Class<?>> modControllers;

        //if the first parameter is actually a mod namespace
        //todo: fix this so the actual controller name can be passed in too
        if (registeredControllers.containsKey(action))
        {
            String modName = action;
            String modController = segments.get(0);
            method = segments.get(1);
            segments.remove(0);
            segments.remove(0);

            modControllers = registeredControllers.get(modName);
            needsMinecraftProvider = false;
            selectedController = findControllerForRequest(modController, modControllers);

            if (selectedController == null)
            {
                return processBadRequest(session, "Controller for " + method + " in " + modName + " not found");
            }
        }

        else
        {
            method = segments.get(0);
            segments.remove(0);
            selectedController = findControllerForRequest(action);
            if (selectedController == null)
            {
                //todo: string.Format
                return processBadRequest(session, "Controller for " + action + " not found"); //todo: return 404
            }
        }

        try
        {
            Method selectedMethod = findMethodForRequest(httpMethod, selectedController, method);

            if (selectedMethod == null)
            {
                return processBadRequest(session, "Method " + method + " on " + selectedController + " not found"); //todo: return 404
            }

            //todo: why don't implicit constructors work here?
            Constructor<?> controllerConstructor = needsMinecraftProvider ? selectedController.getConstructor(IMinecraftProvider.class) : selectedController.getConstructor();
            actionToBeRouted = new Action(controllerConstructor, selectedMethod, segments);
        }

        catch (NoSuchMethodException e)
        {
            return processBadRequest(session, e);
        }

        //todo: these ternary operators are starting to give me the shits
        if (httpMethod.equals("GET"))
        {
            return processGet(session, needsMinecraftProvider ? provider : null, actionToBeRouted);
        }

        else if (httpMethod.equals("POST"))
        {
            return processPost(session, needsMinecraftProvider ? provider : null, actionToBeRouted);
        }

        else
        {
            return processBadRequest(session);
        }
    }

    private NanoHTTPD.Response RouteToHTML(NanoHTTPD.IHTTPSession session)
    {
        return responseFromResource("index.html");
    }

    private NanoHTTPD.Response responseFromResource(String resourceName)
    {
        ClassLoader loader = getClass().getClassLoader();
        URL resource = loader.getResource(resourceName);
        File resourceFile = new File(resource.getFile());

        try
        {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", new FileInputStream(resourceFile), resourceFile.length());
        }

        catch (FileNotFoundException fileNotFound)
        {
            //todo: proper JSON error here
            return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", resourceName + " not found");
        }
    }

    private NanoHTTPD.Response processGet(NanoHTTPD.IHTTPSession session, IMinecraftProvider provider, Action actionToBeRouted)
    {
        try
        {
            Object retVal;
            String[] callingParameters = getParametersForMethod(actionToBeRouted.getMethod(), actionToBeRouted.getParameters());

            //todo: didn't we just do this logic before?
            Object controllerInstance = provider == null ? actionToBeRouted.getController().newInstance() : actionToBeRouted.getController().newInstance(provider);
            if (callingParameters.length > 0)
            {
                retVal = actionToBeRouted.getMethod().invoke(controllerInstance, callingParameters);
            }

            else
            {
                retVal = actionToBeRouted.getMethod().invoke(controllerInstance);
            }

            NanoHTTPD.Response.IStatus status = NanoHTTPD.Response.Status.OK;
            if (retVal instanceof ServiceResponse)
            {
                status = ((ServiceResponse) retVal).getStatus();
            }

            String json = gson.toJson(retVal);
            return newFixedLengthResponse(status, MIMEType, json) ;
        }

        catch (Exception e)
        {
            return processBadRequest(session, e);
        }
    }

    private NanoHTTPD.Response processPost(NanoHTTPD.IHTTPSession session, IMinecraftProvider provider, Action actionToBeRouted)
    {
        ServiceResponse body = null;

        try
        {
            int length = 0;
            String contentLength = session.getHeaders().get("content-length");
            try
            {
                length = Integer.parseInt(contentLength);
            }
            catch (NumberFormatException nfe)
            {
                return processBadRequest(session, "Content-Length missing or invalid");
            }

            String postBody;

            Type type = findTypeForRequest(actionToBeRouted.getMethod());

            Object request = null;
            if (length > 0)
            {
                //https://github.com/NanoHttpd/nanohttpd/issues/99
                //request = gson.fromJson(session.p(), type);
            }

            //todo: didn't we just do this logic before?
            Object controllerInstance = provider == null ? actionToBeRouted.getController().newInstance() : actionToBeRouted.getController().newInstance(provider);
            if (request != null)
            {
                actionToBeRouted.getMethod().invoke(controllerInstance, request);
            }

            else
            {
                actionToBeRouted.getMethod().invoke(controllerInstance);
            }
        }
        catch (Exception e)
        {
            return processBadRequest(session, e);
        }

        return newFixedLengthResponse(NanoHTTPD.Response.Status.ACCEPTED, MIMEType, "");
    }

    private Class<?> findControllerForRequest(final String controller)
    {
        return findControllerForRequest(controller, avaliableControllers);
    }

    private Class<?> findControllerForRequest(final String controller, final Iterable<Class<?>> controllers)
    {
        return Iterables.find(controllers, new Predicate<Class<?>>()
        {
            public boolean apply(Class<?> input)
            {
                return input.getName().contains(controller);
            }
        }, null);
    }

    private Method findMethodForRequest(String HttpMethod, Class<?> controller, String action)
    {
        action = HttpMethod.toLowerCase() + action;
        for (Method method : controller.getMethods())
        {
            if (method.getName().toLowerCase().equals(action.toLowerCase()))
            {
                return method;
            }
        }
        return null;
    }

    //todo: HACK: this is not good, it's imposing constraints on the variable order for actions
    private Type findTypeForRequest(Method selectedMethod)
    {
        return selectedMethod.getParameterTypes()[0];
    }


    private String[] getParametersForMethod(Method selectedMethod, List<String> urlSegments)
    {
        //this kinda sucks
        Parameters annotationParameters = selectedMethod.getAnnotation(Parameters.class);

        if (annotationParameters == null)
        {
            return new String[0];
        }

        String parameters[] = annotationParameters.Names();
        String[] callingParameters = new String[0];


        //split them off so they can be used as method arguments
        if (parameters.length == urlSegments.size() - 3)
        {
            callingParameters = new String[urlSegments.size() - 3];
            System.arraycopy(urlSegments, 3, callingParameters, 0, urlSegments.size() - 3);
        }

        return callingParameters;
    }

    //todo: clean up bad request processing
    private NanoHTTPD.Response processBadRequest(NanoHTTPD.IHTTPSession session)
    {
        return processBadRequest(session, "Bad request for " + session.getUri());
    }

    private NanoHTTPD.Response processBadRequest(NanoHTTPD.IHTTPSession session, Throwable exception)
    {
        JsonObject exceptionJson = new JsonObject();

        if (exception instanceof InvocationTargetException)
        {
            exception = ((InvocationTargetException) exception).getTargetException();
        }
        String message = exception.getMessage() == null ? "" : exception.getMessage();


        exceptionJson.add("message", new JsonPrimitive(exception.toString() + ": " + message));
        exceptionJson.add("stacktrace", gson.toJsonTree(exception.getStackTrace()));

        return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                                    MIMEType, exceptionJson.toString());
    }

    private NanoHTTPD.Response processBadRequest(NanoHTTPD.IHTTPSession session, String message)
    {
        JsonObject errorMessage = new JsonObject();
        errorMessage.add("error", new JsonPrimitive(message));

        return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                        MIMEType,
                        errorMessage.toString());
    }
}
