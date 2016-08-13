package org.voltagex.rebridge.controllers;

import org.voltagex.rebridge.api.annotations.Controller;
import org.voltagex.rebridge.api.annotations.Parameters;
import org.voltagex.rebridge.providers.IMinecraftProvider;
import org.voltagex.rebridge.providers.IWorldProvider;

/**
 * Created by live on 13/08/2016.
 */
@Controller
public class World
{
    private static IWorldProvider world;
    public World(IMinecraftProvider provider)
    {
        world = provider.getWorld();
    }

    @Parameters(Names={"Time"})
    public void postTime(String time)
    {
        world.setTime(Long.parseLong(time));
    }

    public long getTime()
    {
        return world.getTime();
    }
}
