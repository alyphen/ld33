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

package com.seventh_root.ld33.client;

import com.seventh_root.ld33.client.network.LD33ClientBoundPacketDecoder;
import com.seventh_root.ld33.client.network.LD33ClientHandler;
import com.seventh_root.ld33.client.network.LD33ServerBoundPacketEncoder;
import com.seventh_root.ld33.client.panel.ConnectionPanel;
import com.seventh_root.ld33.client.panel.LoginPanel;
import com.seventh_root.ld33.client.panel.WorldPanel;
import com.seventh_root.ld33.common.encrypt.EncryptionManager;
import com.seventh_root.ld33.common.network.packet.serverbound.ServerBoundPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;
import static java.util.logging.Level.SEVERE;

public class LD33Client extends JPanel {

    private static final long DELAY = 25L;

    private LD33ClientFrame frame;

    private Logger logger;

    private EncryptionManager encryptionManager;

    private ConnectionPanel connectionPanel;
    private LoginPanel loginPanel;
    private WorldPanel worldPanel;

    private Channel channel;

    private boolean running;

    private byte[] serverPublicKey;

    private String playerName;

    public LD33Client(LD33ClientFrame frame) {
        this.frame = frame;

        logger = Logger.getLogger(getClass().getCanonicalName());

        encryptionManager = new EncryptionManager();

        setLayout(new CardLayout());
        connectionPanel = new ConnectionPanel(this);
        add(connectionPanel, "connect");
        loginPanel = new LoginPanel(this);
        add(loginPanel, "login");
        worldPanel = new WorldPanel(this);
        add(worldPanel, "world");
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void connect(String address, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new LD33ServerBoundPacketEncoder(),
                                    new LD33ClientBoundPacketDecoder(),
                                    new LD33ClientHandler(LD33Client.this)
                            );
                        }
                    });
            channel = bootstrap.connect(address, port).sync().channel();
            start();
            channel.closeFuture().sync();
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    private void start() {
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

    public Logger getLogger() {
        return logger;
    }

    public EncryptionManager getEncryptionManager() {
        return encryptionManager;
    }

    public void sendPacket(ServerBoundPacket packet) {
        channel.writeAndFlush(packet);
    }

    public byte[] getServerPublicKey() {
        return serverPublicKey;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}