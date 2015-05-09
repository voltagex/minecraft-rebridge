package org.voltagex.rebridge;


import fi.iki.elonen.NanoHTTPD;
import org.voltagex.rebridge.providers.FakeMinecraftProvider;

import java.io.InputStream;
import java.io.OutputStream;

public class TestServer extends Rebridge
{

    protected Router testRouter;

    public TestServer()
    {
        super();
        testRouter = new Router(new FakeMinecraftProvider());
    }

    public
    @Override
    Response serve(IHTTPSession session)
    {
        return testRouter.route(session);
    }

    public HTTPSession createSession(InputStream inputStream, OutputStream outputStream)
    {
        return new HTTPSession(new TestTempFileManager(), inputStream, outputStream);
    }


    public static class TestTempFileManager extends NanoHTTPD.DefaultTempFileManager
    {
        public void _clear()
        {
            super.clear();
        }

        @Override
        public void clear()
        {
            // ignore
        }
    }
}