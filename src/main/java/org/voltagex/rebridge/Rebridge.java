package org.voltagex.rebridge;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import fi.iki.elonen.NanoHTTPD;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.voltagex.rebridge.api.annotations.Controller;
import org.voltagex.rebridge.api.entities.DynamicCommand;
import org.voltagex.rebridge.providers.FakeMinecraftProvider;
import org.voltagex.rebridge.providers.IMinecraftProvider;
import org.voltagex.rebridge.providers.MinecraftProvider;

import java.util.List;
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

    //todo: is there a better way of setting up for testing?
    public Response serve(Router router, IHTTPSession session)
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

    @Mod.EventHandler
    public void processIMC(FMLInterModComms.IMCEvent event)
    {
        for (final FMLInterModComms.IMCMessage imcMessage : event.getMessages())
        {
            ModContainer container;
            if (imcMessage.key == "register")
            {

                List<ModContainer> modContainers = Loader.instance().getActiveModList();
                container = Iterables.find(modContainers, new Predicate<ModContainer>()
                {
                    public boolean apply(ModContainer input)
                    {
                        return input.getName().contains(imcMessage.getSender());
                    }
                }, null);

                Reflections reflections;

                //todo: do this once per run, not per call
                reflections = new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(container.getMod().getClass().getPackage().getName()))
                        .setScanners(
                                new SubTypesScanner(true),
                                new TypeAnnotationsScanner(),
                                new FieldAnnotationsScanner(),
                                new MethodAnnotationsScanner(),
                                new MethodParameterScanner(),
                                new MethodParameterNamesScanner(),
                                new MemberUsageScanner()));
                List<Class<?>> newControllers = Lists.newArrayList(reflections.getTypesAnnotatedWith(Controller.class));

                //todo: this nasty hack shouldn't really be needed but I only really want to import one "level" of controllers
                   List<Class<?>> filteredControllers = Lists.newArrayList(Iterables.filter(newControllers, new Predicate<Class<?>>() {
                       @Override
                       public boolean apply(Class<?> input)
                       {
                           return input.getName().contains(imcMessage.getSender());
                       }
                   }));

                router.addRoute(imcMessage.getSender(),filteredControllers);
            }
        }
    }

}