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

public class UnitPurchaseClientBoundPacket extends ClientBoundPacket {

    private String playerUUID;
    private int x;
    private int y;
    private String unitType;

    public UnitPurchaseClientBoundPacket(UUID playerUUID, int x, int y, String unitType) {
        this.playerUUID = playerUUID.toString();
        this.x = x;
        this.y = y;
        this.unitType = unitType;
    }

    @Override
    public int getId() {
        return 8;
    }

    public UUID getPlayerUUID() {
        return UUID.fromString(playerUUID);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getUnitType() {
        return unitType;
    }

    @Override
    public void write(ByteBuf buf) throws UnsupportedEncodingException {
        super.write(buf);
        writeString(buf, getPlayerUUID().toString());
        buf.writeInt(getX());
        buf.writeInt(getY());
        writeString(buf, getUnitType());
    }
}
