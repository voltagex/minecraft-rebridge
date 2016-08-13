package org.voltagex.rebridge.api.entities;

import fi.iki.elonen.NanoHTTPD;

public class CustomStatus implements NanoHTTPD.Response.IStatus
{
    int status;
    String description;

    public CustomStatus(int Status, String Description)
    {
        status = Status;
        description = Description;
    }

    @Override
    public String getDescription()
    {
        return status + " " + description;
    }

    @Override
    public int getRequestStatus()
    {
        return status;
    }
}
