package org.voltagex.rebridge.api.entities;

import net.minecraft.client.settings.GameSettings;

public class GameSettingsResponse extends ServiceResponse
{
    GameSettings Settings;

    public GameSettingsResponse(GameSettings settings)
    {
        Settings = settings;
    }
}
