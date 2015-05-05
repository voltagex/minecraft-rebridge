package org.voltagex.rebridge.serializers;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.voltagex.rebridgeapi.entities.Position;

import static org.testng.Assert.assertEquals;

public class PositionSerializerTest
{

    GsonBuilder builder = new GsonBuilder();

    @BeforeTest
    public void setUp()
    {
        builder.registerTypeAdapter(Position.class, new PositionResponseSerializer());
    }

    @Test
    public void testSerialize() throws Exception
    {
        Position expected = new Position(1.0f, 2.0f, 3.0f);
        String inputJson = "{position: {x: 1.0, y: 2.0, z: 3.0}}";

        Position actual = builder.create().fromJson(inputJson, Position.class);
        assertEquals(actual.getX(), expected.getX());
        assertEquals(actual.getY(), expected.getY());
        assertEquals(actual.getZ(), expected.getZ());
    }

    @Test
    public void testSerializeWithMissingValues() throws Exception
    {
        Position expected = new Position();
        expected.setX(1.0f);
        expected.setZ(3.0f);
        String inputJson = "{position: {x: 1.0, z: 3.0}}";

        Position actual = builder.create().fromJson(inputJson, Position.class);
        assertEquals(actual.getX(), expected.getX());
        assertEquals(actual.getY(), expected.getY());
        assertEquals(actual.getZ(), expected.getZ());
    }

    @Test(expectedExceptions = JsonParseException.class)
    public void testSerializeWithInvalidJson() throws Exception
    {
        String inputJson = "{lol: rofl}";
        Position actual = builder.create().fromJson(inputJson, Position.class);
    }

    @Test
    public void testDeserialize() throws Exception
    {
        String expected = "{\"position\":{\"x\":1.0,\"y\":2.0,\"z\":3.0}}";
        Position actualObject = new Position(1.0f, 2.0f, 3.0f);

        String actual = builder.create().toJson(actualObject);
        assertEquals(actual, expected);
    }

    @Test
    public void testDeserializeWithMissingValues() throws Exception
    {
        Position actualObject = new Position();
        actualObject.setX(1.0f);
        actualObject.setZ(3.0f);

        String expected = "{\"position\":{\"x\":1.0,\"z\":3.0}}";

        String actual = builder.create().toJson(actualObject);
        assertEquals(actual, expected);

    }
}