package org.voltagex.rebridge.serializers;

import com.google.gson.*;
import net.minecraft.client.settings.GameSettings;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class GameSettingsSerializer implements JsonSerializer<GameSettings>
{
   @Override
    public JsonElement serialize(GameSettings src, Type typeOfSrc, JsonSerializationContext context)
    {
        Field[] fields = src.getClass().getFields();
        JsonObject parent = new JsonObject();

        for (Field field : fields)
        {
            JsonObject valueAndType = new JsonObject();
            try
            {
                valueAndType.add(field.get(src).toString(), new JsonPrimitive(field.getType().getName()));
                parent.add(field.getName(), valueAndType);
            }
            catch (IllegalAccessException ignored)
            {
            }
        }

        return parent;
    }
}
