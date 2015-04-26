package org.voltagex.rebridge.controllers;

import fi.iki.elonen.NanoHTTPD;
import net.minecraft.client.Minecraft;
import org.voltagex.rebridge.annotations.Controller;
import org.voltagex.rebridge.entities.JsonResponse;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adam on 4/26/2015.
 */

@Controller
public class Player
{
    private static net.minecraft.client.Minecraft minecraft = Minecraft.getMinecraft();
    public JsonResponse getName()
    {
        JsonResponse response = new JsonResponse();
        if (minecraft.thePlayer != null)
        {
            response.setStatus(NanoHTTPD.Response.Status.OK);
            response.setResponseBody(new AbstractMap.SimpleEntry<String, String>("name", minecraft.thePlayer.getName()));
        }

        else
        {
            response.setStatus(NanoHTTPD.Response.Status.NOT_FOUND);
            response.setResponseBody(new AbstractMap.SimpleEntry<String, String>("error", "No player on server"));
        }
        return response;
    }
}
