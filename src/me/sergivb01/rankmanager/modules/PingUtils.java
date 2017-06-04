package me.sergivb01.rankmanager.modules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class PingUtils implements Listener
{
    private static final Logger logger;
    
    public static void msg(final CommandSender target, final String message) {
        target.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public static void log(final String message) {
        PingUtils.logger.info(message);
    }
    
    public static int getPlayerPing(final Player player) throws Exception {
        final boolean ping = false;
        final Class craftPlayer = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
        final Object converted = craftPlayer.cast(player);
        final Method handle = converted.getClass().getMethod("getHandle", (Class<?>[])new Class[0]);
        final Object entityPlayer = handle.invoke(converted);
        final Field pingField = entityPlayer.getClass().getField("ping");
        final int ping2 = pingField.getInt(entityPlayer);
        return ping2;
    }
    
    public static String getServerVersion() {
        final Pattern brand = Pattern.compile("(v|)[0-9][_.][0-9][_.][R0-9]*");
        final String pkg = Bukkit.getServer().getClass().getPackage().getName();
        String version = pkg.substring(pkg.lastIndexOf(46) + 1);
        if (!brand.matcher(version).matches()) {
            version = "";
        }
        return version;
    }
    
    public static void warning(final String message) {
        PingUtils.logger.warning(message);
    }
    
    static {
        logger = Bukkit.getLogger();
    }
}
