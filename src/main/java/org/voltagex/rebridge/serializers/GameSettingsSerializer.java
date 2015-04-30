package org.voltagex.rebridge.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.client.settings.GameSettings;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class GameSettingsSerializer implements JsonSerializer<GameSettings>
{
   @Override
    public JsonElement serialize(GameSettings src, Type typeOfSrc, JsonSerializationContext context)
    {
        Field[] fields =  src.getClass().getFields();
        JsonObject object = new JsonObject();
        object.add("GameSettings", context.serialize(fields));
        return object;
    }
}
