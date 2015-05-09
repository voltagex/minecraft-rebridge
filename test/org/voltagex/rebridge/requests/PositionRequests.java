package org.voltagex.rebridge.requests;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.voltagex.rebridge.BaseTest;
import org.voltagex.rebridge.TestServer;

import static org.testng.AssertJUnit.assertEquals;

public class PositionRequests extends BaseTest
{
    protected TestServer testServer;
    @BeforeTest
    public void setUp()
    {
        testServer = new TestServer();
    }

    @Test
    public void testGoodRequest() throws Exception
    {
        String input = CreatePOST("/Player/Position","{position: {x: 100}}");
        String ret = getTestResponse(testServer, input);

        String expected = "HTTP/1.1 202 Accepted";
        String actual = ret.split("\r\n")[0].trim(); //grab the first line only - we can't control the Date value in the response
        assertEquals(expected,actual);
    }

    @Test
    public void testBadRequest() throws Exception
    {
        String input = CreatePOST("/Player/Position","{lol: rofl}");

        String ret = getTestResponse(testServer, input);
        String[] retArray = ret.split("\r\n");
        String actual = retArray[retArray.length-1].trim();
        //todo: should this really be returning the full ParseException here? Isn't there a return code for that?
        //assertEquals("{\"error\":\"Controller for some not found\"}", actual);
    }
}