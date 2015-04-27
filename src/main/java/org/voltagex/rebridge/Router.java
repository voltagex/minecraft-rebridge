package org.voltagex.rebridge;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.iki.elonen.NanoHTTPD;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.voltagex.rebridge.annotations.Controller;
import org.voltagex.rebridge.entities.PositionResponse;
import org.voltagex.rebridge.entities.ServiceResponse;
import org.voltagex.rebridge.entities.SimpleResponse;
import serializers.PositionResponseSerializer;
import serializers.SimpleResponseSerializer;

import java.lang.reflect.Method;
import java.util.HashSet;

public class Router
{
    private static GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson;

    private final static String MIMEType = "application/json";
    private static HashSet<Class<?>> avaliableControllers = new HashSet<Class<?>>();

    public Router()
    {
        gsonBuilder.registerTypeAdapter(SimpleResponse.class, new SimpleResponseSerializer());
        gsonBuilder.registerTypeAdapter(PositionResponse.class, new PositionResponseSerializer());
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
            return processGet(session);
        }
        else if (method.equals("POST"))
        {
            return processPost(session);
        }
        else
        {
            return processBadRequest(session);
        }
    }

    private NanoHTTPD.Response processGet(NanoHTTPD.IHTTPSession session)
    {
        String uri = session.getUri();
        String[] segments = uri.split("/");
        final String controller = segments[1];
        String action = segments[2];
        if (segments.equals(null))
        {
            //todo: redirect/bad request or something
            return processBadRequest(session);
        }

        if (controller.isEmpty())
        {
            return processBadRequest(session);
        }

        //todo: decide whether some kind of "Action" type consisting of the Controller and the Method would be better here
        Class<?> selectedController = findControllerForRequest(controller);
        if (selectedController.equals(null))
        {
            //todo: string.Format
            return processBadRequest(session, "Action " + action + " on " + controller + " not found"); //todo: return 404
        }

        Method selectedMethod = findMethodForRequest("get", selectedController, action);

        try
        {
            Object retVal = selectedMethod.invoke(selectedController.newInstance());
            String json = gson.toJson(retVal);

            return new NanoHTTPD.Response(((ServiceResponse) retVal).getStatus(), MIMEType, json);
        }

        catch (Exception e)
        {
            return processBadRequest(session, e);
        }
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


    private NanoHTTPD.Response processPost(NanoHTTPD.IHTTPSession session)
    {
        return null;
    }

    //todo: clean up bad request processing
    private NanoHTTPD.Response processBadRequest(NanoHTTPD.IHTTPSession session)
    {
        return new NanoHTTPD.Response
                (NanoHTTPD.Response.Status.BAD_REQUEST,
                        MIMEType, "Bad request for " + session.getUri());
    }

    private NanoHTTPD.Response processBadRequest(NanoHTTPD.IHTTPSession session, Exception exception)
    {
        String exceptionMessage = gson.toJson(exception.getStackTrace());

        return new NanoHTTPD.Response
                (NanoHTTPD.Response.Status.BAD_REQUEST,
                        MIMEType, exceptionMessage
                );
    }

    private NanoHTTPD.Response processBadRequest(NanoHTTPD.IHTTPSession session, String message)
    {
        return new NanoHTTPD.Response
                (NanoHTTPD.Response.Status.BAD_REQUEST,
                        MIMEType,
                        message);
    }
}
