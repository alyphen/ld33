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

import com.seventh_root.ld33.common.network.packet.serverbound.*;
import com.seventh_root.ld33.common.player.Player;
import com.seventh_root.ld33.common.world.Dragon;
import com.seventh_root.ld33.common.world.Unit;
import com.seventh_root.ld33.common.world.Wall;
import com.seventh_root.ld33.server.LD33Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.UUID;

public class LD33ServerBoundPacketDecoder extends ByteToMessageDecoder {

    private LD33Server server;

    public LD33ServerBoundPacketDecoder(LD33Server server) {
        this.server = server;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.isReadable()) {
            int id = in.readInt();
            switch (id) {
                case 0:
                    byte[] encodedPublicKey = new byte[in.readInt()];
                    in.readBytes(encodedPublicKey);
                    out.add(new PublicKeyServerBoundPacket(encodedPublicKey));
                    break;
                case 1:
                    String loggingInPlayerName = readString(in);
                    byte[] encryptedPassword = new byte[in.readInt()];
                    in.readBytes(encryptedPassword);
                    boolean signUp = in.readBoolean();
                    out.add(new PlayerLoginServerBoundPacket(loggingInPlayerName, encryptedPassword, signUp));
                    break;
                case 2:
                    out.add(new PlayerJoinServerBoundPacket());
                    break;
                case 3:
                    out.add(new PlayerQuitServerBoundPacket());
                    break;
                case 4:
                    String loginResponseMessage = readString(in);
                    boolean loginSuccess = in.readBoolean();
                    out.add(new PlayerLoginResponseServerBoundPacket(loginResponseMessage, loginSuccess));
                    break;
                case 5:
                    String spawningUnitUUID = readString(in);
                    String spawningUnitPlayerUUID = readString(in);
                    int spawningUnitX = in.readInt();
                    int spawningUnitY = in.readInt();
                    String spawningUnitType = readString(in);
                    Unit spawningUnit;
                    switch (spawningUnitType) {
                        case "wall":
                            spawningUnit = new Wall(UUID.fromString(spawningUnitUUID), Player.getByUUID(null, UUID.fromString(spawningUnitPlayerUUID)), server.getWorld().getTileAt(spawningUnitX, spawningUnitY));
                            break;
                        case "dragon":
                            spawningUnit = new Dragon(UUID.fromString(spawningUnitUUID), Player.getByUUID(null, UUID.fromString(spawningUnitPlayerUUID)), server.getWorld().getTileAt(spawningUnitX, spawningUnitY));
                            break;
                        default:
                            spawningUnit = null;
                            break;
                    }
                    out.add(new UnitSpawnServerBoundPacket(spawningUnit));
                    break;
                case 6:
                    String movingUnitUUID = readString(in);
                    int movingUnitTargetX = in.readInt();
                    int movingUnitTargetY = in.readInt();
                    out.add(new UnitMoveServerBoundPacket(Unit.getByUUID(server.getDatabaseConnection(), server.getWorld(), UUID.fromString(movingUnitUUID)), movingUnitTargetX, movingUnitTargetY));
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
