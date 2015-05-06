package org.voltagex.rebridge.api.entities;

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
