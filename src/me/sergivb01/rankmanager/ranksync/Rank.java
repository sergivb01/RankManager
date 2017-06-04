package me.sergivb01.rankmanager.ranksync;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Rank
{
    private static List<Rank> ranks;
    private String name;
    private String flair;
    private List<String> users;
    private List<String> disabledPermissions;
    private String parent;
    private boolean defaultRank;
    private List<String> permissions;
    
    public Rank(final String name, final boolean defaultRank, final String flair, final List<String> users, final List<String> permissions, final List<String> disabledPermissions, final String parent) {
        this.name = name;
        this.defaultRank = defaultRank;
        this.flair = flair;
        this.users = users;
        this.permissions = permissions;
        this.disabledPermissions = disabledPermissions;
        this.parent = parent;
        Rank.ranks.add(this);
    }
    
    public static List<Rank> getRanks() {
        return Rank.ranks;
    }
    
    public static List<Rank> getDefaultRanks() {
        final List<Rank> results = new ArrayList<Rank>();
        for (final Rank rank : Rank.ranks) {
            if (rank.isDefaultRank()) {
                results.add(rank);
            }
        }
        return results;
    }
    
    public static List<Rank> getRanks(final UUID uuid) {
        final List<Rank> ranks = new ArrayList<Rank>();
        for (final Rank rank : Rank.ranks) {
            if (rank.contains(uuid)) {
                ranks.add(rank);
            }
        }
        return ranks;
    }
    
    public static Rank getRank(final String name) {
        for (final Rank rank : Rank.ranks) {
            if (rank.getName().equalsIgnoreCase(name)) {
                return rank;
            }
        }
        for (final Rank rank : Rank.ranks) {
            if (rank.getName().toLowerCase().startsWith(name.toLowerCase())) {
                return rank;
            }
        }
        return null;
    }
    
    public static String getPrefix(final UUID uuid) {
        String prefix = "";
        for (final Rank rank : getRanks(uuid)) {
            if (rank.contains(uuid)) {
                prefix += rank.getFlair();
            }
        }
        return prefix;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getFlair() {
        return this.flair;
    }
    
    public boolean isDefaultRank() {
        return this.defaultRank;
    }
    
    public void add(final UUID uuid) {
        this.users.add(uuid.toString());
    }
    
    public void remove(final UUID uuid) {
        this.users.remove(uuid.toString());
    }
    
    public boolean contains(final UUID uuid) {
        return this.users.contains(uuid.toString());
    }
    
    public List<String> getPermissions() {
        return this.permissions;
    }
    
    public String getParent() {
        return this.parent;
    }
    
    public void addPermission(final String permission) {
        this.permissions.add(permission);
    }
    
    public void removePermission(final String permission) {
        this.permissions.remove(permission);
    }
    
    public static Rank getRank(final UUID uuid) {
        for (final Rank rank : Rank.ranks) {
            if (rank.contains(uuid)) {
                return rank;
            }
        }
        return null;
    }
    
    public List<String> getDisabledPermissions() {
        return this.disabledPermissions;
    }
    
    static {
        Rank.ranks = new ArrayList<Rank>();
    }
}
