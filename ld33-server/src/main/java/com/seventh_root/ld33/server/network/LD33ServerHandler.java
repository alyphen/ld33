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

import com.seventh_root.ld33.common.network.packet.serverbound.PublicKeyServerBoundPacket;
import com.seventh_root.ld33.server.LD33Server;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

@Sharable
public class LD33ServerHandler extends ChannelHandlerAdapter {

    private LD33Server server;

    private ChannelGroup channels;

    private final AttributeKey<byte[]> publicKey;

    public LD33ServerHandler(LD33Server server) {
        this.server = server;
        this.channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        publicKey = AttributeKey.valueOf("publicKey");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof PublicKeyServerBoundPacket) {
            PublicKeyServerBoundPacket packet = (PublicKeyServerBoundPacket) msg;
            ctx.channel().attr(publicKey).set(packet.getEncodedPublicKey());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }

}
