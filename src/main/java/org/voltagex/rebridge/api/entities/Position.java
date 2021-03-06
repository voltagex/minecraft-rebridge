package org.voltagex.rebridge.api.entities;

public class Position extends ServiceResponse
{
    /**
     * Represents x, y, z coordinates from Minecraft (BlockPos)
     */
    public Position()
    {
    }

    /**
     * Represents x, y, z coordinates from Minecraft (BlockPos)
     * @param x
     * @param y
     * @param z
     */
    public Position(Float x, Float y, Float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private Float x;
    private Float y;

    public Float getZ()
    {
        return z;
    }

    public void setZ(Float z)
    {
        this.z = z;
    }

    public Float getX()
    {
        return x;
    }

    public void setX(Float x)
    {
        this.x = x;
    }

    public Float getY()
    {
        return y;
    }

    public void setY(Float y)
    {
        this.y = y;
    }

    private Float z;
}
