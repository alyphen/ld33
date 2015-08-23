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

package com.seventh_root.ld33.common.pathfinding;

/**
 * A Factory which creates new instances of an implementation of the
 * <code>AbstractNode</code> at given coordinates.
 * <p>
 * Must be implemented and given to <code>Map</code> instance on
 * construction.
 *
 * @see AbstractNode
 * @version 1.0
 */
public interface NodeFactory {

    /**
     * creates new instances of an implementation of the
     * <code>AbstractNode</code>.
     * In an implementation, it should return a new node with its position
     * set to the given x and y values.
     *
     * @param x position on the x-axis
     * @param y position on the y-axis
     * @return
     */
    public AbstractNode createNode(int x, int y);

}
