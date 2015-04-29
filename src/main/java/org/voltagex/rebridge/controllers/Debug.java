package org.voltagex.rebridge.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RegistryNamespaced;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.voltagex.rebridge.annotations.Controller;
import org.voltagex.rebridge.entities.DebugResponse;
import org.voltagex.rebridge.providers.IMinecraftProvider;

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
}
