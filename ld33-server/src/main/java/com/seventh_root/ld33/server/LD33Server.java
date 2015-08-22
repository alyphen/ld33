/*
 * Copyright 2015 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seventh_root.ld33.server;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;
import static java.util.logging.Level.SEVERE;

public class LD33Server {

    private Map<String, Object> config;
    private Logger logger;
    private boolean running;
    private static final long DELAY = 25L;

    public static void main(String[] args) {
        new LD33Server().start();
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public LD33Server() {
        loadConfig();
        logger = Logger.getLogger(getClass().getCanonicalName());
    }

    public void start() {
        setRunning(true);
        long beforeTime, timeDiff, sleep;
        beforeTime = currentTimeMillis();
        while (isRunning()) {
            doTick();
            timeDiff = currentTimeMillis() - beforeTime;
            sleep = DELAY - timeDiff;
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException exception) {
                    getLogger().log(SEVERE, "Thread interrupted", exception);
                }
            }
            beforeTime = currentTimeMillis();
        }
    }

    private void doTick() {

    }

    public void loadConfig() {
        File configFile = new File("./config.json");
        try {
            saveDefaultConfig(configFile);
        } catch (IOException exception) {
            getLogger().log(SEVERE, "Failed to create default config", exception);
        }
        try {
            Reader reader = new FileReader(configFile);
            Gson gson = new Gson();
            config = gson.fromJson(reader, new TypeToken<HashMap<String, Object>>() {
            }.getType());
        } catch (FileNotFoundException exception) {
            getLogger().log(SEVERE, "Failed to find configuration", exception);
        }
    }

    public void saveDefaultConfig(File configFile) throws IOException {
        if (!configFile.getParentFile().isDirectory()) {
            if (!configFile.getParentFile().delete()) {
                throw new IOException("Failed to remove " + configFile.getParentFile().getPath() + ": do you have permission?");
            }
        }
        if (!configFile.getParentFile().exists()) {
            if (!configFile.getParentFile().mkdirs()) {
                throw new IOException("Failed to create directory " + configFile.getParentFile().getPath() + ": do you have permission?");
            }
        }
        if (!configFile.exists()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Map<String, Object> defaultConfig = new HashMap<>();
            defaultConfig.put("port", "37896");
            gson.toJson(defaultConfig, new FileWriter(configFile));
        }
    }

    public void saveDefaultConfig() throws IOException {
        saveDefaultConfig(new File("./config.json"));
    }

    public void saveConfig(File configFile) throws IOException {
        if (!configFile.getParentFile().isDirectory()) {
            if (!configFile.getParentFile().delete()) {
                throw new IOException("Failed to remove " + configFile.getParentFile().getPath() + ": do you have permission?");
            }
        }
        if (!configFile.getParentFile().exists()) {
            if (!configFile.getParentFile().mkdirs()) {
                throw new IOException("Failed to create directory " + configFile.getParentFile().getPath() + ": do you have permission?");
            }
        }
        if (!configFile.exists()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(config, new FileWriter(configFile));
        }
    }

}
