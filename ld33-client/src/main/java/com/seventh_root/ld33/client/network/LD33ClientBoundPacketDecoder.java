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

import com.seventh_root.ld33.common.network.packet.clientbound.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class LD33ClientBoundPacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.isReadable()) {
            int id = in.readInt();
            switch (id) {
                case 0:
                    byte[] encodedPublicKey = new byte[in.readInt()];
                    in.readBytes(encodedPublicKey);
                    out.add(new PublicKeyClientBoundPacket(encodedPublicKey));
                    break;
                case 1:
                    out.add(new PlayerLoginClientBoundPacket());
                    break;
                case 2:
                    String joiningPlayerName = readString(in);
                    out.add(new PlayerJoinClientBoundPacket(joiningPlayerName));
                    break;
                case 3:
                    String quittingPlayerName = readString(in);
                    out.add(new PlayerQuitClientBoundPacket(quittingPlayerName));
                    break;
                case 4:
                    out.add(new PingClientBoundPacket());
                    break;
            }
        }
    }

    private String readString(ByteBuf buf) {
        String str = "";
        byte b = -1;
        while (buf.readableBytes() > 0 && b != 0) {
            b = buf.readByte();
            if (b != 0) str += (char) b;
        }
        return str;
    }

}
