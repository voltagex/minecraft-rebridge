package org.voltagex.rebridge.providers;

import com.google.gson.GsonBuilder;

public interface IWorldProvider
{
    String getName();

    void setTime(long time);

    long getTime();
}
