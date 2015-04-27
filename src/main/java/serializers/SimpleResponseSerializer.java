package serializers;

import com.google.gson.*;
import org.voltagex.rebridge.entities.SimpleResponse;

import java.lang.reflect.Type;

public class SimpleResponseSerializer implements JsonSerializer<SimpleResponse>
{
    /**
     * Serializes a SimpleResponse to
     * {key: val}
     * @param src
     * @param typeOfSrc
     * @param context
     * @return
     */
    @Override
    public JsonElement serialize(SimpleResponse src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject keyValue = new JsonObject();
        JsonPrimitive valueAsJson = new JsonPrimitive(src.getValue());
        keyValue.add(src.getKey(), valueAsJson);
        return keyValue;
    }
}
