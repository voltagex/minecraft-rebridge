package org.voltagex.rebridge.providers;

public class FakeMinecraftProvider implements IMinecraftProvider
{
    public static FakePlayerProvider player;

    public FakeMinecraftProvider()
    {
        player = new FakePlayerProvider();
    }
    public IPlayerProvider player()
    {
        return this.player;
    }

    public String getProviderName()
    {
        return "Fake Minecraft Provider";
    }
}
