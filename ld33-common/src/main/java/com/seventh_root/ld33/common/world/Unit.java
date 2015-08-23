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

import com.seventh_root.ld33.common.database.DatabaseEntity;
import com.seventh_root.ld33.common.player.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static java.lang.Math.abs;

public abstract class Unit implements DatabaseEntity {

    private static Map<String, Unit> unitsByUUID;

    static {
        unitsByUUID = new HashMap<>();
    }

    public static void cacheUnit(Unit unit) {
        unitsByUUID.put(unit.getUUID().toString(), unit);
    }

    public static void uncacheUnit(Unit unit) {
        unitsByUUID.remove(unit.getUUID().toString());
    }

    private Connection databaseConnection;

    private UUID uuid;
    private UUID playerUUID;
    private int health;
    private int maxHealth;
    private boolean solid;
    private Tile tile;
    private int dx;
    private int dy;
    private int xOffset;
    private int yOffset;
    private Tile target;
    private List<Tile> path;

    public Unit(Connection databaseConnection, Player player, int health, int maxHealth, boolean solid, Tile tile) {
        this.databaseConnection = databaseConnection;
        this.playerUUID = player.getUUID();
        this.health = health;
        this.maxHealth = maxHealth;
        this.solid = solid;
        this.tile = tile;
        this.dx = 0;
        this.dy = 0;
        this.xOffset = 0;
        this.yOffset = 0;
    }

    public Unit(Player player, int health, int maxHealth, boolean solid, Tile tile) {
        this(null, player, health, maxHealth, solid, tile);
    }

    public Connection getDatabaseConnection() {
        return databaseConnection;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public Player getPlayer() throws SQLException {
        return Player.getByUUID(databaseConnection, playerUUID);
    }

    public void setPlayer(Player player) {
        this.playerUUID = player.getUUID();
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public boolean isSolid() {
        return solid;
    }

    public void setSolid(boolean solid) {
        this.solid = solid;
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile.setUnit(null);
        this.tile = tile;
        tile.setUnit(this);
    }

    public int getDX() {
        return dx;
    }

    public void setDX(int dx) {
        this.dx = dx;
    }

    public int getDY() {
        return dy;
    }

    public void setDY(int dy) {
        this.dy = dy;
    }

    public int getXOffset() {
        return xOffset;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public Tile getTarget() {
        return target;
    }

    public void setTarget(Tile target) {
        this.target = target;
    }

    public void moveTo(Tile tile) {
        setTarget(tile);
    }

    public abstract int getSpeed();

    public void onTick() {
        if (getTarget() != null) {
            if (getTarget() != getTile()) {
                if (path == null) path = getTile().getWorld().findPath(getTile(), getTarget());
                if (path != null) {
                    Tile nextTile = path.get(0);
                    while (nextTile == getTile()) {
                        path.remove(0);
                        if (!path.isEmpty()) {
                            nextTile = path.get(0);
                        } else {
                            nextTile = null;
                            path = null;
                            break;
                        }
                    }
                    if (nextTile != null) {
                        if (abs(getXOffset()) == 64 || abs(getYOffset()) == 64) {
                            setTile(nextTile);
                            setXOffset(0);
                            setYOffset(0);
                        } else {
                            if (nextTile.getX() > getTile().getX()) {
                                setDX(getSpeed());
                            } else if (nextTile.getX() < getTile().getX()) {
                                setDX(-getSpeed());
                            } else {
                                setDX(0);
                            }
                            if (nextTile.getY() > getTile().getY()) {
                                setDY(getSpeed());
                            } else if (nextTile.getY() < getTile().getY()) {
                                setDY(-getSpeed());
                            } else {
                                setDY(0);
                            }
                            if (abs(getDX()) > 0) {
                                setXOffset(getXOffset() + getDX());
                            }
                            if (abs(getDY()) > 0) {
                                setYOffset(getYOffset() + getDY());
                            }
                        }
                    }
                } else {
                    setTarget(null);
                }
            }
        }
    }

    public static Unit getByUUID(Connection databaseConnection, World world, UUID uuid) throws SQLException {
        if (unitsByUUID.containsKey(uuid.toString())) return unitsByUUID.get(uuid.toString());
        if (databaseConnection != null) {
            PreparedStatement statement = databaseConnection.prepareStatement(
                    "SELECT uuid, player_uuid, health, max_health, solid, x, y, type FROM unit WHERE uuid = ? LIMIT 1"
            );
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Unit unit = fromResultSet(databaseConnection, world, resultSet);
                cacheUnit(unit);
                return unit;
            }
        }
        return null;
    }

    public static Unit getByPosition(Connection databaseConnection, World world, int x, int y) throws SQLException {
        PreparedStatement statement = databaseConnection.prepareStatement(
                "SELECT uuid, player_uuid, health, max_health, solid, x, y, type FROM unit WHERE x = ? AND y = ? LIMIT 1"
        );
        statement.setInt(1, x);
        statement.setInt(2, y);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            Unit unit = fromResultSet(databaseConnection, world, resultSet);
            cacheUnit(unit);
            return unit;
        }
        return null;
    }

    public static Unit fromResultSet(Connection databaseConnection, World world, ResultSet resultSet) throws SQLException {
        switch (resultSet.getString("type")) {
            case "wall":
                return new Wall(UUID.fromString(resultSet.getString("uuid")), Player.getByUUID(databaseConnection, UUID.fromString(resultSet.getString("player_uuid"))), world.getTileAt(resultSet.getInt("x"), resultSet.getInt("y")));
            case "dragon":
                return new Dragon(UUID.fromString(resultSet.getString("uuid")), Player.getByUUID(databaseConnection, UUID.fromString(resultSet.getString("player_uuid"))), world.getTileAt(resultSet.getInt("x"), resultSet.getInt("y")));
        }
        return null;
    }

    public static List<Unit> getAllUnits(Connection databaseConnection, World world) throws SQLException {
        PreparedStatement statement = databaseConnection.prepareStatement(
                "SELECT uuid, player_uuid, health, max_health, solid, x, y, type FROM unit"
        );
        ResultSet resultSet = statement.executeQuery();
        List<Unit> units = new ArrayList<>();
        while (resultSet.next()) {
            Unit unit = fromResultSet(databaseConnection, world, resultSet);
            cacheUnit(unit);
            units.add(unit);
        }
        return units;
    }
}
