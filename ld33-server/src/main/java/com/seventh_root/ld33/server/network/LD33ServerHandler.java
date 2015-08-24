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

package com.seventh_root.ld33.server.network;

import com.seventh_root.ld33.common.network.packet.clientbound.*;
import com.seventh_root.ld33.common.network.packet.serverbound.*;
import com.seventh_root.ld33.common.player.Player;
import com.seventh_root.ld33.common.world.*;
import com.seventh_root.ld33.server.LD33Server;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.logging.Level.SEVERE;

@Sharable
public class LD33ServerHandler extends ChannelHandlerAdapter {

    private LD33Server server;

    private ChannelGroup channels;

    private static final AttributeKey<Player> PLAYER = AttributeKey.valueOf("PLAYER");
    private static final AttributeKey<byte[]> PUBLIC_KEY = AttributeKey.valueOf("PUBLIC_KEY");

    public LD33ServerHandler(LD33Server server) {
        this.server = server;
        this.channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
        ctx.writeAndFlush(new PublicKeyClientBoundPacket(server.getEncryptionManager().getKeyPair().getPublic().getEncoded()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof PublicKeyServerBoundPacket) {
            PublicKeyServerBoundPacket packet = (PublicKeyServerBoundPacket) msg;
            ctx.channel().attr(PUBLIC_KEY).set(packet.getEncodedPublicKey());
        } else if (msg instanceof PlayerLoginServerBoundPacket) {
            PlayerLoginServerBoundPacket packet = (PlayerLoginServerBoundPacket) msg;
            if (packet.isSignUp()) {
                if (Player.getByName(server.getDatabaseConnection(), packet.getPlayerName()) == null) {
                    String playerName = packet.getPlayerName();
                    String password = server.getEncryptionManager().decrypt(packet.getEncryptedPassword());
                    Player player = new Player(server.getDatabaseConnection(), playerName, password);
                    ctx.channel().attr(PLAYER).set(player);
                    ctx.writeAndFlush(new PlayerLoginResponseClientBoundPacket("Sign up successful. Entering the game world...", true));
                    channels.writeAndFlush(new PlayerJoinClientBoundPacket(player.getUUID(), player.getName(), player.getResources()));
                    Random random = new Random();
                    int startX = random.nextInt(server.getWorld().getWidth() - 1) + 1;
                    int startY = random.nextInt(server.getWorld().getHeight() - 1) + 1;
                    Tile startTile = server.getWorld().getTileAt(startX, startY);
                    while (startTile.getUnit() != null) {
                        startX = random.nextInt(server.getWorld().getWidth() - 1) + 1;
                        startY = random.nextInt(server.getWorld().getHeight() - 1) + 1;
                        startTile = server.getWorld().getTileAt(startX, startY);
                    }
                    Dragon dragon = new Dragon(server.getDatabaseConnection(), player, startTile, System.currentTimeMillis());
                    startTile.setUnit(dragon);
                    Flag flag = new Flag(server.getDatabaseConnection(), player, startTile.getAdjacent(0, -1), System.currentTimeMillis());
                    startTile.getAdjacent(0, -1).setUnit(flag);
                    sendWorldInfo(ctx);
                    sendUnits(ctx);
                    channels.stream().filter(channel -> channel != ctx.channel()).forEach(channel -> {
                        try {
                            channel.writeAndFlush(new UnitSpawnClientBoundPacket(dragon));
                            channel.writeAndFlush(new UnitSpawnClientBoundPacket(flag));
                        } catch (SQLException exception) {
                            server.getLogger().log(SEVERE, "Failed to send unit spawn packet", exception);
                        }
                    });
                    channels.writeAndFlush(new ChatMessageClientBoundPacket(player.getName() + " joined the game for the first time!"));
                    server.getLogger().info(player.getName() + " signed up from " + ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());
                } else {
                    ctx.writeAndFlush(new PlayerLoginResponseClientBoundPacket("Sign up unsuccessful: that username is already in use", false));
                    server.getLogger().info("A user from " + ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress() + " attempted to sign up with the username " + packet.getPlayerName() + " which was already in use");
                }
            } else {
                Player player = Player.getByName(server.getDatabaseConnection(), packet.getPlayerName());
                if (player != null) {
                    if (player.checkPassword(server.getEncryptionManager().decrypt(packet.getEncryptedPassword()))) {
                        if (channels.stream().filter(channel -> channel.attr(PLAYER).get() != null && channel.attr(PLAYER).get().getUUID().toString().equals(player.getUUID().toString())).collect(Collectors.toList()).size() == 0) {
                            ctx.channel().attr(PLAYER).set(player);
                            ctx.writeAndFlush(new PlayerLoginResponseClientBoundPacket("Login successful. Entering the game world...", true));
                            channels.writeAndFlush(new PlayerJoinClientBoundPacket(player.getUUID(), player.getName(), player.getResources()));
                            sendWorldInfo(ctx);
                            sendUnits(ctx);
                            channels.writeAndFlush(new ChatMessageClientBoundPacket(player.getName() + " joined the game. Welcome back!"));
                            server.getLogger().info(player.getName() + " logged in from " + ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());
                        } else {
                            ctx.writeAndFlush(new PlayerLoginResponseClientBoundPacket("Login unsuccessful: already logged in", false));
                            server.getLogger().info(player.getName() + " attempted to log in from " + ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress() + " but was already logged in");
                        }
                    } else {
                        ctx.writeAndFlush(new PlayerLoginResponseClientBoundPacket("Login unsuccessful: incorrect credentials", false));
                        server.getLogger().info("A user attempted to log in as " + packet.getPlayerName() + " from " + ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress() + " but supplied an incorrect password");
                    }
                } else {
                    ctx.writeAndFlush(new PlayerLoginResponseClientBoundPacket("Login unsuccessful: incorrect credentials", false));
                    server.getLogger().info("A user attempted to log in as " + packet.getPlayerName() + " from " + ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress() + " but supplied an invalid username");
                }
            }
        //} else if (msg instanceof PlayerJoinServerBoundPacket) {
            // Not totally sure whether I'll use this one
        } else if (msg instanceof PlayerQuitServerBoundPacket) {
            Player player = ctx.channel().attr(PLAYER).get();
            channels.stream().filter(channel -> channel != ctx.channel()).forEach(channel -> channel.writeAndFlush(new PlayerQuitClientBoundPacket(player.getUUID(), player.getName())));
            ctx.close();
            channels.writeAndFlush(new ChatMessageClientBoundPacket(ctx.channel().attr(PLAYER).get().getName() + " left the game"));
            server.getLogger().info(ctx.channel().attr(PLAYER).get().getName() + " left the game");
        } else if (msg instanceof UnitSpawnServerBoundPacket) {
            UnitSpawnServerBoundPacket packet = (UnitSpawnServerBoundPacket) msg;
            Unit unit = packet.getUnit(server.getWorld());
            unit.getTile().setUnit(unit);
        } else if (msg instanceof UnitMoveServerBoundPacket) {
            UnitMoveServerBoundPacket packet = (UnitMoveServerBoundPacket) msg;
            Unit unit = Unit.getByUUID(server.getDatabaseConnection(), server.getWorld(), packet.getUnitUUID());
            if (unit != null) {
                Tile tile = server.getWorld().getTileAt(packet.getTargetX(), packet.getTargetY());
                if (tile != null) {
                    unit.moveTo(tile);
                    if (tile.getUnit() != null) {
                        if (tile.getUnit().getPlayerUUID().toString().equals(ctx.channel().attr(PLAYER).get().getUUID().toString())) {
                            ctx.writeAndFlush(new ChatMessageClientBoundPacket("You cannot destroy your own buildings!"));
                        }
                    }
                    channels.writeAndFlush(new UnitMoveClientBoundPacket(unit, unit.getTile().getX(), unit.getTile().getY(), packet.getTargetX(), packet.getTargetY()));
                }
            }
        } else if (msg instanceof ChatMessageServerBoundPacket) {
            ChatMessageServerBoundPacket packet = (ChatMessageServerBoundPacket) msg;
            channels.writeAndFlush(new ChatMessageClientBoundPacket(ctx.channel().attr(PLAYER).get().getName() + ": " + packet.getMessage()));
            server.getLogger().info(ctx.channel().attr(PLAYER).get().getName() + ": " + packet.getMessage());
        } else if (msg instanceof UnitPurchaseServerBoundPacket) {
            UnitPurchaseServerBoundPacket packet = (UnitPurchaseServerBoundPacket) msg;
            Player player = ctx.channel().attr(PLAYER).get();
            int cost = server.getEconomyManager().getResourceCost(packet.getUnitType());
            Tile tile = server.getWorld().getTileAt(packet.getX(), packet.getY());
            if (tile.getUnit() == null) {
                if (player.getResources() >= cost) {
                    player.setResources(player.getResources() - cost);
                    player.update();
                    switch (packet.getUnitType()) {
                        case "wall":
                            Wall wall = new Wall(server.getDatabaseConnection(), player, tile, System.currentTimeMillis() + (server.getEconomyManager().getTimeCost(packet.getUnitType()) * 60000));
                            wall.getTile().setUnit(wall);
                            channels.writeAndFlush(new UnitSpawnClientBoundPacket(wall));
                            break;
                        case "flag":
                            Flag flag = new Flag(server.getDatabaseConnection(), player, tile, System.currentTimeMillis() + (server.getEconomyManager().getTimeCost(packet.getUnitType()) * 60000));
                            flag.getTile().setUnit(flag);
                            channels.writeAndFlush(new UnitSpawnClientBoundPacket(flag));
                            break;
                    }
                } else {
                    ctx.writeAndFlush(new ChatMessageClientBoundPacket("You do not have the resources to build that."));
                }
            } else {
                ctx.writeAndFlush(new ChatMessageClientBoundPacket("You can't build there."));
            }
        } else if (msg instanceof PlayerInformationServerBoundPacket) {
            PlayerInformationServerBoundPacket packet = (PlayerInformationServerBoundPacket) msg;
            ctx.writeAndFlush(new PlayerInformationClientBoundPacket(Player.getByUUID(server.getDatabaseConnection(), packet.getPlayerUUID())));
        }
    }

    private void sendWorldInfo(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new WorldInformationClientBoundPacket(server.getWorld().getWidth(), server.getWorld().getHeight()));
    }

    private void sendUnits(ChannelHandlerContext ctx) {
        for (int x = 0; x < server.getWorld().getWidth(); x++) {
            for (int y = 0; y < server.getWorld().getHeight(); y++) {
                Tile tile = server.getWorld().getTileAt(x, y);
                if (tile != null) {
                    Unit unit = tile.getUnit();
                    if (unit != null) {
                        try {
                            ctx.writeAndFlush(new UnitSpawnClientBoundPacket(unit));
                        } catch (SQLException exception) {
                            server.getLogger().log(SEVERE, "Failed to send unit " + unit.getUUID().toString() + " to " + ctx.attr(PLAYER).get().getName(), exception);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        server.getLogger().log(SEVERE, "A network exception occurred", cause);
    }

}
