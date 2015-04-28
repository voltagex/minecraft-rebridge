package org.voltagex.rebridge.controllers;

import fi.iki.elonen.NanoHTTPD;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import org.voltagex.rebridge.annotations.Controller;
import org.voltagex.rebridge.entities.*;
import org.voltagex.rebridge.providers.IMinecraftProvider;

import java.util.ArrayList;
import java.util.List;

@Controller
public class Player
{
    private static IMinecraftProvider provider;

    private Player()
    {

    }

    public Player(IMinecraftProvider provider)
    {
        this.provider = provider;
    }

    public ServiceResponse getName()
    {
        Simple response = new Simple();
        if (provider.player().playerIsOnServer())
        {
            response.setKeyValue("name", provider.player().getName());
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
        if (provider.player().playerIsOnServer())
        {
            return provider.player().getPosition();
        }

        else
        {
            return NoPlayerResponse();
        }
    }

    public ServiceResponse postPosition(Position position)
    {
        if (provider.player().playerIsOnServer())
        {
            provider.player().setPosition(position);
            return new StatusResponse(NanoHTTPD.Response.Status.ACCEPTED);
        }

        else
        {
            return NoPlayerResponse();
        }
    }

    private ServiceResponse NoPlayerResponse()
    {
        Simple response = new Simple();
        response.setStatus(NanoHTTPD.Response.Status.NOT_FOUND);
        response.setKeyValue("error", "No player on server");
        return response;
    }

    public ServiceResponse getInventory()
    {
        ListResponse response = provider.player().getInventory();
        if (response == null)
        {
            return NoPlayerResponse();
        }

        return response;
    }
}
