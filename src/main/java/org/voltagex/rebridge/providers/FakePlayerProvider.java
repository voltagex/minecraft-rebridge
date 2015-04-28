package org.voltagex.rebridge.providers;

import org.voltagex.rebridge.entities.InventoryItem;
import org.voltagex.rebridge.entities.ListResponse;
import org.voltagex.rebridge.entities.Position;

import java.util.List;

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

    public ListResponse getInventory()
    {
        return null;
    }
}
