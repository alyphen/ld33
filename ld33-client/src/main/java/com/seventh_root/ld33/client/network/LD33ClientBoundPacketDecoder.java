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
import com.seventh_root.ld33.common.player.Player;
import com.seventh_root.ld33.common.world.Dragon;
import com.seventh_root.ld33.common.world.Unit;
import com.seventh_root.ld33.common.world.Wall;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.UUID;

public class LD33ClientBoundPacketDecoder extends ByteToMessageDecoder {

    private LD33Client client;

    public LD33ClientBoundPacketDecoder(LD33Client client) {
        this.client = client;
    }

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
                    String joiningPlayerUUID = readString(in);
                    String joiningPlayerName = readString(in);
                    out.add(new PlayerJoinClientBoundPacket(UUID.fromString(joiningPlayerUUID), joiningPlayerName));
                    break;
                case 3:
                    String quittingPlayerUUID = readString(in);
                    String quittingPlayerName = readString(in);
                    out.add(new PlayerQuitClientBoundPacket(UUID.fromString(quittingPlayerUUID), quittingPlayerName));
                    break;
                case 4:
                    String loginResponseMessage = readString(in);
                    boolean success = in.readBoolean();
                    out.add(new PlayerLoginResponseClientBoundPacket(loginResponseMessage, success));
                    break;
                case 5:
                    String unitUUID = readString(in);
                    String playerUUID = readString(in);
                    int x = in.readInt();
                    int y = in.readInt();
                    String type = readString(in);
                    Unit unit;
                    switch (type) {
                        case "wall":
                            unit = new Wall(UUID.fromString(unitUUID), Player.getByUUID(null, UUID.fromString(playerUUID)), client.getWorldPanel().getWorld().getTileAt(x, y));
                            break;
                        case "dragon":
                            unit = new Dragon(UUID.fromString(unitUUID), Player.getByUUID(null, UUID.fromString(playerUUID)), client.getWorldPanel().getWorld().getTileAt(x, y));
                            break;
                        default:
                            unit = null;
                            break;
                    }
                    out.add(new UnitSpawnClientBoundPacket(unit));
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
