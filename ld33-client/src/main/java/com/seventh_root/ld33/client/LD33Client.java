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
import com.seventh_root.ld33.client.panel.*;
import com.seventh_root.ld33.client.texture.TextureManager;
import com.seventh_root.ld33.common.economy.EconomyManager;
import com.seventh_root.ld33.common.encrypt.EncryptionManager;
import com.seventh_root.ld33.common.network.packet.serverbound.ServerBoundPacket;
import com.seventh_root.ld33.common.player.Player;
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
    private TextureManager textureManager;
    private EconomyManager economyManager;

    private ConnectionPanel connectionPanel;
    private LoginPanel loginPanel;
    private WorldPanel worldPanel;
    private ChatPanel chatPanel;
    private ShopPanel shopPanel;
    private GamePanel gamePanel;

    private Channel channel;

    private boolean running;

    private byte[] serverPublicKey;

    private String playerName;
    private Player player;

    public LD33Client(LD33ClientFrame frame) {
        this.frame = frame;

        logger = Logger.getLogger(getClass().getCanonicalName());

        encryptionManager = new EncryptionManager();
        textureManager = new TextureManager(this);
        economyManager = new EconomyManager();

        setLayout(new CardLayout());
        connectionPanel = new ConnectionPanel(this);
        add(connectionPanel, "connect");
        loginPanel = new LoginPanel(this);
        add(loginPanel, "login");
        worldPanel = new WorldPanel(this);
        chatPanel = new ChatPanel(this);
        shopPanel = new ShopPanel(this);
        gamePanel = new GamePanel(this);
        add(gamePanel, "game");
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
                                    new LD33ClientBoundPacketDecoder(LD33Client.this),
                                    new LD33ClientHandler(LD33Client.this)
                            );
                        }
                    });
            channel = bootstrap.connect(address, port).sync().channel();
            start();
            channel.closeFuture().sync();
        } catch (InterruptedException exception) {
            getLogger().log(SEVERE, "Event loop group interrupted", exception);
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
        getWorldPanel().onTick();
    }

    public Logger getLogger() {
        return logger;
    }

    public EncryptionManager getEncryptionManager() {
        return encryptionManager;
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ConnectionPanel getConnectionPanel() {
        return connectionPanel;
    }

    public LoginPanel getLoginPanel() {
        return loginPanel;
    }

    public WorldPanel getWorldPanel() {
        return worldPanel;
    }

    public ChatPanel getChatPanel() {
        return chatPanel;
    }

    public ShopPanel getShopPanel() {
        return shopPanel;
    }

    public void showPanel(String panel) {
        ((CardLayout) getLayout()).show(this, panel);
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

    public void setServerPublicKey(byte[] serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

}
