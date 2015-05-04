package org.voltagex.rebridge.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.voltagex.rebridge.providers.IMinecraftProvider;
import org.voltagex.rebridgeapi.annotations.Controller;
import org.voltagex.rebridgeapi.annotations.Parameters;
import org.voltagex.rebridgeapi.entities.StreamResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@Controller
public class Item
{
    private IMinecraftProvider provider;

    private Item()
    {

    }

    public Item(IMinecraftProvider provider)
    {
        this.provider = provider;
    }

    @Parameters(Names = {"Name"})
    public StreamResponse getIcon(String Name) throws IOException
    {
        ArrayList<ItemStack> subItems = new ArrayList<ItemStack>();
        net.minecraft.item.Item item70 = net.minecraft.item.Item.getByNameOrId(Name);
        item70.getSubItems(item70, null, subItems);

        String iconName = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(subItems.get(0)).getTexture().getIconName();
        String filePath = "textures/" + iconName.split("minecraft:")[1] + ".png";
        InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(filePath)).getInputStream();
        return new StreamResponse(stream, "image/png");
    }
}

