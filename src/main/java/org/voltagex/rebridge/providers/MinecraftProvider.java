package org.voltagex.rebridge.providers;

import com.google.gson.GsonBuilder;
import net.minecraft.client.settings.GameSettings;
import org.voltagex.rebridge.serializers.GameSettingsSerializer;

import java.lang.reflect.Field;

public class MinecraftProvider implements IMinecraftProvider
{
    public IPlayerProvider player()
    {
        return new SinglePlayerProvider();
    }

    public String getProviderName()
    {
        return "Minecraft Single Player Provider";
    }

    @Override
    public GsonBuilder registerExtraTypeAdapters(GsonBuilder builder)
    {
        builder.registerTypeAdapter(GameSettings.class,new GameSettingsSerializer());
        return builder;
    }
}
