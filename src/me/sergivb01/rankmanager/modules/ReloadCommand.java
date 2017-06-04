package me.sergivb01.rankmanager.modules;

import me.sergivb01.rankmanager.RankManager;
import me.sergivb01.rankmanager.ranksync.RankSync;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {
    private RankSync rankSync = new RankSync();

    public ReloadCommand() {
        super();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!sender.hasPermission("core.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }
        if(sender.hasPermission("core.reload")) {
            RankManager.instance.registerRanks();
            RankManager.instance.reloadConfig();
            //RankManager.instance.manager.closeConnection();
            //RankManager.instance.manager.openConnection();
            RankManager.dbManager.closeConnection();
            RankManager.dbManager.openConnection();
            sender.sendMessage(ChatColor.RED + "Reloaded!");

            for(Player p : Bukkit.getOnlinePlayers()){
                rankSync.applyPerms(p);
                p.sendMessage(ChatColor.GREEN + "Permissions applied");
            }


        }
        return true;
    }
}

