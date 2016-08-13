package org.voltagex.rebridge;

import fi.iki.elonen.NanoHTTPD;

import org.voltagex.rebridge.api.entities.CustomStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

/**
 * Created by live on 13/08/2016.
 */
public class WebResourceRouter
{
    private String prefix;
    private String host;

    public WebResourceRouter(NanoHTTPD.IHTTPSession session, String prefix)
    {
        this.prefix = prefix;
        this.host = session.getHeaders().get("host");
        this.host = "http://" + this.host;
    }

    public NanoHTTPD.Response sendIndex()
    {
        return sendTemporaryRedirect("/web/index.html");
    }

    private NanoHTTPD.Response sendTemporaryRedirect(String redirectTo)
    {
        redirectTo = host + redirectTo;
        NanoHTTPD.Response response = newFixedLengthResponse(new CustomStatus(302, "Moved Temporarily"), "text/plain", "...");
        response.addHeader("Location",redirectTo);

        return response;
    }



    public NanoHTTPD.Response responseFromResource(String resourceName)
    {
        try
        {
            System.out.println(resourceName);
            resourceName = resourceName.split(prefix)[1];
            System.out.println(resourceName);
            ClassLoader loader = getClass().getClassLoader();
            URL resource = loader.getResource(resourceName);
            File resourceFile = new File(resource.getFile());


            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", new FileInputStream(resourceFile), resourceFile.length());
        }

        catch (FileNotFoundException|NullPointerException fileNotFound)
        {
            //todo: proper JSON error here
            return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", resourceName + " not found");
        }
    }
}
