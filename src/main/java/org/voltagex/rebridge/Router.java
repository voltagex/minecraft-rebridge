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
import org.voltagex.rebridge.annotations.Controller;
import org.voltagex.rebridge.annotations.Parameters;
import org.voltagex.rebridge.entities.Position;
import org.voltagex.rebridge.entities.ServiceResponse;
import org.voltagex.rebridge.entities.Simple;
import org.voltagex.rebridge.entities.StreamResponse;
import org.voltagex.rebridge.providers.IMinecraftProvider;
import org.voltagex.rebridge.serializers.PositionResponseSerializer;
import org.voltagex.rebridge.serializers.SimpleResponseSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;

public class Router
{
    private static GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson;

    private final static String MIMEType = "application/json";
    private static HashSet<Class<?>> avaliableControllers = new HashSet<Class<?>>();
    private static IMinecraftProvider provider;

    private Router()
    {

    }

    public Router(IMinecraftProvider provider)
    {
        Router.provider = provider;
        gsonBuilder.registerTypeAdapter(Simple.class, new SimpleResponseSerializer());
        gsonBuilder.registerTypeAdapter(Position.class, new PositionResponseSerializer());
        gsonBuilder = provider.registerExtraTypeAdapters(gsonBuilder);
        gson = gsonBuilder.create();

        HashSet<Class<?>> types;
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

        String method = session.getMethod().name();
        if (method.equals("GET"))
        {
            return processGet(session,provider);
        }
        else if (method.equals("POST"))
        {
            return processPost(session,provider);
        }
        else
        {
            return processBadRequest(session);
        }
    }

    private NanoHTTPD.Response processGet(NanoHTTPD.IHTTPSession session, IMinecraftProvider provider)
    {
        //todo: handle parameters

        String uri = session.getUri();
        String[] segments = uri.split("/");
        final String controller = segments[1];
        String action = segments[2];
        /*if (segments == null) //todo: redirect/bad request or something on request for "/"
        {
            return processBadRequest(session);
        }*/

        if (controller.isEmpty())
        {
            return processBadRequest(session);
        }

        //todo: decide whether some kind of "Action" type consisting of the Controller and the Method would be better here
        Class<?> selectedController = findControllerForRequest(controller);
        if (selectedController == null)
        {
            //todo: string.Format
            return processBadRequest(session, "Action " + action + " on " + controller + " not found"); //todo: return 404
        }

        Method selectedMethod = findMethodForRequest("get", selectedController, action);

        try
        {
            Constructor<?> controllerConstructor = selectedController.getConstructor(IMinecraftProvider.class);
            Object retVal;

            String[] callingParameters = getParametersForMethod(selectedMethod, segments);
            if (callingParameters.length > 0)
            {
                retVal = selectedMethod.invoke(controllerConstructor.newInstance(provider), callingParameters);
            }

            else
            {
                retVal = selectedMethod.invoke(controllerConstructor.newInstance(provider));
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

    private NanoHTTPD.Response processPost(NanoHTTPD.IHTTPSession session, IMinecraftProvider provider)
    {
        String uri = session.getUri();
        String[] segments = uri.split("/");
        final String controller = segments[1];
        String action = segments[2];
        ServiceResponse body = null;

        try
        {
            String contentLength = session.getHeaders().get("content-length");
            int length = Integer.parseInt(contentLength);
            String postBody;

            Class<?> selectedController = findControllerForRequest(controller);

            if (selectedController == null)
            {
                //todo: string.Format
                return processBadRequest(session, "Action " + action + " on " + controller + " not found"); //todo: return 404
            }

            Method selectedMethod = findMethodForRequest("post", selectedController, action);
            Type type = findTypeForRequest(selectedMethod);

            Object request;
            if (length > 0)
            {
                //https://github.com/NanoHttpd/nanohttpd/issues/99
                postBody = session.parsePost();
                request = gson.fromJson(postBody, type);
            }

            Constructor<?> controllerConstructor = selectedController.getConstructor(IMinecraftProvider.class);

            String[] callingParameters = getParametersForMethod(selectedMethod, segments);
            if (callingParameters.length > 0)
            {
                selectedMethod.invoke(controllerConstructor.newInstance(provider), callingParameters);
            }

            else
            {
                selectedMethod.invoke(controllerConstructor.newInstance(provider));
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


    private String[] getParametersForMethod(Method selectedMethod, String[] urlSegments)
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
        if (parameters.length == urlSegments.length - 3)
        {
            callingParameters = new String[urlSegments.length - 3];
            System.arraycopy(urlSegments, 3, callingParameters, 0, urlSegments.length - 3);
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
