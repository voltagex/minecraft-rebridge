package org.voltagex.exampleextendedmod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "extendedmod", version = "0.01", dependencies = "required-after:Rebridge")
public class ExtendedRouteMod
{
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        org.voltagex.rebridge.Router.AddRoute("someothermod", this.getClass());
    }


    public String Test()
    {
        return "Hello World";
    }
}