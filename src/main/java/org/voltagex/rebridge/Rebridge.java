package org.voltagex.rebridge;

import fi.iki.elonen.NanoHTTPD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minidev.json.JSONObject;

@Mod(modid = Consts.MODID, version = Consts.VERSION)
public class Rebridge extends NanoHTTPD {


    public Rebridge() {
        //problem, MC freezes if the port can't be bound. Why?
        super(9999);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ServerRunner.run(Rebridge.class);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String path = session.getUri();
        if (path.equals("/status")) {
            String retVal = "This is ExampleMod/Rebridge running on Minecraft " + FMLClientHandler.instance().getClient().getVersion();
            retVal += "\nForge version " + ForgeVersion.getVersion();
            retVal += "\nRequest is " + session.getUri();
            return new Response(Response.Status.ACCEPTED, "text/plain", retVal);
        }
        if (path.equals("/session")) {
            return GetSessionInformation(session);
        }

        if (path.equals("/player")) {
            return GetPlayerInformation(session);
        }
        return new Response("Bad Request"); //TODO: return proper 400
    }

    public Response GetSessionInformation(IHTTPSession session) {
        JSONObject o = new JSONObject();
        o.put("session", Minecraft.getSessionInfo());
        return new Response(o.toString());
    }

    public Response GetPlayerInformation(IHTTPSession session) {
        try {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

            JSONObject o = new JSONObject();
            JSONObject p = new JSONObject();

            o.put("x", player.getPosition().getX());
            o.put("y", player.getPosition().getY());
            o.put("z", player.getPosition().getZ());
            p.put("position", o);

            return new Response(Response.Status.OK, "text/json", p.toString());
        } catch (NullPointerException npe) {
            return new Response(Response.Status.NOT_FOUND, "text/json", "Player not found");
        }
    }

}
