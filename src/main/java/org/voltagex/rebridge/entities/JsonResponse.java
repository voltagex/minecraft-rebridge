package org.voltagex.rebridge.entities;

import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;

import java.util.Objects;

/**
 * Created by Adam on 4/26/2015.
 */
public class JsonResponse
{
    private final Gson gson = new Gson();
    private NanoHTTPD.Response.IStatus status;
    private Object responseBody;

    public NanoHTTPD.Response.IStatus getStatus()
    {
        return status;
    }

    public void setStatus(NanoHTTPD.Response.IStatus value)
    {
        this.status = value;
    }

    public Object getResponseBody()
    {
        return responseBody;
    }

    public void setResponseBody(Object value)
    {
        this.responseBody = value;
    }

//Todo: fix to register custom serializers first
//
//    @Override
//    public String toString()
//    {
//        return gson.toJson(responseBody);
//    }
}
