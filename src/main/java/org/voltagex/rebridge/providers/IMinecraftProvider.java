package org.voltagex.rebridge.providers;

import com.google.gson.GsonBuilder;

public interface IMinecraftProvider
{
    IPlayerProvider getPlayer();
    IWorldProvider getWorld();
    String getProviderName();
    GsonBuilder registerExtraTypeAdapters(GsonBuilder builder);
}
