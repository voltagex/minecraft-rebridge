package org.voltagex.rebridge.requests;
import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.voltagex.rebridge.Rebridge;
import org.voltagex.rebridge.Router;
import org.voltagex.rebridge.providers.FakeMinecraftProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class SimpleRequestsTest
{
    protected TestServer testServer;
    protected Router testRouter;

    @BeforeTest
    public void setUp()
    {
        testServer = new TestServer();
        testRouter = new Router(new FakeMinecraftProvider());
    }

    public String getStringFromStream(InputStream stream)
    {
        try
        {
            return IOUtils.toString(stream, "UTF-8");
        }

        catch (Throwable ignored)
        {
            return "";
        }
    }

    public String getTestResponse(String input) throws IOException
    {
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        OutputStream outputStream = new ByteArrayOutputStream();
        NanoHTTPD.IHTTPSession session = testServer.createSession(inputStream, outputStream);
        session.execute();
        NanoHTTPD.Response re = testServer.serve(session);

       return getStringFromStream(re.getData());
    }

    public class TestServer extends Rebridge
    {

        public TestServer()
        {
            super();
        }

    public @Override Response serve(IHTTPSession session)
    {
        return testRouter.route(session);
    }

        public HTTPSession createSession(InputStream inputStream, OutputStream outputStream) {
            return new HTTPSession(new TestTempFileManager(), inputStream, outputStream);
        }
    }

    public static class TestTempFileManager extends NanoHTTPD.DefaultTempFileManager {
        public void _clear() {
            super.clear();
        }

        @Override
        public void clear() {
            // ignore
        }
    }

    @Test
    public void testGoodRequest() throws Exception
    {
        String input = "GET /Player/Position\r\n";
        String ret = getTestResponse(input);

        String expected = "{\"position";
        assertTrue(ret.startsWith(expected));
    }

    @Test
    public void testBadRequest() throws Exception
    {
        String input = "GET /some/method/that/doesn't/exist\r\n";

        String ret = getTestResponse(input);
        assertEquals(ret,"{\"error\":\"Action some not found\"}");
    }
}