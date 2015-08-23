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

package com.seventh_root.ld33.common.player;

import com.seventh_root.ld33.common.database.DatabaseEntity;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public class Player implements DatabaseEntity {

    private static Map<String, Player> playersByUUID;
    private static Map<String, Player> playersByName;

    static {
        playersByUUID = new HashMap<>();
        playersByName = new HashMap<>();
    }

    public static void cachePlayer(Player player) {
        playersByUUID.put(player.getUUID().toString(), player);
        playersByName.put(player.getName(), player);
    }

    public static void uncachePlayer(Player player) {
        playersByUUID.remove(player.getUUID().toString());
        playersByName.remove(player.getName());
    }

    private Connection databaseConnection;

    private UUID uuid;
    private String name;
    private String passwordHash;
    private String passwordSalt;
    private int resources;

    public Player(Connection databaseConnection, String name, String password) throws SQLException {
        this.databaseConnection = databaseConnection;
        this.name = name;
        setPassword(password);
        this.resources = 100;
        insert();
    }

    private Player(Connection databaseConnection, UUID uuid, String name, String passwordHash, String passwordSalt, int resources) {
        this.databaseConnection = databaseConnection;
        this.uuid = uuid;
        this.name = name;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.resources = resources;
    }

    public Player(UUID uuid, String name, int resources) {
        this.uuid = uuid;
        this.name = name;
        this.resources = resources;
    }

    public Connection getDatabaseConnection() {
        return databaseConnection;
    }

    public UUID getUUID() {
        return uuid;
    }

    private void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws SQLException {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPassword(String password) throws SQLException {
        passwordSalt = RandomStringUtils.randomAlphanumeric(32);
        passwordHash = DigestUtils.sha256Hex(password + passwordSalt);
    }

    public boolean checkPassword(String password) {
        return sha256Hex(password + getPasswordSalt()).equals(getPasswordHash());
    }

    public int getResources() {
        return resources;
    }

    public void setResources(int resources) {
        this.resources = resources;
    }

    @Override
    public void insert() throws SQLException {
        PreparedStatement statement = getDatabaseConnection().prepareStatement(
                "INSERT INTO player(uuid, name, password_hash, password_salt, resources) VALUES(?, ?, ?, ?, ?)"
        );
        setUUID(UUID.randomUUID());
        statement.setString(1, getUUID().toString());
        statement.setString(2, getName());
        statement.setString(3, getPasswordHash());
        statement.setString(4, getPasswordSalt());
        statement.setInt(5, getResources());
        statement.executeUpdate();
        cachePlayer(this);
    }

    @Override
    public void update() throws SQLException {
        PreparedStatement statement = getDatabaseConnection().prepareStatement(
                "UPDATE player SET name = ?, password_hash = ?, password_salt = ?, resources = ? WHERE uuid = ?"
        );
        statement.setString(1, getName());
        statement.setString(2, getPasswordHash());
        statement.setString(3, getPasswordSalt());
        statement.setInt(4, getResources());
        statement.setString(5, getUUID().toString());
        statement.executeUpdate();
    }

    @Override
    public void delete() throws SQLException {
        PreparedStatement statement = getDatabaseConnection().prepareStatement(
                "DELETE FROM player WHERE uuid = ?"
        );
        statement.setString(1, getUUID().toString());
        uncachePlayer(this);
    }

    public static Player getByUUID(Connection databaseConnection, UUID uuid) throws SQLException {
        if (playersByUUID.containsKey(uuid.toString())) return playersByUUID.get(uuid.toString());
        if (databaseConnection != null) {
            PreparedStatement statement = databaseConnection.prepareStatement(
                    "SELECT uuid, name, password_hash, password_salt, resources FROM player WHERE uuid = ? LIMIT 1"
            );
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Player player = new Player(databaseConnection, UUID.fromString(resultSet.getString("uuid")), resultSet.getString("name"), resultSet.getString("password_hash"), resultSet.getString("password_salt"), resultSet.getInt("resources"));
                cachePlayer(player);
                return player;
            }
        }
        return null;
    }

    public static Player getByName(Connection databaseConnection, String playerName) throws SQLException {
        if (playersByName.containsKey(playerName)) return playersByName.get(playerName);
        if (databaseConnection != null) {
            PreparedStatement statement = databaseConnection.prepareStatement(
                    "SELECT uuid, name, password_hash, password_salt, resources FROM player WHERE name = ? LIMIT 1"
            );
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Player(databaseConnection, UUID.fromString(resultSet.getString("uuid")), resultSet.getString("name"), resultSet.getString("password_hash"), resultSet.getString("password_salt"), resultSet.getInt("resources"));
            }
        }
        return null;
    }

}
