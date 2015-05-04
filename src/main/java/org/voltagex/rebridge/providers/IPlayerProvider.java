package org.voltagex.rebridge.providers;

import org.voltagex.rebridgeapi.entities.ObjectResponse;
import org.voltagex.rebridgeapi.entities.Position;

public interface IPlayerProvider
{
    Boolean playerIsOnServer();
    String getName();

    Position getPosition();
    void setPosition(Position position);

    ObjectResponse getInventory();
}
