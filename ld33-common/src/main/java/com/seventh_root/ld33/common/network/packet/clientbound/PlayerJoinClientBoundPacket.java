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

package com.seventh_root.ld33.common.network.packet.clientbound;

import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class PlayerJoinClientBoundPacket extends ClientBoundPacket {

    private String playerUUID;
    private String playerName;
    private int playerResources;

    public PlayerJoinClientBoundPacket(UUID playerUUID, String playerName, int playerResources) {
        this.playerUUID = playerUUID.toString();
        this.playerName = playerName;
        this.playerResources = playerResources;
    }

    @Override
    public int getId() {
        return 2;
    }

    public UUID getPlayerUUID() {
        return UUID.fromString(playerUUID);
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPlayerResources() {
        return playerResources;
    }

    @Override
    public void write(ByteBuf buf) throws UnsupportedEncodingException {
        super.write(buf);
        writeString(buf, getPlayerUUID().toString());
        writeString(buf, getPlayerName());
        buf.writeInt(getPlayerResources());
    }
}
