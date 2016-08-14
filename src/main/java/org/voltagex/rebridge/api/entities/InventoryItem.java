package org.voltagex.rebridge.api.entities;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryItem
{
    private int id;
    private String name;
    private String displayName;

    public InventoryItem(Item item)
    {
        id = item.getIdFromItem(item);
        name = item.getUnlocalizedName();
    }

    public InventoryItem(ItemStack itemStack)
    {
        this(itemStack.getItem());
        displayName = itemStack.getDisplayName();
    }
}

