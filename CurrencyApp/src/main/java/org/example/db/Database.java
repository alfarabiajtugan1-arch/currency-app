package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    // !!! ЗДЕСЬ ДОЛЖЕН БЫТЬ ПОЛНЫЙ ПУТЬ К ТОМУ mydb.db,
    // КОТОРЫЙ ТЫ ОТКРЫВАЕШЬ В DBeaver !!!
    // примеры:
    // private static final String DB_PATH = "C:/Users/Ал-Фараби/OneDrive/Документы/NetBeansProjects/CurrencyApp/mydb.db";
    // или тот, что реально у тебя в DBeaver в свойствах подключения
    private static final String DB_PATH =
            "C:/Users/Ал-Фараби/OneDrive/Документы/NetBeansProjects/CurrencyApp/mydb.db";

    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("[Database] Использую БД: " + DB_PATH);
            initSchema();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private static void initSchema() {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            // USERS
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    name     TEXT    NOT NULL,
                    email    TEXT    NOT NULL UNIQUE,
                    password TEXT    NOT NULL
                )
                """);

            // DEMO-АККАУНТЫ
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS demo_accounts (
                    user_id   INTEGER PRIMARY KEY,
                    balances  TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """);

            // ТРАНЗАКЦИИ
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id       INTEGER      NOT NULL,
                    from_currency TEXT         NOT NULL,
                    to_currency   TEXT         NOT NULL,
                    rate_date     TEXT         NOT NULL,
                    amount        REAL         NOT NULL,
                    rate          REAL         NOT NULL,
                    result        REAL         NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """);

            System.out.println("[Database] Схема БД инициализирована");
        } catch (Exception e) {
            System.err.println("Ошибка инициализации схемы БД: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
