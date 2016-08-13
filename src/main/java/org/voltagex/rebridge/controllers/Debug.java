package org.voltagex.rebridge.controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.registry.GameData;
import org.voltagex.rebridge.Consts;
import org.voltagex.rebridge.Router;
import org.voltagex.rebridge.api.entities.*;
import org.voltagex.rebridge.providers.IMinecraftProvider;
import org.voltagex.rebridge.api.annotations.Controller;
import org.voltagex.rebridge.api.annotations.Parameters;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;

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
        root.addProperty("Provider",provider.getProviderName());
        return new JsonResponse(NanoHTTPD.Response.Status.OK, root);
    }

    public NanoHTTPD.Response getRoutes()
    {
        return Router.DebugRoutes();
    }

    //todo: can this still be done?
    public ObjectResponse getItems()
    {
        //Map<String,Integer> itemMap = new HashMap<String, Integer>();
        //GameData.getItemRegistry().serializeInto(itemMap);
        //return new ObjectResponse(itemMap);
        return null;
    }

    public ObjectResponse getSprites() throws NoSuchFieldException, IllegalAccessException
    {
        if (minecraft == null)
        {
            return new ObjectResponse("not available");
        }

        Field tmb = minecraft.getTextureMapBlocks().getClass().getDeclaredField("mapRegisteredSprites");
        tmb.setAccessible(true);
        HashMap<String, TextureAtlasSprite> map = ((HashMap<String, TextureAtlasSprite>) tmb.get(minecraft.getTextureMapBlocks()));
        return new ObjectResponse(map);
    }

    public GameSettingsResponse getGameSettings()
    {
        return new GameSettingsResponse(minecraft.gameSettings);
    }


    @Parameters(Names={"Name", "Value"})
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
        }
        catch (ClassNotFoundException e)
        {
            throw new ClassCastException("Couldn't cast setting " + Value + ": " + e.getMessage());
        }
    }

    public ObjectResponse getIconNames() throws Exception
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

        return new ObjectResponse(strings);
    }

    public ServiceResponse getItemIcon() throws IOException
    {
        /*ArrayList<ItemStack> subItems = new ArrayList<ItemStack>();
        Item item = Item.getItemById(70);
        item.getSubItems(item, null, subItems);

        //todo: should still be able to grab the item icon here
        String iconName = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(subItems.get(0)).getTexture().getIconName();
        String filePath = "textures/" + iconName.split("minecraft:")[1] + ".png";
        InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(filePath)).getInputStream();
        return new StreamResponse(stream, "image/png");*/
        return null;
    }
}
