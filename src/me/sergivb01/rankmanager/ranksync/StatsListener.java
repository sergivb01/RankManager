package me.sergivb01.rankmanager.ranksync;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class StatsListener
implements Listener {

    SQLManager SQLManager;
    Rank rank;
    public StatsListener() {
        super();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) throws SQLException {
        Player p = (Player) e.getPlayer();
        PermHandler.applyRank(p);
        PermHandler.applyPerms(p);

    }

    /*@EventHandler(priority= EventPriority.HIGHEST)
    public void chatFormatting(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String format = Rank.getRank(p.getName()).getFlair() + "&9: &f:";
        e.setFormat(ChatColor.translateAlternateColorCodes('&', format));
    }*/
}

