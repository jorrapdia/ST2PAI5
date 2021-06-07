package com.example.myapplication;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static Config instance;
    private String server;
    private Integer port;
    private Boolean test;


    private Config(AssetManager am) {
        Properties properties = new Properties();
        try (InputStream is = am.open("config.properties")) {
            properties.load(is);
            server = properties.getProperty("server");
            port = properties.getProperty("port") == null? 8443 : Integer.parseInt(properties.getProperty("port"));
            test = Boolean.parseBoolean(properties.getProperty("test"));
        } catch (IOException e) {
            System.exit(1);
        }

    }

    public static Config getInstance(AssetManager am) {
        if (instance == null) {
            instance = new Config(am);
        }
        return instance;
    }

    public String getServer() {
        return server;
    }

    public Integer getPort() {
        return port;
    }

    public Boolean isTest() {
        return test;
    }
}
