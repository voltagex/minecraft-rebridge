package org.voltagex.rebridge.controllers;

import fi.iki.elonen.NanoHTTPD;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import org.voltagex.rebridge.annotations.Controller;
import org.voltagex.rebridge.entities.PositionResponse;
import org.voltagex.rebridge.entities.ServiceResponse;
import org.voltagex.rebridge.entities.SimpleResponse;
import org.voltagex.rebridge.entities.StatusResponse;

@Controller
public class Player
{
    private static net.minecraft.client.Minecraft minecraft = Minecraft.getMinecraft();
    public ServiceResponse getName()
    {
        SimpleResponse response = new SimpleResponse();
        if (minecraft.thePlayer != null)
        {
            response.setKeyValue("name",minecraft.thePlayer.getName());
        }

        else
        {
            response.setStatus(NanoHTTPD.Response.Status.NOT_FOUND);
            response.setKeyValue("error", "No player on server");
        }
        return response;
    }

    public ServiceResponse getPosition()
    {
        if (minecraft.thePlayer != null)
        {
            float x, y, z;
            //todo: possible to remove dependency on Minecraft type here?
            BlockPos pos = minecraft.thePlayer.getPosition();
            x = pos.getX();
            y = pos.getY();
            z = pos.getZ();

            PositionResponse response = new PositionResponse(x,y,z);

            return response;
        }

        else
        {
            SimpleResponse response = new SimpleResponse();
            response.setStatus(NanoHTTPD.Response.Status.NOT_FOUND);
            response.setKeyValue("error", "No player on server");
            return response;
        }
    }

    public ServiceResponse postPosition(PositionResponse position)
    {
        if (minecraft.thePlayer != null)
        {
            minecraft.thePlayer.setPosition(position.getX(),position.getY(),position.getZ());
            return new StatusResponse(NanoHTTPD.Response.Status.ACCEPTED);

        }

        else
        {
            SimpleResponse response = new SimpleResponse();
            response.setStatus(NanoHTTPD.Response.Status.NOT_FOUND);
            response.setKeyValue("error", "No player on server");
            return response;
        }
    }

    public ServiceResponse postTest(SimpleResponse test)
    {
        return new SimpleResponse("userInput", test.getValue());
    }
}
