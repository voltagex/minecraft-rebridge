package org.voltagex.rebridge.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.voltagex.rebridge.annotations.Controller;
import org.voltagex.rebridge.annotations.Parameters;
import org.voltagex.rebridge.entities.DebugResponse;
import org.voltagex.rebridge.providers.IMinecraftProvider;

import java.lang.reflect.Field;
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

    public DebugResponse getItems()
    {

        Map<String,Integer> itemMap = new HashMap<String, Integer>();
        GameData.getItemRegistry().serializeInto(itemMap);
        return new DebugResponse(itemMap);
    }

    public DebugResponse getGameSettings()
    {
        Class<?> settingsClass = minecraft.gameSettings.getClass();
        return new DebugResponse((Field[])(settingsClass.getDeclaredFields()));
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
}
