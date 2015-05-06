package org.voltagex.rebridge.api.entities;

public class InventoryItem
{
    private int id;
    private String name;
    private int stackSize;

    public int getStackSize()
    {
        return stackSize;
    }

    public void setStackSize(int stackSize)
    {
        this.stackSize = stackSize;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}

