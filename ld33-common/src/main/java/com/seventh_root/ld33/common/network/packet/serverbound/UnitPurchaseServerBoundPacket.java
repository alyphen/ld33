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

package com.seventh_root.ld33.common.network.packet.serverbound;

import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;

public class UnitPurchaseServerBoundPacket extends ServerBoundPacket {

    private int x;
    private int y;
    private String unitType;

    public UnitPurchaseServerBoundPacket(int x, int y, String unitType) {
        this.x = x;
        this.y = y;
        this.unitType = unitType;
    }

    @Override
    public int getId() {
        return 8;
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
        buf.writeInt(getX());
        buf.writeInt(getY());
        writeString(buf, getUnitType());
    }

}
