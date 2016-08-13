package org.voltagex.rebridge.api.entities;

import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class JsonResponse
{
    private String mimeType;
    private String jsonString;
    private long totalBytes;
    private NanoHTTPD.Response.IStatus _status;
    public JsonResponse(NanoHTTPD.Response.IStatus status, JsonObject responseObject)
    {
        mimeType = "text/json";

        jsonString = responseObject.toString();
        totalBytes = jsonString.length();
        _status = status;
    }

    public NanoHTTPD.Response toResponse()
    {
        InputStream jsonStream = new ByteArrayInputStream(jsonString.getBytes());
        return newFixedLengthResponse(_status, mimeType,jsonStream,totalBytes);
    }
}
