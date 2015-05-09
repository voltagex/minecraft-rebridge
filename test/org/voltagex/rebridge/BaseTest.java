package org.voltagex.rebridge;

import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BaseTest
{
    public String getTestResponse(TestServer server, String input) throws IOException, NanoHTTPD.ResponseException
    {
        Map<String, String> files = new HashMap<String, String>();
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        NanoHTTPD.IHTTPSession session = server.createSession(inputStream, outputStream);
        session.execute();

        return outputStream.toString("UTF-8");
    }



    public String CreatePOST(String path, String body)
    {
        String template = "POST %s HTTP/1.1\r\n";
        template += "Content-Length: %d\r\n";
        template += "%s";
        return String.format(template,path,body.length(),body);
    }
}
