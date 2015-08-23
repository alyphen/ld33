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

package com.seventh_root.ld33.common.world;

import com.seventh_root.ld33.common.player.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class Wall extends Unit {

    public Wall(Connection databaseConnection, Player player, Tile tile) {
        super(databaseConnection, player, 100, 100, true, tile);
    }

    public Wall(UUID uuid, Player player, Tile tile) {
        super(player, 100, 100, true, tile);
        setUUID(uuid);
        setPlayer(player);
        setTile(tile);
    }

    @Override
    public void insert() throws SQLException {
        PreparedStatement statement = getDatabaseConnection().prepareStatement(
                "INSERT INTO unit(uuid, player_uuid, health, max_health, solid, x, y, type) VALUES(?, ?, ?, ?, ?, ?, ?, ?)"
        );
        setUUID(UUID.randomUUID());
        statement.setString(1, getUUID().toString());
        statement.setString(2, getPlayer().getUUID().toString());
        statement.setInt(3, getHealth());
        statement.setInt(4, getMaxHealth());
        statement.setBoolean(5, isSolid());
        statement.setInt(6, getTile().getX());
        statement.setInt(7, getTile().getY());
        statement.setString(8, "wall");
        statement.executeUpdate();
    }

    @Override
    public void update() throws SQLException {
        PreparedStatement statement = getDatabaseConnection().prepareStatement(
                "UPDATE unit SET player_uuid = ?, health = ?, max_health = ?, solid = ?, x = ?, y = ?, type = ? WHERE uuid = ?"
        );
        statement.setString(1, getPlayer().getUUID().toString());
        statement.setInt(2, getHealth());
        statement.setInt(3, getMaxHealth());
        statement.setBoolean(4, isSolid());
        statement.setInt(5, getTile().getX());
        statement.setInt(6, getTile().getY());
        statement.setString(7, "wall");
        statement.setString(8, getUUID().toString());
        statement.executeUpdate();
    }

    @Override
    public void delete() throws SQLException {
        PreparedStatement statement = getDatabaseConnection().prepareStatement(
                "DELETE FROM unit WHERE uuid = ?"
        );
        statement.setString(1, getUUID().toString());
        statement.executeUpdate();
    }

    @Override
    public int getSpeed() {
        return 0;
    }

}
