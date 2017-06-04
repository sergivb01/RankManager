package me.sergivb01.rankmanager.modules;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingPlayer
implements CommandExecutor {
    public PingPlayer() {
        super();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player) && args.length == 0) {
            PingUtils.msg(sender, "&cOnly players can execute this command!");
            return true;
        }
        if (!sender.hasPermission("core.ping")) {
            PingUtils.msg(sender, "&cYou don't have permission to use this command.");
            return true;
        }
        if (args.length == 0) {
            try {
                PingUtils.msg(sender, "&6%player%'s ping&e: %ping%ms".replace("%player%", sender.getName()).replace("%ping%", String.valueOf(PingUtils.getPlayerPing((Player)sender))));
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            PingUtils.msg(sender, "&cPlayer %player% is not online.".replace("%player%", args[0]));
            return true;
        }
        try {
            PingUtils.msg(sender, "&6%player%'s ping&e: %ping%ms".replace("%player%", player.getName()).replace("%ping%", String.valueOf(PingUtils.getPlayerPing(player))));
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}

