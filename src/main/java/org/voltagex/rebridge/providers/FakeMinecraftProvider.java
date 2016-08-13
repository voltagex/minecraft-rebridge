package org.voltagex.rebridge.providers;

import com.google.gson.GsonBuilder;

public class FakeMinecraftProvider implements IMinecraftProvider
{
    public static FakePlayerProvider player;

    public FakeMinecraftProvider()
    {
        player = new FakePlayerProvider();
    }

    public IPlayerProvider getPlayer()
    {
        return this.player;
    }
    //todo: Let there be light
    public IWorldProvider getWorld() { return null; }
    public String getProviderName()
    {
        return "Fake Minecraft Provider";
    }

    @Override
    public GsonBuilder registerExtraTypeAdapters(GsonBuilder builder)
    {
        return builder;
    }
}
