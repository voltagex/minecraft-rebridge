package org.voltagex.exampleextendedmod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.voltagex.rebridge.api.annotations.Controller;

@Mod(modid = "extendedmod", version = "0.01", dependencies = "required-after:Rebridge")
@Controller
public class ExtendedRouteMod
{
    public ExtendedRouteMod()
    {

    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        //org.voltagex.rebridge.Router.AddRoute("someothermod", this.getClass());
        FMLInterModComms.sendMessage("Rebridge","register","extendedmod");
    }

    public String getTest()
    {
        return "Hello World";
    }
}