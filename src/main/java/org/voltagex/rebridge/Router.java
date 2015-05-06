package org.voltagex.rebridge;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.iki.elonen.NanoHTTPD;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.voltagex.rebridge.providers.IMinecraftProvider;
import org.voltagex.rebridge.api.entities.*;
import org.voltagex.rebridge.serializers.PositionResponseSerializer;
import org.voltagex.rebridge.serializers.SimpleResponseSerializer;
import org.voltagex.rebridge.api.annotations.Controller;
import org.voltagex.rebridge.api.annotations.Parameters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class Router
{
    private static GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson;
    private final static String MIMEType = "application/json";
    private static HashSet<Class<?>> avaliableControllers = new HashSet<Class<?>>();
    private static HashMap<String,Class<?>> registeredControllers = new HashMap<String, Class<?>>();
    private static IMinecraftProvider provider;

    private Router()
    {

    }


    public static void AddRoute(String namespace, Class<?> controllerClass)
    {
        System.out.println("Called addRoute from " + namespace);
        registeredControllers.put(namespace, controllerClass);
    }

    public Router(IMinecraftProvider provider)
    {
        Router.provider = provider;
        gsonBuilder.registerTypeAdapter(Simple.class, new SimpleResponseSerializer());
        gsonBuilder.registerTypeAdapter(Position.class, new PositionResponseSerializer());
        gsonBuilder = provider.registerExtraTypeAdapters(gsonBuilder);
        gson = gsonBuilder.create();

        Reflections reflections;

        //todo: do this once per run, not per call
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
        List<String> segments = new LinkedList<String>(Arrays.asList(uri.split("/")));
        // 0: /
        // 1: Player
        // 2: Position


        //todo: do we really need 3 parts of the path?
        if (segments.size() < 3)
        {
            return processBadRequest(session);
        }

        segments.remove(0);
        // 0: Player
        // 1: Position

        String action = segments.get(0);
        segments.remove(0);

        String method = segments.get(0);
        segments.remove(0);

        Class<?> selectedController;

        //if the first parameter is actually a mod namespace
        if (registeredControllers.containsKey(action))
        {
            selectedController = registeredControllers.get(action);
            needsMinecraftProvider = false;
        }

        else
        {
            selectedController = findControllerForRequest(action);
            if (selectedController == null)
            {
                //todo: string.Format
                return processBadRequest(session, "Action " + action + " not found"); //todo: return 404
            }
        }

        try
        {
            Method selectedMethod = findMethodForRequest(httpMethod, selectedController, method);
            Constructor<?> controllerConstructor = needsMinecraftProvider ? selectedController.getConstructor(IMinecraftProvider.class) : selectedController.getConstructor();
            actionToBeRouted = new Action(controllerConstructor, selectedMethod, segments);
        }

        catch (NoSuchMethodException e)
        {
            return processBadRequest(session, e);
        }

        if (httpMethod.equals("GET"))
        {
            return processGet(session, provider, actionToBeRouted);
        }

        else if (httpMethod.equals("POST"))
        {
            return processPost(session, provider, actionToBeRouted);
        }

        else
        {
            return processBadRequest(session);
        }
    }

    private NanoHTTPD.Response processGet(NanoHTTPD.IHTTPSession session, IMinecraftProvider provider, Action actionToBeRouted)
    {
        //todo: handle parameters
        //todo: handle routes from mods
        //todo: move to Guava MultiMap to allow multiple controllers from the same namespace

        try
        {
            Object retVal;
            String[] callingParameters = getParametersForMethod(actionToBeRouted.getMethod(), actionToBeRouted.getParameters());

            if (callingParameters.length > 0)
            {
                retVal = actionToBeRouted.getMethod().invoke(actionToBeRouted.getController().newInstance(provider), callingParameters);
            }

            else
            {
                retVal = actionToBeRouted.getMethod().invoke(actionToBeRouted.getController().newInstance(provider));
            }

            if (retVal instanceof StreamResponse)
            {
                StreamResponse streamResponse = (StreamResponse) retVal;
                return new NanoHTTPD.Response(streamResponse.getStatus(), streamResponse.getMimeType(), ((StreamResponse) retVal).getInputStream());
            }

            String json = gson.toJson(retVal);
            return new NanoHTTPD.Response(((ServiceResponse) retVal).getStatus(), MIMEType, json);
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
            String contentLength = session.getHeaders().get("content-length");
            int length = Integer.parseInt(contentLength);
            String postBody;

            Type type = findTypeForRequest(actionToBeRouted.getMethod());

            Object request;
            if (length > 0)
            {
                //https://github.com/NanoHttpd/nanohttpd/issues/99
                postBody = session.parsePost();
                request = gson.fromJson(postBody, type);
            }

            String[] callingParameters = getParametersForMethod(actionToBeRouted.getMethod(), actionToBeRouted.getParameters());
            if (callingParameters.length > 0)
            {
                actionToBeRouted.getMethod().invoke(actionToBeRouted.getController().newInstance(provider), callingParameters);
            }

            else
            {
                actionToBeRouted.getMethod().invoke(actionToBeRouted.getController().newInstance(provider));
            }
        }
                catch (Exception e)
                {
                    return processBadRequest(session,e);
                }

            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.ACCEPTED, MIMEType,"");
        }

    private Class<?> findControllerForRequest(final String controller)
    {
        return Iterables.find(avaliableControllers, new Predicate<Class<?>>()
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


        exceptionJson.add("message",new JsonPrimitive(exception.toString() + ": " + message));
        exceptionJson.add("stacktrace", gson.toJsonTree(exception.getStackTrace()));

        return new NanoHTTPD.Response
                (NanoHTTPD.Response.Status.BAD_REQUEST,
                        MIMEType, exceptionJson.toString()
                );
    }

    private NanoHTTPD.Response processBadRequest(NanoHTTPD.IHTTPSession session, String message)
    {
        JsonObject errorMessage = new JsonObject();
        errorMessage.add("error", new JsonPrimitive(message));

        return new NanoHTTPD.Response
                (NanoHTTPD.Response.Status.BAD_REQUEST,
                        MIMEType,
                        errorMessage.toString());
    }
}
