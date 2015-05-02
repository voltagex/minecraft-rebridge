package org.voltagex.rebridge;

import fi.iki.elonen.NanoHTTPD;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.voltagex.rebridge.entities.DynamicCommand;
import org.voltagex.rebridge.providers.FakeMinecraftProvider;
import org.voltagex.rebridge.providers.IMinecraftProvider;
import org.voltagex.rebridge.providers.MinecraftProvider;

import java.util.Set;

@Mod(modid = Consts.MODID, version = Consts.VERSION)
public class Rebridge extends NanoHTTPD
{
    private static Configuration config;
    private static Router router;
    private static IMinecraftProvider provider;
    public Rebridge()
    {
        //problem, MC freezes if the port can't be bound. Why?
        super(9999);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        router = new Router(new MinecraftProvider());
        ServerRunner.run(Rebridge.class);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        ConfigCategory webHooks = config.getCategory("WebHooks");
        if (webHooks.getChildren().size() < 1)
        {
            ConfigCategory exampleCommandCategory = new ConfigCategory("example", webHooks);

            exampleCommandCategory.put("", new Property("URL", "https://httpbin.org/get", Property.Type.STRING));
            exampleCommandCategory.put("disabled", new Property("disabled", "true", Property.Type.BOOLEAN));
            config.save();
        }
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        Set<ConfigCategory> hooks = config.getCategory("WebHooks").getChildren();
        for (ConfigCategory hook : hooks)
        {

            String commandName = hook.getName();
            String logMessage;
            if (!hook.get("disabled").getBoolean(false))
            {
                String commandURL = hook.get("URL").getString();
                DynamicCommand newCommand = new DynamicCommand(commandName, commandURL);
                event.registerServerCommand(newCommand);
                logMessage = String.format("Loaded command hook %s", commandName);
            }
            else
            {
                logMessage = String.format("Skipped loading disabled hook %s", commandName);
            }

            System.out.println(logMessage);
        }
    }


    @Override
    public Response serve(IHTTPSession session)
    {
        return router.route(session);
    }

    public static void main(String[] args)
    {
        router = new Router(new FakeMinecraftProvider());
        ServerRunner.run(Rebridge.class);
        try
        {
            System.in.read();
        }
        catch (Throwable ignored)
        {
        }
        System.out.println("Server stopped.\n");
    }

}