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
import com.seventh_root.ld33.common.world.Flag;
import com.seventh_root.ld33.common.world.Unit;
import com.seventh_root.ld33.common.world.Wall;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LD33ClientBoundPacketDecoder extends ByteToMessageDecoder {

    private LD33Client client;

    public LD33ClientBoundPacketDecoder(LD33Client client) {
        this.client = client;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] output = new byte[in.readableBytes()];
        in.readBytes(output);
        System.out.println(Arrays.toString(output));
        in.resetReaderIndex();
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
                    int joiningPlayerResources = in.readInt();
                    out.add(new PlayerJoinClientBoundPacket(UUID.fromString(joiningPlayerUUID), joiningPlayerName, joiningPlayerResources));
                    break;
                case 3:
                    String quittingPlayerUUID = readString(in);
                    String quittingPlayerName = readString(in);
                    out.add(new PlayerQuitClientBoundPacket(UUID.fromString(quittingPlayerUUID), quittingPlayerName));
                    break;
                case 4:
                    String loginResponseMessage = readString(in);
                    boolean loginSuccess = in.readBoolean();
                    out.add(new PlayerLoginResponseClientBoundPacket(loginResponseMessage, loginSuccess));
                    break;
                case 5:
                    String spawningUnitUUID = readString(in);
                    String spawningUnitPlayerUUID = readString(in);
                    int spawningUnitX = in.readInt();
                    int spawningUnitY = in.readInt();
                    String type = readString(in);
                    long spawningUnitCompletionTime = in.readLong();
                    Unit unit;
                    switch (type) {
                        case "wall":
                            unit = new Wall(UUID.fromString(spawningUnitUUID), Player.getByUUID(null, UUID.fromString(spawningUnitPlayerUUID)), client.getWorldPanel().getWorld().getTileAt(spawningUnitX, spawningUnitY), spawningUnitCompletionTime);
                            break;
                        case "dragon":
                            unit = new Dragon(UUID.fromString(spawningUnitUUID), Player.getByUUID(null, UUID.fromString(spawningUnitPlayerUUID)), client.getWorldPanel().getWorld().getTileAt(spawningUnitX, spawningUnitY), spawningUnitCompletionTime);
                            break;
                        case "flag":
                            unit = new Flag(UUID.fromString(spawningUnitUUID), Player.getByUUID(null, UUID.fromString(spawningUnitPlayerUUID)), client.getWorldPanel().getWorld().getTileAt(spawningUnitX, spawningUnitY), spawningUnitCompletionTime);
                            break;
                        default:
                            unit = null;
                            break;
                    }
                    out.add(new UnitSpawnClientBoundPacket(unit));
                    break;
                case 6:
                    String movingUnitUUID = readString(in);
                    int movingUnitX = in.readInt();
                    int movingUnitY = in.readInt();
                    int movingUnitTargetX = in.readInt();
                    int movingUnitTargetY = in.readInt();
                    out.add(new UnitMoveClientBoundPacket(Unit.getByUUID(null, client.getWorldPanel().getWorld(), UUID.fromString(movingUnitUUID)), movingUnitX, movingUnitY, movingUnitTargetX, movingUnitTargetY));
                    break;
                case 7:
                    String chatMessage = readString(in);
                    out.add(new ChatMessageClientBoundPacket(chatMessage));
                    break;
                case 8:
                    String purchasingPlayerUUID = readString(in);
                    int purchasedUnitX = in.readInt();
                    int purchasedUnitY = in.readInt();
                    String purchasedUnitType = readString(in);
                    out.add(new UnitPurchaseClientBoundPacket(UUID.fromString(purchasingPlayerUUID), purchasedUnitX, purchasedUnitY, purchasedUnitType));
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
