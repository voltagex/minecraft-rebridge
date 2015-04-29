package org.voltagex.rebridge.serializers;

import com.google.gson.*;
import org.voltagex.rebridge.entities.InventoryItem;
import org.voltagex.rebridge.entities.Inventory;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class InventorySerializer implements JsonSerializer<Inventory>
{
   @Override
    public JsonElement serialize(Inventory src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject parent = new JsonObject();

        if (src.getList().size() == 0)
        {
            return parent;
        }

        parent.add(src.getClass().getSimpleName(), context.serialize(src.getList()));
        return parent;
    }
}
