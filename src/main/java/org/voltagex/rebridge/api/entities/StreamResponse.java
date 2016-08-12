package org.voltagex.rebridge.api.entities;

import java.io.InputStream;

@Deprecated
public class StreamResponse extends ServiceResponse
{
    InputStream inputStream;
    String mimeType;

    public StreamResponse(InputStream inputStream, String MIMEType)
    {
        this.inputStream = inputStream;
        this.mimeType = MIMEType;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String getMimeType()
    {
        return mimeType;
    }
}
