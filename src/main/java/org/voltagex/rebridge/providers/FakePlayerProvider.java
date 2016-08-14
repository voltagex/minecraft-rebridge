package org.voltagex.rebridge.providers;

import org.voltagex.rebridge.api.entities.JsonResponse;
import org.voltagex.rebridge.api.entities.Position;

public class FakePlayerProvider implements IPlayerProvider
{
    public Boolean playerIsOnServer()
    {
        return true;
    }

    public Position getPosition()
    {
        return new Position(0f,0f,0f);
    }

    public String getName()
    {
        return "Fake Player";
    }

    public void setPosition(Position position)
    {
        return;
    }

    public JsonResponse getInventory()
    {
        return null;
    }

    @Override
    public Boolean giveItem(String ItemName, int Amount)
    {
        return null;
    }
}
