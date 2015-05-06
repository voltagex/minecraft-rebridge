package org.voltagex.rebridge.controllers;

import fi.iki.elonen.NanoHTTPD;
import org.voltagex.rebridge.providers.IMinecraftProvider;
import org.voltagex.rebridge.api.annotations.Controller;
import org.voltagex.rebridge.api.entities.*;

@Controller
public class Player
{
    private static IMinecraftProvider provider;

    private Player()
    {

    }

    public Player(IMinecraftProvider provider)
    {
        Player.provider = provider;
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
        ObjectResponse response = new ObjectResponse(provider.player().getInventory());
        if (response.getReturnedObject() == null)
        {
            return NoPlayerResponse();
        }

        return response;
    }
}
