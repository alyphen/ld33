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

import static java.lang.Math.abs;

/**
 * A simple Example implementation of a Node only overriding the setHCosts
 * method; uses manhatten method.
 */
public class NodeImpl extends AbstractNode {

        public NodeImpl(int x, int y) {
            super(x, y);
        }

        public void setHCosts(AbstractNode endNode) {
            setHCosts((abs(this.getX() - endNode.getX()) + abs(this.getY() - endNode.getY())) * BASIC_MOVEMENT_COST);
        }

}
