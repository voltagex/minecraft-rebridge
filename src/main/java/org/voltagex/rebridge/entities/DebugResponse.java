package org.voltagex.rebridge.entities;

import fi.iki.elonen.NanoHTTPD;

public class DebugResponse extends ServiceResponse
{
    private Object debuggedThing;
    public DebugResponse(Object thing)
    {
        debuggedThing = thing;
    }
}
