package org.voltagex.rebridge.providers;

import org.voltagex.rebridgeapi.entities.ObjectResponse;
import org.voltagex.rebridgeapi.entities.Position;

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

    public ObjectResponse getInventory()
    {
        return null;
    }
}
