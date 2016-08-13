package org.voltagex.rebridge.providers;

import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import org.voltagex.rebridge.serializers.GameSettingsSerializer;

import java.lang.reflect.Field;

public class SingleWorldProvider implements IWorldProvider
{
    private static net.minecraft.client.Minecraft minecraft = Minecraft.getMinecraft();
    public SingleWorldProvider()
    {

    }

    @Override
    public String getName()
    {
        return minecraft.theWorld.getWorldType().getWorldTypeName();
    }

    @Override
    public void setTime(long time)
    {
        minecraft.theWorld.setWorldTime(time);
    }

    @Override
    public long getTime()
    {
       return minecraft.theWorld.getWorldTime();
    }
}
