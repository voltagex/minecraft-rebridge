package org.voltagex.rebridge.providers;

import fi.iki.elonen.NanoHTTPD;
import org.voltagex.rebridge.api.entities.JsonResponse;
import org.voltagex.rebridge.api.entities.Position;

public interface IPlayerProvider
{
    Boolean playerIsOnServer();
    String getName();

    Position getPosition();
    void setPosition(Position position);

    JsonResponse getInventory();

    Boolean giveItem(String ItemName, int Amount) throws NanoHTTPD.ResponseException;
}
