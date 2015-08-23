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

public class Tile {

    private World world;
    private int x;
    private int y;
    private Unit unit;

    public Tile(World world, int x, int y, Unit unit) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.unit = unit;
    }

    public Tile(World world, int x, int y) {
        this(world, x, y, null);
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Tile getAdjacent(int hor, int ver) {
        return getWorld().getTileAt(getX() + hor, getY() + ver);
    }

}
