package org.voltagex.rebridge.serializers;

import com.google.gson.*;
import org.voltagex.rebridge.entities.ListResponse;
import org.voltagex.rebridge.entities.Simple;

import java.lang.reflect.Type;
import java.util.List;

public class ListResponseSerializer implements JsonSerializer<ListResponse>
{
    @Override
    public JsonElement serialize(ListResponse src, Type typeOfSrc, JsonSerializationContext context)
    {
        List<?> containedList = src.getList();
        JsonObject parent = new JsonObject();
        //todo: this won't work if the list is empty
        parent.add(containedList.get(0).getClass().getCanonicalName(), context.serialize(src.getList()));
        return parent;
    }
}
