package org.voltagex.rebridge.providers;

import com.google.gson.GsonBuilder;

public interface IMinecraftProvider
{
    IPlayerProvider player();
    String getProviderName();
    GsonBuilder registerExtraTypeAdapters(GsonBuilder builder);
}
