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

import java.io.IOException;
import java.net.SocketException;

@Mod(modid = Consts.MODID, version = Consts.VERSION)
public class Rebridge extends NanoHTTPD
{
    private final Router router = new Router();

    public Rebridge()
    {
        //problem, MC freezes if the port can't be bound. Why?
        super(9999);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        ServerRunner.run(Rebridge.class);
    }

    @Override
    public Response serve(IHTTPSession session)
    {
        return router.route(session);
    }
}