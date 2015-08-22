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
        } else if (msg instanceof PlayerLoginClientBoundPacket) {
            // Not sure whether I'll use this one
        } else if (msg instanceof PlayerJoinClientBoundPacket) {
            //TODO
        } else if (msg instanceof PlayerQuitClientBoundPacket) {
            //TODO
        } else if (msg instanceof PlayerLoginResponseClientBoundPacket) {
            PlayerLoginResponseClientBoundPacket packet = (PlayerLoginResponseClientBoundPacket) msg;
            client.getLoginPanel().setStatusMessage(packet.getMessage());
            client.getLoginPanel().reEnableLoginButtons();
            if (packet.isSuccess()) {
                client.showPanel("world");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }

}
