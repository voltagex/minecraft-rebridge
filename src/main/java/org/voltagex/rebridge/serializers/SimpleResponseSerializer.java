package org.voltagex.rebridge.serializers;

import com.google.gson.*;
import org.voltagex.rebridge.api.entities.Simple;

import java.lang.reflect.Type;

public class SimpleResponseSerializer implements JsonSerializer<Simple>
{
    /**
     * Serializes a Simple object to
     * {key: val}
     * @param src
     * @param typeOfSrc
     * @param context
     * @return
     */
    @Override
    public JsonElement serialize(Simple src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject keyValue = new JsonObject();
        JsonPrimitive valueAsJson = new JsonPrimitive(src.getValue() == null ? "" : src.getValue());
        keyValue.add(src.getKey() == null ? "" : src.getKey(), valueAsJson);
        return keyValue;
    }
}
