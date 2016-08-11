package org.voltagex.rebridge.api.entities;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DynamicCommand extends CommandBase
{
    private String name;
    private String callbackURL;

    public DynamicCommand(String name, String callbackURL)
    {
        this.name = name;
        this.callbackURL = callbackURL;

    }
    @Override
    public String getCommandName()
    {
        return this.name;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "usage!";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List getCommandAliases()
    {
        ArrayList<String> aliases = new ArrayList<String>();
        if (name.toLowerCase() != name)
        {
            aliases.add(name.toLowerCase());
        }

        return aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException
    {
        ITextComponent message = new TextComponentString("executed " + this.name);
        sender.addChatMessage(message);

        HttpClient client = HttpClients.createDefault();
        try
        {
            URIBuilder builder = new URIBuilder(this.callbackURL);
            //todo: handle new parameter format
            /*if (args.length > 0)
            {
                builder.addParameter("parameter", args[0]);
            }*/
                HttpGet getRequest = new HttpGet(builder.build());
                HttpResponse response = client.execute(getRequest);

                try
                {
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    while ((line = reader.readLine()) != null)
                    {
                        stringBuilder.append(line);
                    }

                    message = new TextComponentString(stringBuilder.toString());
                    sender.addChatMessage(message);
                }

                catch (IOException e)
                {

                }
        }

        catch (Exception e)
        {
            throw new CommandException(e.getMessage());
        }
    }



    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return false;
    }

}
