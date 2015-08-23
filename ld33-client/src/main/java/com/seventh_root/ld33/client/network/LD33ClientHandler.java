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

package com.seventh_root.ld33.client.network;

import com.seventh_root.ld33.client.LD33Client;
import com.seventh_root.ld33.common.network.packet.clientbound.*;
import com.seventh_root.ld33.common.network.packet.serverbound.PublicKeyServerBoundPacket;
import com.seventh_root.ld33.common.player.Player;
import com.seventh_root.ld33.common.world.Dragon;
import com.seventh_root.ld33.common.world.Unit;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import static javax.swing.JOptionPane.showMessageDialog;

public class LD33ClientHandler extends ChannelHandlerAdapter {

    private final LD33Client client;

    public LD33ClientHandler(LD33Client client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        showMessageDialog(null, "Disconnected from the server");
        System.exit(0);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg.getClass().getName());
        if (msg instanceof PublicKeyClientBoundPacket) {
            ctx.writeAndFlush(new PublicKeyServerBoundPacket(client.getEncryptionManager().getKeyPair().getPublic().getEncoded()));
            PublicKeyClientBoundPacket packet = (PublicKeyClientBoundPacket) msg;
            client.setServerPublicKey(packet.getEncodedPublicKey());
            client.showPanel("login");
        //} else if (msg instanceof PlayerLoginClientBoundPacket) {
            // Not sure whether I'll use this one
        } else if (msg instanceof PlayerJoinClientBoundPacket) {
            PlayerJoinClientBoundPacket packet = (PlayerJoinClientBoundPacket) msg;
            Player player = new Player(packet.getPlayerUUID(), packet.getPlayerName());
            Player.cachePlayer(player);
            if (packet.getPlayerName().equals(client.getPlayerName())) {
                client.setPlayer(player);
            }
        } else if (msg instanceof PlayerQuitClientBoundPacket) {
            PlayerQuitClientBoundPacket packet = (PlayerQuitClientBoundPacket) msg;
            Player.uncachePlayer(Player.getByUUID(null, packet.getPlayerUUID()));
        } else if (msg instanceof PlayerLoginResponseClientBoundPacket) {
            PlayerLoginResponseClientBoundPacket packet = (PlayerLoginResponseClientBoundPacket) msg;
            client.getLoginPanel().setStatusMessage(packet.getMessage());
            client.getLoginPanel().reEnableLoginButtons();
            if (packet.isSuccess()) {
                client.showPanel("game");
            }
        } else if (msg instanceof UnitSpawnClientBoundPacket) {
            UnitSpawnClientBoundPacket packet = (UnitSpawnClientBoundPacket) msg;
            Unit unit = packet.getUnit(client.getWorldPanel().getWorld());
            unit.getTile().setUnit(unit);
            if (unit instanceof Dragon) {
                Dragon dragon = (Dragon) unit;
                if (dragon.getPlayer().getUUID().toString().equals(client.getPlayer().getUUID().toString())) {
                    client.getWorldPanel().setCameraFocus(dragon);
                    client.getWorldPanel().setSelectedUnit(dragon);
                }
            }
            Unit.cacheUnit(unit);
        } else if (msg instanceof UnitMoveClientBoundPacket) {
            UnitMoveClientBoundPacket packet = (UnitMoveClientBoundPacket) msg;
            Unit unit = Unit.getByUUID(null, client.getWorldPanel().getWorld(), packet.getUnitUUID());
            if (unit != null) {
                unit.setTile(client.getWorldPanel().getWorld().getTileAt(packet.getX(), packet.getY()));
                unit.moveTo(client.getWorldPanel().getWorld().getTileAt(packet.getTargetX(), packet.getTargetY()));
            }
        } else if (msg instanceof ChatMessageClientBoundPacket) {
            ChatMessageClientBoundPacket packet = (ChatMessageClientBoundPacket) msg;
            client.getChatPanel().append(packet.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }

}
