package org.voltagex.rebridge.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;
import org.voltagex.rebridge.providers.IMinecraftProvider;
import org.voltagex.rebridge.api.annotations.Controller;
import org.voltagex.rebridge.api.annotations.Parameters;
import org.voltagex.rebridge.api.entities.GameSettingsResponse;
import org.voltagex.rebridge.api.entities.ObjectResponse;
import org.voltagex.rebridge.api.entities.ServiceResponse;
import org.voltagex.rebridge.api.entities.StreamResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
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
