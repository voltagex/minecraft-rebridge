package org.voltagex.rebridge.providers;

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
}
