package org.voltagex.rebridge.entities;

import java.util.ArrayList;

public class Inventory extends ListResponse<InventoryItem>
{
    ArrayList<InventoryItem> containedList;
    public Inventory(ArrayList<InventoryItem> list)
    {
        containedList = list;
    }

    public ArrayList<InventoryItem> getList()
    {
        return containedList;
    }
}
