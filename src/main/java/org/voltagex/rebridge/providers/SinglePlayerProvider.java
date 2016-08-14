package org.voltagex.rebridge.providers;

import fi.iki.elonen.NanoHTTPD;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fml.common.registry.GameData;
import org.voltagex.rebridge.api.entities.InventoryItem;
import org.voltagex.rebridge.api.entities.JsonResponse;
import org.voltagex.rebridge.api.entities.Position;

import java.util.ArrayList;

public class SinglePlayerProvider implements IPlayerProvider
{
    private static net.minecraft.client.Minecraft minecraft = Minecraft.getMinecraft();
    public Boolean playerIsOnServer()
    {
        return minecraft.thePlayer != null;
    }

    public Position getPosition()
    {
        if (minecraft.thePlayer == null)
        {
            return null;
        }

        BlockPos minecraftPosition = minecraft.thePlayer.getPosition();
        Position position = new Position((float)minecraftPosition.getX(),(float)minecraftPosition.getY(),(float)minecraftPosition.getZ());
        return position;
    }

    public void setPosition(Position position)
    {
        if (minecraft.thePlayer == null)
        {
            return;
        }

        Position currentPos = getPosition();

        if (position.getX() == null)
        {
            position.setX(currentPos.getX());
        }

        if (position.getY() == null)
        {
            position.setY(currentPos.getY());
        }

        if (position.getZ() == null)
        {
            position.setZ(currentPos.getZ());
        }

        minecraft.thePlayer.setPositionAndUpdate(position.getX(),position.getY(),position.getZ());
    }

    public String getName()
    {
        if (minecraft.thePlayer == null)
        {
            return null;
        }

        return minecraft.thePlayer.getName();
    }

    public JsonResponse getInventory()
    {
        //todo: Clean this up so it's only doing JSON stuff closer to the controller/action
        ArrayList<InventoryItem> list = new ArrayList<InventoryItem>();

        if (minecraft.thePlayer == null)
        {
            return null;
        }

        ItemStack[] minecraftInventory = minecraft.thePlayer.inventory.mainInventory;
        //Deprecated but I can't find the replacement
        RegistryNamespaced registry = GameData.getItemRegistry();

        for (ItemStack item : minecraftInventory)
        {
            if (item == null)
            {
                continue;
            }

            InventoryItem newItem = new InventoryItem(item);
            list.add(newItem);
        }

        return new JsonResponse(list);
    }

    @Override
    public Boolean giveItem(String ItemName, int Amount) throws NanoHTTPD.ResponseException
    {
        Item item = Item.getByNameOrId(ItemName.toLowerCase());
        if (item == null)
        {
            throw new NanoHTTPD.ResponseException(NanoHTTPD.Response.Status.NOT_FOUND, "Item " + ItemName + " not found");
        }
        return minecraft.thePlayer.inventory.addItemStackToInventory(new ItemStack(item,Amount));
    }
}
