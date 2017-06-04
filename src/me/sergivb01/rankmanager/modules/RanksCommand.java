package me.sergivb01.rankmanager.modules;

import me.sergivb01.rankmanager.ranksync.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class RanksCommand implements CommandExecutor {

    public RanksCommand() {
        super();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!sender.hasPermission("core.ranks")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }
        if(sender.hasPermission("core.ranks")) {
            String ranksMsg = "";

            ArrayList<String> msg = new ArrayList<String>();

            for(Rank rank : Rank.getRanks()){
                msg.add(rank.getName());
            }

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lServer ranks&7:"));
            for(int i = 0; i < msg.size(); i++){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l * &7") + msg.get(i));
            }
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Remember to set the player rank in lowercase!!!");

        }
        return true;
    }
}

