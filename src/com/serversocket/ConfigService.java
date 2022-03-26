package com.serversocket;

import java.io.*;
import java.util.Hashtable;

public class ConfigService {
    private String IP;
    private int port;

    private static final String CONFIG_FILE = "config.txt";
    private final String configPath;

    public static String IP_KEY = "IP";
    public static String PORT_KEY = "PORT";

    private final Hashtable<String, String> configSettings;

    public ConfigService() throws Exception {
        this.configPath = ClientServer.SERVER_ROOT + CONFIG_FILE;
        this.configSettings = new Hashtable<>();
        setAllConfigs();
    }

    private void setAllConfigs() throws Exception {
        File file = new File(configPath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        String request;
        do {
            request = bufferedReader.readLine();
        } while (appendToHash(request));

        if (!doesSettingsHaveKey(IP_KEY)) {
            throw new Exception("Config at " + configPath + " doesn't have " + IP_KEY + " key");
        }

        if (!doesSettingsHaveKey(PORT_KEY)) {
            throw new Exception("Config at " + configPath + " doesn't have " + PORT_KEY + " key");
        }

        IP = getSettingsWithKey(IP_KEY);
        port = Integer.parseInt(getSettingsWithKey(PORT_KEY));
    }

    private boolean appendToHash(String line) {
        if (line == null) return false;

        int colonIndex = line.indexOf(":");

        if (colonIndex != -1) {
            configSettings.put(line.substring(0, colonIndex), line.substring(colonIndex + 2));
            return true;
        }
        return false;
    }

    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }

    public String getSettingsWithKey(String key) {
        return configSettings.get(key);
    }

    public boolean doesSettingsHaveKey(String key) {
        return configSettings.containsKey(key);
    }
}
