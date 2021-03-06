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

import com.seventh_root.ld33.common.world.Unit;
import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class UnitMoveClientBoundPacket extends ClientBoundPacket {

    private String unitUUID;
    private int x;
    private int y;
    private int targetX;
    private int targetY;

    public UnitMoveClientBoundPacket(Unit unit, int x, int y, int targetX, int targetY) {
        this.unitUUID = unit.getUUID().toString();
        this.x = x;
        this.y = y;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    public int getId() {
        return 6;
    }

    public UUID getUnitUUID() {
        return UUID.fromString(unitUUID);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getTargetX() {
        return targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    @Override
    public void write(ByteBuf buf) throws UnsupportedEncodingException {
        super.write(buf);
        writeString(buf, getUnitUUID().toString());
        buf.writeInt(getX());
        buf.writeInt(getY());
        buf.writeInt(getTargetX());
        buf.writeInt(getTargetY());
    }
}
