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

import com.seventh_root.ld33.common.economy.EconomyManager;
import com.seventh_root.ld33.common.encrypt.EncryptionManager;
import com.seventh_root.ld33.common.world.Unit;
import com.seventh_root.ld33.common.world.World;
import com.seventh_root.ld33.server.config.Config;
import com.seventh_root.ld33.server.network.LD33ClientBoundPacketEncoder;
import com.seventh_root.ld33.server.network.LD33ServerBoundPacketDecoder;
import com.seventh_root.ld33.server.network.LD33ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;
import static java.util.logging.Level.SEVERE;

public class LD33Server {

    private LD33ServerHandler handler;
    private Config config;
    private Connection databaseConnection;
    private Logger logger;
    private EncryptionManager encryptionManager;
    private EconomyManager economyManager;
    private World world;
    private boolean running;
    private static final long DELAY = 25L;

    public static void main(String[] args) {
        new Thread(() -> new LD33Server().start()).start();
    }

    public LD33Server() {
        logger = Logger.getLogger(getClass().getCanonicalName());
        loadConfig();
        try {
            databaseConnection = DriverManager.getConnection(
                    "jdbc:mysql://" + getConfig().getMap("database").get("url") + "/" + getConfig().getMap("database").get("database"),
                    (String) getConfig().getMap("database").get("user"),
                    (String) getConfig().getMap("database").get("password")
            );
        } catch (SQLException exception) {
            getLogger().log(SEVERE, "Failed to connect to database", exception);
        }
        encryptionManager = new EncryptionManager();
        economyManager = new EconomyManager();
        world = new World((int) ((double) getConfig().getMap("world").get("width")), (int) ((double) getConfig().getMap("world").get("height")));
        loadUnits();
    }

    public Config getConfig() {
        return config;
    }

    public Connection getDatabaseConnection() {
        return databaseConnection;
    }

    public Logger getLogger() {
        return logger;
    }

    public EncryptionManager getEncryptionManager() {
        return encryptionManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public World getWorld() {
        return world;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    private void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            handler = new LD33ServerHandler(this);
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new LD33ClientBoundPacketEncoder(),
                                    new LD33ServerBoundPacketDecoder(LD33Server.this),
                                    handler
                            );
                        }
                    });
            Channel channel = bootstrap.bind(getConfig().getInt("port", 37896)).sync().channel();
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
            channel.closeFuture().sync();
        } catch (InterruptedException exception) {
            getLogger().log(SEVERE, "Event loop group interrupted", exception);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void doTick() {
        try {
            world.onTick();
        } catch (SQLException exception) {
            getLogger().log(SEVERE, "Failed to update unit in database", exception);
        }
    }

    public void loadConfig() {
        File configFile = new File("./config.json");
        try {
            saveDefaultConfig(configFile);
        } catch (IOException exception) {
            getLogger().log(SEVERE, "Failed to create default config", exception);
        }
        try {
            config = Config.load(configFile);
        } catch (IOException exception) {
            getLogger().log(SEVERE, "Failed to load configuration", exception);
        }
    }

    public void saveDefaultConfig(File configFile) throws IOException {
        if (!configFile.exists()) {
            Config defaultConfig = new Config();
            defaultConfig.set("port", 37896);
            Map<String, Object> databaseSettings = new HashMap<>();
            databaseSettings.put("url", "localhost");
            databaseSettings.put("database", "ld33");
            databaseSettings.put("user", "ld33");
            databaseSettings.put("password", "secret");
            defaultConfig.set("database", databaseSettings);
            Map<String, Object> worldSettings = new HashMap<>();
            worldSettings.put("width", 2000);
            worldSettings.put("height", 2000);
            defaultConfig.set("world", worldSettings);
            defaultConfig.save(configFile);
        }
    }

    public void saveDefaultConfig() throws IOException {
        saveDefaultConfig(new File("./config.json"));
    }

    public void loadUnits() {
        try {
            List<Unit> units = Unit.getAllUnits(getDatabaseConnection(), getWorld());
            units.forEach(unit -> unit.getTile().setUnit(unit));
        } catch (SQLException exception) {
            getLogger().log(SEVERE, "Failed to load units", exception);
        }

    }

}
