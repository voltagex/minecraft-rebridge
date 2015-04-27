package serializers;

import com.google.gson.*;
import org.voltagex.rebridge.entities.PositionResponse;
import org.voltagex.rebridge.entities.SimpleResponse;

import java.lang.reflect.Type;

public class PositionResponseSerializer implements JsonSerializer<PositionResponse>
{
    /**
     * Serializes a Position response to
     * {position: {x: 1.0, y: 2.0, z: 3.0}}
     * @param src
     * @param typeOfSrc
     * @param context
     * @return
     */
    @Override
    public JsonElement serialize(PositionResponse src, Type typeOfSrc, JsonSerializationContext context)
    {

        JsonPrimitive x = new JsonPrimitive(src.getX());
        JsonPrimitive y = new JsonPrimitive(src.getY());
        JsonPrimitive z = new JsonPrimitive(src.getZ());

        JsonObject parent = new JsonObject();
        JsonObject position = new JsonObject();

        position.add("x", x);
        position.add("y", y);
        position.add("z", z);

        parent.add("position", position);
        return parent;
    }
}
