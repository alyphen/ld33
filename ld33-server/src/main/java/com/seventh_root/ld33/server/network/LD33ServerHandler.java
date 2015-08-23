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
import com.seventh_root.ld33.common.world.Dragon;
import com.seventh_root.ld33.common.world.Tile;
import com.seventh_root.ld33.common.world.Unit;
import com.seventh_root.ld33.server.LD33Server;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.sql.SQLException;
import java.util.Random;

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
        System.out.println(msg.getClass().getName());
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
                    channels.writeAndFlush(new PlayerJoinClientBoundPacket(player.getUUID(), player.getName()));
                    Random random = new Random();
                    int startX = random.nextInt(server.getWorld().getWidth());
                    int startY = random.nextInt(server.getWorld().getHeight());
                    Tile startTile = server.getWorld().getTileAt(startX, startY);
                    while (startTile.getUnit() != null) {
                        startX = random.nextInt(server.getWorld().getWidth());
                        startY = random.nextInt(server.getWorld().getHeight());
                        startTile = server.getWorld().getTileAt(startX, startY);
                    }
                    startTile.setUnit(new Dragon(server.getDatabaseConnection(), player, startTile));
                    sendUnits(ctx);
                } else {
                    ctx.writeAndFlush(new PlayerLoginResponseClientBoundPacket("Sign up unsuccessful: that username is already in use", false));
                }
            } else {
                Player player = Player.getByName(server.getDatabaseConnection(), packet.getPlayerName());
                if (player != null) {
                    if (player.checkPassword(server.getEncryptionManager().decrypt(packet.getEncryptedPassword()))) {
                        ctx.channel().attr(PLAYER).set(player);
                        ctx.writeAndFlush(new PlayerLoginResponseClientBoundPacket("Login successful. Entering the game world...", true));
                        channels.writeAndFlush(new PlayerJoinClientBoundPacket(player.getUUID(), player.getName()));
                        sendUnits(ctx);
                    } else {
                        ctx.writeAndFlush(new PlayerLoginResponseClientBoundPacket("Login unsuccessful: incorrect credentials", false));
                    }
                } else {
                    ctx.writeAndFlush(new PlayerLoginResponseClientBoundPacket("Login unsuccessful: incorrect credentials", false));
                }
            }
        //} else if (msg instanceof PlayerJoinServerBoundPacket) {
            // Not totally sure whether I'll use this one
        } else if (msg instanceof PlayerQuitServerBoundPacket) {
            Player player = ctx.channel().attr(PLAYER).get();
            channels.stream().filter(channel -> channel != ctx.channel()).forEach(channel -> channel.writeAndFlush(new PlayerQuitClientBoundPacket(player.getUUID(), player.getName())));
            ctx.close();
        } else if (msg instanceof UnitSpawnServerBoundPacket) {
            UnitSpawnServerBoundPacket packet = (UnitSpawnServerBoundPacket) msg;
            Unit unit = packet.getUnit(server.getWorld());
            unit.getTile().setUnit(unit);
        } else if (msg instanceof UnitMoveServerBoundPacket) {
            UnitMoveServerBoundPacket packet = (UnitMoveServerBoundPacket) msg;
            Unit unit = Unit.getByUUID(server.getDatabaseConnection(), server.getWorld(), packet.getUnitUUID());
            if (unit != null) {
                unit.moveTo(server.getWorld().getTileAt(packet.getTargetX(), packet.getTargetY()));
                channels.writeAndFlush(new UnitMoveClientBoundPacket(unit, unit.getTile().getX(), unit.getTile().getY(), packet.getTargetX(), packet.getTargetY()));
            }
        }
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
