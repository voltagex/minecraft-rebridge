package org.voltagex.rebridge.api.entities;

import fi.iki.elonen.NanoHTTPD;

public class StatusResponse extends ServiceResponse
{
    public StatusResponse(NanoHTTPD.Response.IStatus status)
    {
        this.setStatus(status);
    }
}
