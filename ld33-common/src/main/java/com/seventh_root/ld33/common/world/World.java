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

import com.seventh_root.ld33.common.pathfinding.Map;
import com.seventh_root.ld33.common.pathfinding.NodeFactoryImpl;
import com.seventh_root.ld33.common.pathfinding.NodeImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class World {

    private Tile[][] tiles;
    private int width;
    private int height;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        tiles = new Tile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new Tile(this, x, y);
            }
        }
    }

    public Tile getTileAt(int x, int y) {
        if (x > 0 && y > 0 && x < getWidth() && y < getHeight())
            return tiles[x][y];
        else
            return null;
    }

    public void onTick() throws SQLException {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile tile = getTileAt(x, y);
                if (tile != null) {
                    Unit unit = tile.getUnit();
                    if (unit != null) {
                        unit.onTick();
                    }
                }
            }
        }
    }

    public List<Tile> findPath(Tile start, Tile end) {
        Map<NodeImpl> map = new Map<>(width, height, new NodeFactoryImpl());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Unit unit = tiles[x][y].getUnit();
                map.setWalkable(x, y, unit == null || !unit.isSolid());
            }
        }
        List<NodeImpl> path = map.findPath(start.getX(), start.getY(), end.getX(), end.getY());
        if (path != null) {
            List<Tile> tiles = new ArrayList<>();
            path.forEach(node -> tiles.add(getTileAt(node.getX(), node.getY())));
            return tiles;
        }
        return null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
