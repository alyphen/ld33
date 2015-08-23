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

import com.seventh_root.ld33.common.network.packet.clientbound.ClientBoundPacket;
import com.seventh_root.ld33.common.player.Player;
import com.seventh_root.ld33.common.world.Dragon;
import com.seventh_root.ld33.common.world.Unit;
import com.seventh_root.ld33.common.world.Wall;
import com.seventh_root.ld33.common.world.World;
import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.UUID;

public class UnitSpawnServerBoundPacket extends ClientBoundPacket {

    private String unitUUID;
    private String playerUUID;
    private int x;
    private int y;
    private String type;
    private long completionTime;

    public UnitSpawnServerBoundPacket(Unit unit) throws SQLException {
        this.unitUUID = unit.getUUID().toString();
        this.playerUUID = unit.getPlayer().getUUID().toString();
        this.x = unit.getTile().getX();
        this.y = unit.getTile().getY();
        this.completionTime = unit.getCompletionTime();
        if (unit instanceof Wall)
            this.type = "wall";
        else if (unit instanceof Dragon)
            this.type = "dragon";
    }

    @Override
    public int getId() {
        return 5;
    }

    public Unit getUnit(World world) throws SQLException {
        switch (type) {
            case "wall":
                return new Wall(UUID.fromString(unitUUID), Player.getByUUID(null, UUID.fromString(playerUUID)), world.getTileAt(x, y), completionTime);
            case "dragon":
                return new Dragon(UUID.fromString(unitUUID), Player.getByUUID(null, UUID.fromString(playerUUID)), world.getTileAt(x, y), completionTime);
        }
        return null;
    }

    @Override
    public void write(ByteBuf buf) throws UnsupportedEncodingException {
        super.write(buf);
        writeString(buf, unitUUID);
        writeString(buf, playerUUID);
        buf.writeInt(x);
        buf.writeInt(y);
        writeString(buf, type);
        buf.writeLong(completionTime);
    }
}
