package me.sergivb01.rankmanager;

import me.sergivb01.rankmanager.modules.*;
import me.sergivb01.rankmanager.ranksync.StatsListener;
import me.sergivb01.rankmanager.ranksync.SQLManager;
import me.sergivb01.rankmanager.utilities.DomUtil;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import me.sergivb01.rankmanager.ranksync.Rank;
import me.sergivb01.rankmanager.ranksync.RankSync;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RankManager extends JavaPlugin implements CommandExecutor {
    public static RankManager instance;
    private String hostname;
    private String port;
    private String database;
    private String username;
    private String password;
    public static SQLManager dbManager;
    public static String defaultRank;g
    public NoRain norain;
    public SQLManager manager;
    
    public RankManager() {
        this.hostname = "127.0.0.1";
        this.port = "3306";
        this.database = "hcfstats";
        this.username = "root";
        this.password = "%f8jgr*g3@%ec1aj2854wdc6&";
    }

    public void onEnable() {
        (RankManager.instance = this).loadConfiguration();

        RankManager.dbManager = new SQLManager(this.hostname, this.port, this.database, this.username, this.password);
        if (!RankManager.dbManager.setupDatabase()) {
            Bukkit.getServer().getLogger().log(Level.WARNING, "Error setting up database table 'ranks'.");
        }
        registerCommands();
        this.registerRanks();
        registerEvents();
    }

    public void registerCommands() {
        RankSync rankSync = new RankSync();

        this.getCommand("ranks").setExecutor(new RanksCommand());
        this.getCommand("ping").setExecutor(new PingPlayer());
        this.getCommand("corerl").setExecutor(new ReloadCommand());
        this.getCommand("setrank").setExecutor(rankSync);
    }

    public void registerEvents() {
        RankSync rankSync = new RankSync();

        getServer().getPluginManager().registerEvents(rankSync, this);
        getServer().getPluginManager().registerEvents(new StatsListener(), this);
        getServer().getPluginManager().registerEvents(new PingUtils(), this);
        (this.norain = new NoRain(RankManager.instance)).init();
    }

    public void onDisable() {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Core disabled.");
    }
    
    public void registerRanks() {
        final File file = new File(this.getDataFolder(), "ranks.xml");
        if (!file.exists()) {
            try {
                FileUtils.copyInputStreamToFile(this.getResource("ranks.xml"), file);
            }
            catch (IOException e) {
                e.printStackTrace();
                this.getLogger().warning("Could not copy default ranks file to 'ranks.xml'.");
            }
        }
        try {
            final Document document = DomUtil.parse(file);
            for (final Element rank : document.getRootElement().getChildren("rank")) {
                final String name = rank.getAttributeValue("name");
                final boolean defaultRank = rank.getAttributeValue("default") != null && this.parseBoolean(rank.getAttributeValue("default"));
                final String parent = rank.getAttributeValue("parent");
                final String flair = (rank.getAttributeValue("flair") != null) ? ChatColor.translateAlternateColorCodes('`', rank.getAttributeValue("flair")) : "";
                final List<String> permissions = new ArrayList<String>();
                final List<String> disabledPermissions = new ArrayList<String>();
                final List<String> users = new ArrayList<String>();
                for (final Element permission : rank.getChildren("permission")) {
                    if (permission.getText().startsWith("-")) {
                        disabledPermissions.add(permission.getText().substring(1));
                    }
                    else {
                        permissions.add(permission.getText());
                    }
                }
                new Rank(name, defaultRank, flair, users, permissions, disabledPermissions, parent);
            }
            for (final Rank rank2 : Rank.getRanks()) {
                if (rank2.getParent() != null && Rank.getRank(rank2.getParent()) != null && Rank.getRank(rank2.getParent()).getParent() != null && Rank.getRank(Rank.getRank(rank2.getParent()).getParent()) != null && Rank.getRank(Rank.getRank(rank2.getParent()).getParent()).equals(rank2)) {
                    this.getLogger().warning("Rank inheritance processes were terminated because " + rank2.getName() + " and " + Rank.getRank(rank2.getParent()).getName() + " are parents of each other, which cannot occur.");
                    return;
                }
            }
            final List<Rank> completed = new ArrayList<>();
            for (final Rank rank3 : Rank.getRanks()) {
                if (rank3.getParent() == null) {
                    completed.add(rank3);
                }
            }
            while (!completed.containsAll(Rank.getRanks())) {
                Rank inheriting = null;
                for (final Rank rank4 : Rank.getRanks()) {
                    if (!completed.contains(rank4) && rank4.getParent() != null && completed.contains(Rank.getRank(rank4.getParent()))) {
                        inheriting = rank4;
                    }
                }
                for (final String permission2 : Rank.getRank(inheriting.getParent()).getPermissions()) {
                    inheriting.addPermission(permission2);
                }
                completed.add(inheriting);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            this.getLogger().warning("Could not parse file 'ranks.xml' for ranks.");
        }
        catch (JDOMException e2) {
            e2.printStackTrace();
            this.getLogger().warning("Could not parse file 'ranks.xml' for ranks.");
        }
    }
    
    private boolean parseBoolean(final String string) {
        return string.equalsIgnoreCase("on") || (!string.equalsIgnoreCase("off") && Boolean.parseBoolean(string));
    }
    
    public void loadConfiguration() {
        final FileConfiguration c = this.getConfig();
        c.addDefault("Database.hostname", this.hostname);
        c.addDefault("Database.port", this.port);
        c.addDefault("Database.database", this.database);
        c.addDefault("Database.username", this.username);
        c.addDefault("Database.password", this.password);
        c.addDefault("RankSync.DefaultRank", RankManager.defaultRank);
        c.addDefault("RankSync.ExemptList", RankSync.exemptList);
        c.options().copyDefaults(true);
        this.saveConfig();
        this.hostname = c.getString("Database.hostname", this.hostname);
        this.database = c.getString("Database.database", this.database);
        this.username = c.getString("Database.username", this.username);
        this.password = c.getString("Database.password", this.password);
        RankManager.defaultRank = c.getString("DefaultRank", RankManager.defaultRank);
        RankSync.exemptList = c.getStringList("RankSync.ExemptList");
    }
    
    static {
        RankManager.defaultRank = "User";
    }
}