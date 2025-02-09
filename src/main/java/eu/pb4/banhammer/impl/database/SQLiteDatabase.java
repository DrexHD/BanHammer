package eu.pb4.banhammer.impl.database;

import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase extends AbstractSQLDatabase {
    public SQLiteDatabase(String database) throws Exception {
        Class.forName("org.sqlite.JDBC");

        conn = DriverManager.getConnection("jdbc:sqlite:" + database);

        stat = conn.createStatement();
        this.createTables();
    }

    @Override
    protected String getTableCreation() {
        return "CREATE TABLE IF NOT EXISTS %s (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "bannedUUID varchar(36), bannedIP varchar(40), bannedName varchar(64), bannedDisplay varchar(512), " +
                "adminUUID varchar(36), adminDisplay TEXT, time BIGINT, duration BIGINT, reason varchar(512))";
    }

    @Override
    protected String getHistoryTableCreation(String prefix) {
        return "CREATE TABLE IF NOT EXISTS " + prefix + "history (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "bannedUUID varchar(36), bannedIP varchar(40), bannedName varchar(64), bannedDisplay varchar(512), " +
                "adminUUID varchar(36), adminDisplay TEXT, time BIGINT, duration BIGINT, reason varchar(512), type varchar(16))";
    }
}
