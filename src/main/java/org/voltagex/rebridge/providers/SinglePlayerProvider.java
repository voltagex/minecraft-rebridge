package org.voltagex.rebridge.providers;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.RegistryNamespaced;
import org.voltagex.rebridge.entities.Inventory;
import org.voltagex.rebridge.entities.InventoryItem;
import org.voltagex.rebridge.entities.Position;

import java.util.ArrayList;
import java.util.List;

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

    public Inventory getInventory()
    {
        ArrayList<InventoryItem> list = new ArrayList<InventoryItem>();

        if (minecraft.thePlayer == null)
        {
            return null;
        }

        ItemStack[] minecraftInventory = minecraft.thePlayer.inventory.mainInventory;
        RegistryNamespaced registry = net.minecraftforge.fml.common.registry.GameData.getItemRegistry();
        for (ItemStack item : minecraftInventory)
        {


            if (item == null)
            {
                continue;
            }

            Item innerItem = item.getItem();
            InventoryItem newItem = new InventoryItem();
            newItem.setName(item.getDisplayName());
            newItem.setStackSize(item.stackSize);
            newItem.setId(Item.getIdFromItem(innerItem));

            list.add(newItem);
        }

        return new Inventory(list);
    }



}
