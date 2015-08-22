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

package com.seventh_root.ld33.server.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Config {

    public static Config load(File file) throws FileNotFoundException {
        Reader reader = new FileReader(file);
        Gson gson = new Gson();
        return new Config(gson.fromJson(reader, new TypeToken<HashMap<String, Object>>() {}.getType()));
    }

    private Map<String, Object> config;

    public Config(Map<String, Object> config) {
        this.config = config;
    }

    public Config() {
        this(new HashMap<>());
    }

    public void set(String key, Object value) {
        config.put(key, value);
    }

    public Config getSection(String key) {
        if (config.containsKey(key) && config.get(key) instanceof Map) {
            return new Config((Map<String, Object>) config.get(key));
        }
        return null;
    }

    public Object get(String key, Object defaultValue) {
        if (key.contains(".")) {
            Config subsection = getSection(key.substring(0, key.indexOf(".")));
            if (subsection != null) {
                return subsection.get(key.substring(key.indexOf(".")), defaultValue);
            } else {
                return defaultValue;
            }
        }
        if (config.containsKey(key)) {
            return config.get(key);
        }
        return defaultValue;
    }

    public Object get(String key) {
        return get(key, null);
    }

    public int getInt(String key, int defaultValue) {
        return (int) ((double) get(key, defaultValue));
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public String getString(String key, String defaultValue) {
        return (String) get(key, defaultValue);
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public void save(File file) throws IOException {
        if (!file.getParentFile().isDirectory()) {
            if (!file.getParentFile().delete()) {
                throw new IOException("Failed to remove " + file.getParentFile().getPath() + ": do you have permission?");
            }
        }
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IOException("Failed to create directory " + file.getParentFile().getPath() + ": do you have permission?");
            }
        }
        if (!file.exists()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(config, new FileWriter(file));
        }
    }

}
