package org.voltagex.rebridge.serializers;

import com.google.gson.*;
import org.voltagex.rebridge.api.entities.Position;

import java.lang.reflect.Type;

public class PositionResponseSerializer implements JsonSerializer<Position>, JsonDeserializer<Position>
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
    public JsonElement serialize(Position src, Type typeOfSrc, JsonSerializationContext context)
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

    @Override
    public Position deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject wrapper = json.getAsJsonObject();
        if (!wrapper.has("position"))
        {
            throw new JsonParseException("Couldn't find 'position' element in JSON");
        }

        wrapper = wrapper.get("position").getAsJsonObject();

        Float x = wrapper.has("x") ? wrapper.get("x").getAsFloat() : null;
        Float y = wrapper.has("y") ? wrapper.get("y").getAsFloat() : null;
        Float z = wrapper.has("z") ? wrapper.get("z").getAsFloat() : null;

        return new Position(x,y,z);
    }




}
