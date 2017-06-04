package me.sergivb01.rankmanager.ranksync;

import java.util.ArrayList;
import java.util.List;

import me.sergivb01.rankmanager.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class RankSync
        implements CommandExecutor,
        Listener {
    public static List<String> exemptList = new ArrayList<String>();

    public RankSync() {
        super();
    }

    public static boolean setPlayerGroup(Player player, String group) {
        PermHandler.reset(player);
        Rank rank = Rank.getRank(group);
        if (rank == null) return false;
        rank.add(player.getUniqueId());
        String prefix = Rank.getPrefix(player.getUniqueId());
        player.setDisplayName(prefix + player.getName() + ChatColor.RESET);
        PermHandler.applyPerms(player);
        return true;
    }

    public void applyPerms(Player p){
        if (p.hasPermission("xcore.database")) {
            p.getPlayer().sendMessage(ChatColor.RED + "Loading your profile...");
        }
        if (exemptList.contains(p.getUniqueId().toString())) {
            p.sendMessage(ChatColor.DARK_GREEN + "You are exempt from rank synchronization.");
            return;
        }
        RankManager.dbManager.openConnection();
        if (RankManager.dbManager == null || !RankManager.dbManager.checkConnection()) {
            if (!p.hasPermission("xcore.database")) return;
            p.sendMessage(ChatColor.DARK_RED + "Error connecting to database.");
            return;
        }
        String playerRank = RankManager.dbManager.getPlayerRank(p);
        if (!RankSync.setPlayerGroup(p, playerRank)) {
            p.sendMessage(ChatColor.DARK_RED + "An error occurred while loading your profile.");
            return;
        }
        p.sendMessage(ChatColor.GREEN + "Your profile has been loaded.");

    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (e.getPlayer().hasPermission("xcore.database")) {
            e.getPlayer().sendMessage(ChatColor.RED + "Loading your profile...");
        }
        if (exemptList.contains(e.getPlayer().getUniqueId().toString())) {
            e.getPlayer().sendMessage(ChatColor.DARK_GREEN + "You are exempt from rank synchronization.");
            return;
        }
        RankManager.dbManager.openConnection();
        if (RankManager.dbManager == null || !RankManager.dbManager.checkConnection()) {
            if (!e.getPlayer().hasPermission("xcore.database")) return;
            e.getPlayer().sendMessage(ChatColor.DARK_RED + "Error connecting to database.");
            return;
        }
        String playerRank = RankManager.dbManager.getPlayerRank(e.getPlayer());
        if (!RankSync.setPlayerGroup(e.getPlayer(), playerRank)) {
            e.getPlayer().sendMessage(ChatColor.DARK_RED + "An error occurred while loading your profile.");
            return;
        }
        if (!e.getPlayer().hasPermission("xcore.database")) return;
        e.getPlayer().sendMessage(ChatColor.GREEN + "Your profile has been loaded.");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("setrank")) return true;
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Invalid arguments.");
            sender.sendMessage(ChatColor.RED + " /" + label + " <Player> <Rank>");
            return true;
        }
        String playerName = args[0];
        String newRank = args.length > 1 ? args[1] : "";
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (!player.hasPlayedBefore() && !player.isOnline()) {
            sender.sendMessage(ChatColor.RED + "That player was not found.");
            return true;
        }

        ArrayList<String> rankString = new ArrayList<String>();

        for(Rank rank : Rank.getRanks()){
            rankString.add(rank.getName());
        }

        if(!rankString.contains(newRank.toString())){
            sender.sendMessage(ChatColor.RED + "The specified rank does not exist. Please use /ranks in order to get a list of the available ranks");
            return true;
        }

        RankManager.dbManager.setPlayerRank(player, newRank);
        if (player.isOnline()) {
            Player p = player.getPlayer();
            //p.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&6&lThe Veil Network Ranks&7: \n " + "&e&l * &7Old rank: " + Rank.getRank(p.getUniqueId()).getName() + "\n  " + "&e&l * &7New rank: " + newRank + "\n  &7Please log in back to enjoy your new rank"));
            String send = sender.getName();

            if(!(sender instanceof Player)){send = "Console"; }
            String oldRank = Rank.getRank(p.getUniqueId()).getName();
            p.kickPlayer(ChatColor.GOLD + "" + ChatColor.BOLD + "The Veil Rank System\n\n" + ChatColor.translateAlternateColorCodes('&', "&e&l * &7Old rank: " + oldRank + "\n  " + "&e&l * &7New rank: " + newRank + "\n\n      &7Please log in back to enjoy your new rank!      " + "\n Rank set by: &6" + send));
            //p.kickPlayer(ChatColor.AQUA + sender.getName() + " has set your rank to " + newRank + "\n \n \n" + ChatColor.YELLOW + "Please re log in");
            RankSync.setPlayerGroup(p, newRank);
        }
        sender.sendMessage(ChatColor.AQUA + player.getName() + "'s rank to: " + newRank);
        return true;
    }
}
