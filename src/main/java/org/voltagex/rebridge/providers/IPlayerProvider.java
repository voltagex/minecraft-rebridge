package org.voltagex.rebridge.providers;

import org.voltagex.rebridge.entities.ObjectResponse;
import org.voltagex.rebridge.entities.Position;

public interface IPlayerProvider
{
    Boolean playerIsOnServer();
    String getName();

    Position getPosition();
    void setPosition(Position position);

    ObjectResponse getInventory();
}
