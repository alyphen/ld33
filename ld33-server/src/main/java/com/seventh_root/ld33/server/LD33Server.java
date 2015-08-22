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

import com.seventh_root.ld33.common.encrypt.EncryptionManager;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;
import static java.util.logging.Level.SEVERE;

public class LD33Server {

    private LD33ServerHandler handler;
    private Config config;
    private Connection databaseConnection;
    private Logger logger;
    private EncryptionManager encryptionManager;
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
                                    new LD33ServerBoundPacketDecoder(),
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
            exception.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
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
            config = Config.load(configFile);
        } catch (FileNotFoundException exception) {
            getLogger().log(SEVERE, "Failed to find configuration", exception);
        }
    }

    public void saveDefaultConfig(File configFile) throws IOException {
        if (!configFile.exists()) {
            Config defaultConfig = new Config();
            defaultConfig.set("port", 37896);
            defaultConfig.set("database.url", "localhost");
            defaultConfig.set("database.database", "ld33");
            defaultConfig.set("database.user", "ld33");
            defaultConfig.set("database.password", "secret");
            defaultConfig.save(configFile);
        }
    }

    public void saveDefaultConfig() throws IOException {
        saveDefaultConfig(new File("./config.json"));
    }

}
