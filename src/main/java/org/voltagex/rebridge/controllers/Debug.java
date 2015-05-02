package org.voltagex.rebridge.controllers;

import com.sun.org.glassfish.gmbal.ParameterNames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.IIconCreator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.lwjgl.opengl.GLContext;
import org.voltagex.rebridge.annotations.Controller;
import org.voltagex.rebridge.annotations.Parameters;
import org.voltagex.rebridge.annotations.ResponseMIMEType;
import org.voltagex.rebridge.entities.DebugResponse;
import org.voltagex.rebridge.entities.GameSettingsResponse;
import org.voltagex.rebridge.entities.ServiceResponse;
import org.voltagex.rebridge.entities.StreamResponse;
import org.voltagex.rebridge.providers.IMinecraftProvider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
public class Debug
{
    private static net.minecraft.client.Minecraft minecraft = Minecraft.getMinecraft();

    private Debug()
    {

    }

    public Debug(IMinecraftProvider provider)
    {

    }

    @Parameters(Names={"id"})
    public DebugResponse getItem(String id)
    {
        return new DebugResponse(Item.getItemById(Integer.parseInt(id)).getUnlocalizedName());
    }

    public DebugResponse getItems()
    {

        Map<String,Integer> itemMap = new HashMap<String, Integer>();
        GameData.getItemRegistry().serializeInto(itemMap);
        return new DebugResponse(itemMap);
    }

    public DebugResponse getSprites()
    {
        try
        {
            Field tmb = minecraft.getTextureMapBlocks().getClass().getDeclaredField("mapRegisteredSprites");
            tmb.setAccessible(true);
            HashMap<String,TextureAtlasSprite> map = ((HashMap) tmb.get(minecraft.getTextureMapBlocks()));
            return new DebugResponse(map);
        }
        catch (Exception e)
        {
            return new DebugResponse(false);
        }
    }

    public GameSettingsResponse getGameSettings()
    {
        return new GameSettingsResponse(minecraft.gameSettings);
    }


    @Parameters(Names={"Name", "Value"})
    public DebugResponse postGameSettings(String Name, String Value)
    {
       Class<?> settingsClass = minecraft.gameSettings.getClass();
        try
        {
            Field settingField = settingsClass.getDeclaredField(Name);
            settingField.setAccessible(true);
            try
            {
                settingField.set(null, Value);
            }

            catch (IllegalAccessException e)
            {
                return new DebugResponse(false);
            }
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        return new DebugResponse(true);
    }

    public DebugResponse getIconNames()
    {
        try
        {
            Field tmb = minecraft.getTextureMapBlocks().getClass().getDeclaredField("mapRegisteredSprites");
            tmb.setAccessible(true);
            HashMap<String, TextureAtlasSprite> map = ((HashMap) tmb.get(minecraft.getTextureMapBlocks()));

            ArrayList<String> strings = new ArrayList<String>();
            Iterator it = map.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                strings.add((String)pair.getKey());
            }

            return new DebugResponse(strings);

        }
        catch (Exception e)
        {
            return new DebugResponse(false);
        }
    }

    @ResponseMIMEType(type="image/png")
    public ServiceResponse getItemIcon() throws IOException
    {

        ArrayList<ItemStack> subItems = new ArrayList<ItemStack>();
        Item item70 = Item.getItemById(70);
        item70.getSubItems(item70, null, subItems);

        String iconName = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(subItems.get(0)).getTexture().getIconName();
        try
        {
            String filePath = "textures/" + iconName.split("minecraft:")[1] + ".png";
            InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(filePath)).getInputStream();
            return new StreamResponse(stream);
        }

        catch (IOException ex)
        {
            throw ex;
        }
    }
}
