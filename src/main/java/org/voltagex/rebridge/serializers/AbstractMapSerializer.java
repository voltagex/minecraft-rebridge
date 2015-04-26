package org.voltagex.rebridge.serializers;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.AbstractMap;

/**
 * Created by Adam on 4/26/2015.
 */
public class AbstractMapSerializer implements JsonSerializer<AbstractMap.SimpleEntry<String, String>>
{
    @Override
    public JsonElement serialize(AbstractMap.SimpleEntry<String, String> src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject resultObject = new JsonObject();
        resultObject.add(src.getKey(),new JsonPrimitive(src.getValue()));
        return resultObject;
    }
}
