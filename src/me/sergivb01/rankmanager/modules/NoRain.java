package me.sergivb01.rankmanager.modules;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.Plugin;

public class NoRain
implements Listener {
    private Plugin plugin;

    public NoRain(Plugin plugin) {
        super();
        this.plugin = plugin;
    }

    public void init() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void onRain(WeatherChangeEvent e) {
        final World w = e.getWorld();
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable(){

            @Override
            public void run() {
                if (w.hasStorm()) {
                    w.setStorm(false);
                }
                if (!w.isThundering()) return;
                w.setThundering(false);
            }
        }, 5);
    }

}

