package org.voltagex.rebridge.requests;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.voltagex.rebridge.BaseTest;
import org.voltagex.rebridge.TestServer;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class SimpleRequestsTest extends BaseTest
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
        String input = "GET /Player/Position\r\n";
        String ret = (input);

        String expected = "{\"position";
        assertTrue(ret.startsWith(expected));
    }

    @Test
    public void testBadRequest() throws Exception
    {
        String input = "GET /some/method/that/doesn't/exist\r\n";

        String ret = getTestResponse(testServer,input);
        assertEquals("{\"error\":\"Controller for some not found\"}", ret);
    }
}