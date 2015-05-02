package org.voltagex.rebridge.entities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.iki.elonen.NanoHTTPD;

public abstract class ServiceResponse
{
    protected NanoHTTPD.Response.IStatus status;

    /**
     * Base class for all responses from the web service
     */
    public ServiceResponse()
    {
        status = NanoHTTPD.Response.Status.OK;
    }

    public NanoHTTPD.Response.IStatus getStatus()
    {
        return status;
    }

    public void setStatus(NanoHTTPD.Response.IStatus value)
    {
        this.status = value;
    }

}
