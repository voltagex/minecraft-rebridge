package org.voltagex.rebridge.entities;

public class PositionResponse extends ServiceResponse
{
    /**
     * Represents x, y, z coordinates from Minecraft (BlockPos)
     */
    public PositionResponse()
    {
    }

    /**
     * Represents x, y, z coordinates from Minecraft (BlockPos)
     * @param x
     * @param y
     * @param z
     */
    public PositionResponse(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private float x;
    private float y;

    public float getZ()
    {
        return z;
    }

    public void setZ(float z)
    {
        this.z = z;
    }

    public float getX()
    {
        return x;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public float getY()
    {
        return y;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    private float z;
}
