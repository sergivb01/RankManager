package me.sergivb01.rankmanager.ranksync;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;
import java.util.logging.Level;

import me.sergivb01.rankmanager.RankManager;
import org.bukkit.OfflinePlayer;

public class SQLManager {
    private String user;
    private String database;
    private String password;
    private String port;
    private String hostname;
    private Connection connection = null;

    public SQLManager(String hostname, String port, String database, String username, String password) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
    }

    public boolean setupDatabase() {
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            return false;
        }
        try {
            this.updateSQL("CREATE TABLE IF NOT EXISTS ranks  (username VARCHAR(16),  uuid VARCHAR(36),  rank VARCHAR(20), flag VARCHAR(2),  UNIQUE KEY (uuid))");
            this.updateSQL("\t\nCREATE TABLE IF NOT EXISTS `ranks` (\n `username` varchar(16) DEFAULT NULL,\n `uuid` varchar(36) DEFAULT NULL,\n `rank` varchar(20) DEFAULT NULL,\n UNIQUE KEY `uuid` (`uuid`)\n) ENGINE=InnoDB DEFAULT CHARSET=latin1");

            this.updateSQL("CREATE TABLE IF NOT EXISTS `stats` (`username` varchar(255),`kills` varchar(255),`deaths` varchar(255))");
            return true;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return true;
    }



    public boolean setPlayerRank(OfflinePlayer player, String rank) {
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            return false;
        }
        try {
            if (!rank.isEmpty()) {
                this.updateSQL("DELETE FROM ranks WHERE uuid='" + player.getUniqueId() + "'");
                this.updateSQL(String.format("INSERT INTO ranks (username, uuid, rank) VALUES ('%s', '%s', '%s')", player.getName(), player.getUniqueId().toString(), rank));
                return true;
            }
            this.updateSQL(String.format("DELETE FROM ranks WHERE uuid='%s'", player.getUniqueId()));
            return true;
        }
        catch (SQLException ex) {
            if (ex instanceof MySQLIntegrityConstraintViolationException) {
                return true;
            }
            ex.printStackTrace();
        }
        return true;
    }

    public String getPlayerRank(OfflinePlayer player) {
        String rank = "default";
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            return rank;
        }
        ResultSet ret = this.querySQL("SELECT rank FROM ranks WHERE uuid='" + player.getUniqueId() + "'");
        try {
            while (ret.next()) {
                rank = ret.getString(1);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.closeConnection();
        return rank;
    }

    public Connection openConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database, this.user, this.password);
            return this.connection;
        }
        catch (SQLException ex) {
            RankManager.instance.getLogger().log(Level.SEVERE, "Could not connect to MySQL server: " + ex.getMessage());
            return this.connection;
        }
        catch (ClassNotFoundException ex2) {
            RankManager.instance.getLogger().log(Level.SEVERE, "JDBC Driver not found!");
        }
        return this.connection;
    }

    public boolean checkConnection() {
        return this.connection != null;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void closeConnection() {
        if (this.connection == null) return;
        try {
            this.connection.close();
        }
        catch (SQLException ex) {
            RankManager.instance.getLogger().log(Level.SEVERE, "Error closing the MySQL Connection.");
            ex.printStackTrace();
        }
        this.connection = null;
    }

    public ResultSet querySQL(String query) {
        Connection c = this.checkConnection() ? this.getConnection() : this.openConnection();
        Statement s = null;
        try {
            s = c.createStatement();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        ResultSet ret = null;
        try {
            if (s != null) return s.executeQuery(query);
            throw new AssertionError();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public void updateSQL(String update) throws SQLException {
        Connection c = this.checkConnection() ? this.getConnection() : this.openConnection();
        Statement s = c.createStatement();
        s.executeUpdate(update);
        this.closeConnection();
    }

    /*public int[] getPlayerStats(Player player) throws SQLException {
        int kills = 0;
        int deaths = 0;
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            throw new SQLException();
        }
        try {
            ResultSet ret = this.querySQL("SELECT kills, deaths FROM stats WHERE uuid='" + player.getUniqueId() + "'");
            while (ret.next()) {
                kills = ret.getInt(1);
                deaths = ret.getInt(2);
            }
            return new int[]{kills, deaths};
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{kills, deaths};
    }*/

    public void createDeathbanRecord(UUID uuid) throws SQLException {
        Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis() - 1000);
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            throw new SQLException();
        }
        try {
            ResultSet set = this.querySQL("SELECT * FROM `deathbans` WHERE uuid='" + uuid.toString() + "';");
            if (set.next()) return;
            this.updateSQL("INSERT INTO deathbans VALUES('" + uuid.toString() + "', '" + timestamp + "', 0);");
            return;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean canJoin(UUID uuid) throws SQLException {
        Timestamp expiration = null;
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            throw new SQLException();
        }
        try {
            ResultSet set = this.querySQL("SELECT expiration FROM deathbans WHERE uuid='" + uuid.toString() + "';");
            while (set.next()) {
                expiration = set.getTimestamp(1);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        Timestamp now = new Timestamp(Calendar.getInstance().getTimeInMillis());
        return expiration.before(now);
    }

    public Timestamp getDeathbanExpiration(UUID uuid) throws SQLException {
        Timestamp expiration = null;
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            throw new SQLException();
        }
        try {
            ResultSet set = this.querySQL("SELECT expiration FROM deathbans WHERE uuid='" + uuid.toString() + "';");
            if (set == null) return null;
            while (set.next()) {
                expiration = set.getTimestamp(1);
            }
            return expiration;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return expiration;
    }

    public void setDeathban(UUID uuid, Timestamp timestamp) throws SQLException {
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            throw new SQLException();
        }
        try {
            this.updateSQL("UPDATE deathbans SET expiration='" + timestamp + "' WHERE uuid='" + uuid.toString() + "';");
            return;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getLives(UUID uuid) throws SQLException {
        int lives = 0;
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            throw new SQLException();
        }
        try {
            ResultSet set = this.querySQL("SELECT lives FROM deathbans WHERe uuid='" + uuid.toString() + "';");
            while (set.next()) {
                lives = set.getInt(1);
            }
            return lives;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return lives;
    }

    public void removeLives(UUID uuid, int lives) throws SQLException {
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            throw new SQLException();
        }
        try {
            if (lives == 0) return;
            this.updateSQL("UPDATE deathbans SET lives=lives-" + lives + " WHERE uuid='" + uuid.toString() + "';");
            return;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addLives(UUID uuid, int lives) throws SQLException {
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            throw new SQLException();
        }
        try {
            if (lives == 0) return;
            this.updateSQL("UPDATE deathbans SET lives=lives+" + lives + " WHERE uuid='" + uuid.toString() + "';");
            return;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setLives(UUID uuid, int lives) throws SQLException {
        if (!this.checkConnection()) {
            this.openConnection();
        }
        if (!this.checkConnection()) {
            throw new SQLException();
        }
        try {
            if (lives == 0) return;
            this.updateSQL("UPDATE deathbans SET lives='" + lives + "' WHERE uuid='" + uuid + "';");
            return;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

