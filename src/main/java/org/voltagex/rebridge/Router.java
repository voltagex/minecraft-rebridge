package org.voltagex.rebridge;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;
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

import java.io.IOException;
import java.lang.reflect.*;

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
        gsonBuilder.serializeNulls();
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
        boolean needsMinecraftProvider = true;

        Action actionToBeRouted;
        String httpMethod = session.getMethod().name();
        String uri = session.getUri();
        WebResourceRouter webRouter = new WebResourceRouter(session, "/web/");

        if (uri.equals("/") && httpMethod.equals("GET"))
        {
            return webRouter.sendIndex();
        }

        else if (uri.startsWith("/web/"))
        {
            return webRouter.responseFromResource(uri);
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

        //most of the time this is the controller name. Should it be renamed?
        String action = segments.get(0);
        segments.remove(0);

        String method;

        Class<?> selectedController;


        method = segments.get(0);
        segments.remove(0);
        selectedController = findControllerForRequest(action);
        if (selectedController == null)
        {
            //todo: string.Format
            return processBadRequest(session, "Controller for " + action + " not found"); //todo: return 404
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

            if (retVal instanceof NanoHTTPD.Response)
            {
                return (NanoHTTPD.Response)retVal;
            }

            if (retVal instanceof JsonResponse)
            {
                return ((JsonResponse)retVal).toResponse();
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
        String contentLength = session.getHeaders().get("content-length");
        try
        {
            Integer.parseInt(contentLength);
        }

        catch (NumberFormatException nfe)
        {
            return processBadRequest(session, "Content-Length missing or invalid");
        }

        //this is a hack, but it beats shipping a custom version of NanoHTTPD
        Map<String, String> bodyMap = new HashMap<String, String>();

        try
        {
            session.parseBody(bodyMap);
        }

        catch (NanoHTTPD.ResponseException|IOException e)
        {
            return processBadRequest(session, e);
        }

        try
        {
            Method methodToInvoke = actionToBeRouted.getMethod();
            Parameters parametersNeededForRequest = methodToInvoke.getAnnotation(Parameters.class);

            if (parametersNeededForRequest == null)
            {
                return processBadRequest(session,
                                         String.format("Attempting to post to %s on %s, which doesn't have a parameter annotation yet",
                                                       methodToInvoke.getName(), actionToBeRouted.getController().getName()));
            }

            Object request = gson.fromJson(bodyMap.get("postData"), new Object().getClass());
            LinkedTreeMap<?,?> requestAsTreeMap = null;
            if (request instanceof LinkedTreeMap)
            {
                requestAsTreeMap = (LinkedTreeMap)request;
            }

            ArrayList<Object> parametersForMethod = new ArrayList<Object>();
            //Class<?>[] parameterTypes = methodToInvoke.getParameterTypes();
            String[] parameterNames = parametersNeededForRequest.Names();

            for (int i = 0; i<parameterNames.length; i++)
            {
                if (requestAsTreeMap.containsKey(parameterNames[i]))
                {
                    //Class<?> parameterType = parameterTypes[i];
                    //Object parameterValue = parameterType.cast());
                    parametersForMethod.add(requestAsTreeMap.get(parameterNames[i]).toString());
                }
            }


            //todo: didn't we just do this logic before?
            Object controllerInstance = provider == null ? actionToBeRouted.getController().newInstance() : actionToBeRouted.getController().newInstance(provider);
            if (request != null)
            {
                methodToInvoke.invoke(controllerInstance, parametersForMethod.toArray());
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

    public static NanoHTTPD.Response DebugRoutes()
    {
        JsonObject root = new JsonObject();

        for (Class<?> controller : avaliableControllers)
        {
            root.add(controller.getSimpleName(), gson.toJsonTree(getAllMethodsForController(controller)));
        }

        return new JsonResponse(NanoHTTPD.Response.Status.OK, root).toResponse();
    }

    private static ArrayList<String> getAllMethodsForController(Class<?> controller)
    {
        Method[] methods = controller.getDeclaredMethods();
        ArrayList<String> httpMethods = new ArrayList<String>();
        for (Method method : methods)
        {
            String name = method.getName();
            //todo: work out how to present routes that require post
//          if (name.startsWith("post") || name.startsWith("get"))
            if (name.startsWith("get"))
            {
                name = name.replace("get","");
                httpMethods.add(name);
            }
        }
        return httpMethods;
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
