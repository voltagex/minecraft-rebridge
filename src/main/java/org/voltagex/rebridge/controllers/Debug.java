package org.voltagex.rebridge.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.TextComponentString;
import org.voltagex.rebridge.Router;
import org.voltagex.rebridge.api.annotations.Controller;
import org.voltagex.rebridge.api.annotations.Parameters;
import org.voltagex.rebridge.api.entities.GameSettingsResponse;
import org.voltagex.rebridge.api.entities.JsonResponse;
import org.voltagex.rebridge.providers.IMinecraftProvider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
public class Debug
{
    private static net.minecraft.client.Minecraft minecraft = Minecraft.getMinecraft();
    private static Gson gson;
    private IMinecraftProvider provider;

    private Debug()
    {

    }

    public Debug(IMinecraftProvider provider)
    {
        this.provider = provider;
    }

    public JsonResponse getRebridge()
    {
        JsonObject root = new JsonObject();
        root.addProperty("Provider", provider.getProviderName());
        return new JsonResponse(NanoHTTPD.Response.Status.OK, root);
    }

    public NanoHTTPD.Response getRoutes()
    {
        return Router.DebugRoutes();
    }

    public JsonResponse getSprites() throws NoSuchFieldException, IllegalAccessException, NanoHTTPD.ResponseException
    {
        if (minecraft == null)
        {
            throw new NanoHTTPD.ResponseException(NanoHTTPD.Response.Status.NOT_FOUND, "Not available");
        }

        Field tmb = minecraft.getTextureMapBlocks().getClass().getDeclaredField("mapRegisteredSprites");
        tmb.setAccessible(true);
        HashMap<String, TextureAtlasSprite> map = ((HashMap<String, TextureAtlasSprite>) tmb.get(minecraft.getTextureMapBlocks()));
        return new JsonResponse(map);
    }

    public GameSettingsResponse getGameSettings()
    {
        return new GameSettingsResponse(minecraft.gameSettings);
    }


    @Parameters(Names = {"Name", "Value"})
    public void postGameSettings(String Name, String Value) throws NoSuchFieldException, IllegalAccessException
    {
        Class<?> settingsClass = minecraft.gameSettings.getClass();
        Field settingField = settingsClass.getDeclaredField(Name);
        Object settingAsObject = Value;
        try
        {
            Class<?> settingType = Class.forName(settingField.get(minecraft.gameSettings).getClass().getName());
            if (settingType == Boolean.class)
            {
                settingAsObject = Boolean.parseBoolean(Value);
            }
            settingField.setAccessible(true);
            settingField.set(minecraft.gameSettings, settingType.cast(settingAsObject));

            minecraft.thePlayer.addChatComponentMessage(new TextComponentString("Rebridge: Set " + Name + " to " + Value));
        } catch (ClassNotFoundException e)
        {
            throw new ClassCastException("Couldn't cast setting " + Value + ": " + e.getMessage());
        }
    }

    public JsonResponse getIconNames() throws Exception
    {
        Field tmb = minecraft.getTextureMapBlocks().getClass().getDeclaredField("mapRegisteredSprites");
        tmb.setAccessible(true);
        HashMap<String, TextureAtlasSprite> map = ((HashMap<String, TextureAtlasSprite>) tmb.get(minecraft.getTextureMapBlocks()));

        ArrayList<String> strings = new ArrayList<String>();
        for (Object o : map.entrySet())
        {
            Map.Entry pair = (Map.Entry) o;
            strings.add((String) pair.getKey());
        }

        return new JsonResponse(strings);
    }
}

