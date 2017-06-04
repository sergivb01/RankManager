package me.sergivb01.rankmanager.ranksync;

import java.util.HashMap;

import me.sergivb01.rankmanager.RankManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class PermHandler
{
    private static HashMap<Player, PermissionAttachment> attachmentMap;
    
    public static void disablePermission(final Player player, final String permission) {
        if (!PermHandler.attachmentMap.containsKey(player)) {
            PermHandler.attachmentMap.put(player, player.addAttachment(RankManager.instance));
        }
        PermHandler.attachmentMap.get(player).setPermission(permission, false);
    }
    
    public static void enablePermission(final Player player, final String permission) {
        if (!PermHandler.attachmentMap.containsKey(player)) {
            PermHandler.attachmentMap.put(player, player.addAttachment(RankManager.instance));
        }
        PermHandler.attachmentMap.get(player).setPermission(permission, true);
    }
    
    public static void reset(final Player player) {
        for (final Rank rank : Rank.getRanks(player.getUniqueId())) {
            rank.remove(player.getUniqueId());
        }
        if (PermHandler.attachmentMap.get(player) != null) {
            player.removeAttachment(PermHandler.attachmentMap.get(player));
            PermHandler.attachmentMap.remove(player);
        }
    }
    
    public static void applyRank(final Player player) {
        for (final Rank rank : Rank.getDefaultRanks()) {
            if (!rank.contains(player.getUniqueId())) {
                rank.add(player.getUniqueId());
            }
        }
        final String prefix = Rank.getPrefix(player.getUniqueId());
        player.setDisplayName(prefix + player.getName() + ChatColor.RESET);
    }
    
    public static void applyPerms(final Player player) {
        for (final Rank rank : Rank.getRanks(player.getUniqueId())) {
            for (final String permission : rank.getPermissions()) {
                if (!player.hasPermission(permission)) {
                    enablePermission(player, permission);
                }
            }
        }
        for (final Rank rank : Rank.getRanks(player.getUniqueId())) {
            for (final String permission : rank.getDisabledPermissions()) {
                disablePermission(player, permission);
            }
        }
    }
    
    static {
        PermHandler.attachmentMap = new HashMap<Player, PermissionAttachment>();
    }
}
