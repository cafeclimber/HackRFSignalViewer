package com.example.cafeclimber.hackrfsignalviewer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SqlInterface {

    private Connection connection = null;

    public SqlInterface() {}

    private static Connection getConnection(String dbURL, String user, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException");
        }

        Properties properties = new Properties();
        properties.put("user", user);
        properties.put("password", password);
        properties.put("autoReconnect", "true");
        try {
            return DriverManager.getConnection(dbURL, properties);
        }
        catch (SQLException e) {
            System.out.println("SQLException");
        }
        return null;
    }

    public boolean connectToDB(String hostAddress) {
        connection = getConnection("jdbc:mysql://" + hostAddress + "HRG_RF_Fields/",
                    "hrg_user",
                    "hrg");
        return (connection != null);
    }

    public boolean disconnectFromDB() {
        if (connection != null) {
            try {
                connection.close();
            }
            catch (SQLException e) {
                System.out.println("SQLException");
                return false;
            }
        }
        return true;
    }
}
