package org.voltagex.rebridge.serializers;

import com.google.gson.GsonBuilder;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.voltagex.rebridge.api.entities.Simple;

import static org.testng.Assert.assertEquals;

public class SimpleSerializerTest
{

    GsonBuilder builder = new GsonBuilder();

    @BeforeTest
    public void setUp()
    {
        builder.registerTypeAdapter(Simple.class, new SimpleResponseSerializer());
    }

    @Test
    public void testSerialize() throws Exception
    {
        Simple actualObject = new Simple("testkey", "testvalue");
        String expected = "{\"testkey\":\"testvalue\"}";
        String actual = builder.create().toJson(actualObject);
        assertEquals(actual, expected);
    }

    @Test
    public void testSerializeWithMissingValue() throws Exception
    {
        Simple actualObject = new Simple();
        actualObject.setKey("keyonly");
        String actual = builder.create().toJson(actualObject);
        String expected = "{\"keyonly\":\"\"}";

        assertEquals(actual, expected);
    }

    @Test
    public void testSerializeWithMissingKey() throws Exception
    {
        Simple actualObject = new Simple();
        actualObject.setValue("valueonly");
        String actual = builder.create().toJson(actualObject);
        String expected = "{\"\":\"valueonly\"}";

        assertEquals(actual, expected);
    }


}