package org.voltagex.rebridge.providers;

import org.voltagex.rebridge.entities.InventoryItem;
import org.voltagex.rebridge.entities.ListResponse;
import org.voltagex.rebridge.entities.Position;

import java.util.List;

public interface IPlayerProvider
{
    Boolean playerIsOnServer();
    String getName();

    Position getPosition();
    void setPosition(Position position);

    ListResponse getInventory();
}
