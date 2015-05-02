package org.voltagex.rebridge.entities;

import java.io.InputStream;

public class StreamResponse extends ServiceResponse
{
    InputStream inputStream;
    public StreamResponse(InputStream inputStream)
    {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }
}
