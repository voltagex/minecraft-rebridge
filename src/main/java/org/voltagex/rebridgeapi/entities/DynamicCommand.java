package org.voltagex.rebridgeapi.entities;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
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

public class DynamicCommand implements ICommand
{
    private String name;
    private String callbackURL;

    public DynamicCommand(String name, String callbackURL)
    {
        this.name = name;
        this.callbackURL = callbackURL;

    }
    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "usage!";
    }

    @Override
    public List getAliases()
    {
        ArrayList<String> aliases = new ArrayList<String>();
        if (name.toLowerCase() != name)
        {
            aliases.add(name.toLowerCase());
        }

        return aliases;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException
    {
        IChatComponent message = new ChatComponentText("executed " + this.name);
        sender.addChatMessage(message);

        HttpClient client = HttpClients.createDefault();
        try
        {
            URIBuilder builder = new URIBuilder(this.callbackURL);
            if (args.length > 0)
            {
                builder.addParameter("parameter", args[0]);
            }
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

                    message = new ChatComponentText(stringBuilder.toString());
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
    public boolean canCommandSenderUse(ICommandSender sender)
    {
        return true;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return false;
    }

    @Override
    public int compareTo(Object o)
    {
        return 0;
    }
}
