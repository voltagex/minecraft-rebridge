package org.voltagex.rebridge.controllers;

import fi.iki.elonen.NanoHTTPD;
import org.voltagex.rebridge.api.annotations.Parameters;
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
        if (provider.getPlayer().playerIsOnServer())
        {
            response.setKeyValue("name", provider.getPlayer().getName());
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
        if (provider.getPlayer().playerIsOnServer())
        {
            return provider.getPlayer().getPosition();
        }

        else
        {
            return NoPlayerResponse();
        }
    }

    public ServiceResponse postPosition(Position position)
    {
        if (provider.getPlayer().playerIsOnServer())
        {
            provider.getPlayer().setPosition(position);
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

    public JsonResponse getInventory()
    {
        return provider.getPlayer().getInventory();
    }

    @Parameters(Names={"ItemName", "Amount"})
    public Boolean postItem(String ItemName, String Amount) throws NanoHTTPD.ResponseException
    {
        return provider.getPlayer().giveItem(ItemName,Integer.parseInt(Amount));
    }
}
