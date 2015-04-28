package org.voltagex.rebridge;

import fi.iki.elonen.NanoHTTPD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.voltagex.rebridge.providers.FakeMinecraftProvider;
import org.voltagex.rebridge.providers.IMinecraftProvider;
import org.voltagex.rebridge.providers.MinecraftProvider;

import java.io.IOException;
import java.net.SocketException;

@Mod(modid = Consts.MODID, version = Consts.VERSION)
public class Rebridge extends NanoHTTPD
{
    private static Router router;
    private static IMinecraftProvider provider;
    public Rebridge()
    {
        //problem, MC freezes if the port can't be bound. Why?
        super(9999);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        router = new Router(new MinecraftProvider());
        ServerRunner.run(Rebridge.class);
    }

    @Override
    public Response serve(IHTTPSession session)
    {
        return router.route(session);
    }

    public static void main(String[] args)
    {
        router = new Router(new FakeMinecraftProvider());
        ServerRunner.run(Rebridge.class);
        try
        {
            System.in.read();
        }
        catch (Throwable ignored)
        {
        }
        System.out.println("Server stopped.\n");
    }

}