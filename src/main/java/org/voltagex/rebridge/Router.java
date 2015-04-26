package org.voltagex.rebridge;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import fi.iki.elonen.NanoHTTPD;
import net.minidev.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.voltagex.rebridge.annotations.Controller;
import org.voltagex.rebridge.predicates.ReflectionFilters;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Router
{
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
        Set<Class<?>> types;
        Reflections reflections;
        String uri = session.getUri();
        String[] segments = uri.split("/");
        final String controller = segments[1];
        String method = segments[2];
        if (segments.equals(null))
        {
            //todo: redirect/bad request or something
            return processBadRequest(session);
        }


        if (controller.isEmpty())
        {
            return processBadRequest(session);
        }

        try
        {
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
            types = reflections.getTypesAnnotatedWith(Controller.class);
            Class<?>[] typeArray = types.toArray(new Class<?>[types.size()]);
            Iterables.removeIf(types, new Predicate<Class<?>>()
            {
                @Override
                public boolean apply(Class<?> input)
                {
                    return ReflectionFilters.nameNotContains(input, "rebridge.controllers." + controller.toLowerCase());
                }
            });
        }

        catch (ReflectionsException re)
        {
            return processBadRequest(session, re);
        }

        if (types.size() < 1)
        {
            return processBadRequest(session, "Controller " + controller + " not found");
        }

        else
        {
            Iterator<Class<?>> classIterator = types.iterator();
            Class<?> selectedClass = classIterator.next();
            Method selectedMethod = getMethodByName(selectedClass, method);

            try
            {
                Object responseObject = selectedMethod.invoke(selectedClass.newInstance());
                JSONObject responseJSON = new JSONObject();
                responseJSON.put("result", responseObject.toString());
                return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "application/json", responseJSON.toString());

            }

            catch (Exception e)
            {
              return processBadRequest(session, e);
            }
        }

        //return processBadRequest(session,"method not found");

    }

    private NanoHTTPD.Response processPost(NanoHTTPD.IHTTPSession session)
    {
        return null;
    }

    private NanoHTTPD.Response processBadRequest(NanoHTTPD.IHTTPSession session)
    {
        return new NanoHTTPD.Response
                (NanoHTTPD.Response.Status.BAD_REQUEST,
                        acceptHeaderToMIME(session), "Bad request for " + session.getUri());
    }

    private NanoHTTPD.Response processBadRequest(NanoHTTPD.IHTTPSession session, Exception exception)
    {
        return new NanoHTTPD.Response
                (NanoHTTPD.Response.Status.BAD_REQUEST,
                        acceptHeaderToMIME(session),
                        exception.getMessage());
    }

    private NanoHTTPD.Response processBadRequest(NanoHTTPD.IHTTPSession session, String message)
    {
        return new NanoHTTPD.Response
                (NanoHTTPD.Response.Status.BAD_REQUEST,
                        acceptHeaderToMIME(session),
                        message);
    }

    private String acceptHeaderToMIME(NanoHTTPD.IHTTPSession session)
    {
        Map<String, String> headers = session.getHeaders();

        if (headers.containsKey("accept"))
        {
            return headers.get("accept"); //todo: make this work
        }
        return "text/plain";
    }

    @Nullable
    private Method getMethodByName(Class<?> aClass, String name)
    {
        for (Method method : aClass.getMethods())
        {
            if (method.getName().toLowerCase().equals(name.toLowerCase()))
            {
                return method;
            }
        }

        return null;
    }
}
