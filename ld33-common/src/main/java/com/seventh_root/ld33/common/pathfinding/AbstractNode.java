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
 * This class represents an AbstractNode. It has all the appropriate fields as well
 * as getter and setter to be used by the A* algorithm.
 * <p>
 * <p>
 * An <code>AbstractNode</code> has x- and y-coordinates and can be walkable or not.
 * A previous AbstractNode may be set, as well as the
 * <code>fCosts</code>, <code>gCosts</code> and <code>hCosts</code>.
 * <p>
 * <p>
 * <code>fCosts</code>: <code>gCosts</code> + <code>hCosts</code>
 * <p>
 * <code>gCosts</code>: calculated costs from start AbstractNode to this AbstractNode
 * <p>
 * <code>hCosts</code>: estimated costs to get from this AbstractNode to end AbstractNode
 * <p>
 * <p>
 * A subclass has to override the heuristic function
 * <p>
 * <code>setHCosts(AbstractNode endAbstractNode)</code>
 * <p>
 * @see NodeImpl#setHCosts(AbstractNode endNode) example Implementation using manhatten method
 * <p>
 *
 * @version 1.0
 */
public abstract class AbstractNode {

    /** costs to move sideways from one square to another. */
    protected static final int BASIC_MOVEMENT_COST = 10;

    private int x;
    private int y;
    private boolean walkable;

    // for pathfinding:

    /** the previous AbstractNode of this one on the currently calculated path. */
    private AbstractNode previous;

    /** optional extra penalty. */
    private int movementPenalty;

    /** calculated costs from start AbstractNode to this AbstractNode. */
    private int gCosts;

    /** estimated costs to get from this AbstractNode to end AbstractNode. */
    private int hCosts;

    /**
     * constructs a walkable AbstractNode with given coordinates.
     *
     * @param x
     * @param y
     */
    public AbstractNode(int x, int y) {
        this.x = x;
        this.y = y;
        this.walkable = true;
        this.movementPenalty = 0;
    }

    /**
     * sets x and y coordinates.
     *
     * @param x
     * @param y
     */
    public void setCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @return the walkable
     */
    public boolean isWalkable() {
        return walkable;
    }

    /**
     * @param walkable the walkable to set
     */
    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }

    /**
     * returns the node set as previous node on the current path.
     *
     * @return the previous
     */
    public AbstractNode getPrevious() {
        return previous;
    }

    /**
     * @param previous the previous to set
     */
    public void setPrevious(AbstractNode previous) {
        this.previous = previous;
    }

    /**
     * sets a general penalty for the movement on this node.
     *
     * @param movementPenalty the movementPenalty to set
     */
    public void setMovementPenalty(int movementPenalty) {
        this.movementPenalty = movementPenalty;
    }

    /**
     * returns <code>gCosts</code> + <code>hCosts</code>.
     * <p>
     *
     *
     * @return the fCosts
     */
    public int getFCosts() {
        return gCosts + hCosts;
    }

    /**
     * returns the calculated costs from start AbstractNode to this AbstractNode.
     *
     * @return the gCosts
     */
    public int getGCosts() {
        return gCosts;
    }

    /**
     * sets gCosts to <code>gCosts</code> plus <code>movementPenalty</code>
     * for this AbstractNode.
     *
     * @param gCosts the gCosts to set
     */
    private void setGCosts(int gCosts) {
        this.gCosts = gCosts + movementPenalty;
    }

    /**
     * sets gCosts to <code>gCosts</code> plus <code>movementPenalty</code>
     * for this AbstractNode given the previous AbstractNode as well as the basic cost
     * from it to this AbstractNode.
     *
     * @param previousAbstractNode
     * @param basicCost
     */
    public void setGCosts(AbstractNode previousAbstractNode, int basicCost) {
        setGCosts(previousAbstractNode.getGCosts() + basicCost);
    }

    /**
     * sets gCosts to <code>gCosts</code> plus <code>movementPenalty</code>
     * for this AbstractNode given the previous AbstractNode.
     * <p>
     * It will assume <code>BASIC_MOVEMENT_COST</code> as the cost from
     * <code>previousAbstractNode</code> to itself if the movement is not diagonally,
     * otherwise it will assume <code>DIAGONALMOVEMENTCOST</code>.
     * Weather or not it is diagonally is set in the Map class method which
     * finds the adjacent AbstractNodes.
     *
     * @param previousAbstractNode
     */
    public void setGCosts(AbstractNode previousAbstractNode) {
        setGCosts(previousAbstractNode, BASIC_MOVEMENT_COST);
    }

    /**
     * calculates - but does not set - g costs.
     * <p>
     * It will assume <code>BASIC_MOVEMENT_COST</code> as the cost from
     * <code>previousAbstractNode</code> to itself if the movement is not diagonally,
     * otherwise it will assume <code>DIAGONALMOVEMENTCOST</code>.
     * Weather or not it is diagonally is set in the Map class method which
     * finds the adjacent AbstractNodes.
     *
     * @param previousAbstractNode
     * @return gCosts
     */
    public int calculateGCosts(AbstractNode previousAbstractNode) {
            return previousAbstractNode.getGCosts() + BASIC_MOVEMENT_COST + movementPenalty;
    }

    /**
     * calculates - but does not set - g costs, adding a movementPenalty.
     *
     * @param previousAbstractNode
     * @param movementCost costs from previous AbstractNode to this AbstractNode.
     * @return gCosts
     */
    public int calculateGCosts(AbstractNode previousAbstractNode, int movementCost) {
        return previousAbstractNode.getGCosts() + movementCost + movementPenalty;
    }

    /**
     * returns estimated costs to get from this AbstractNode to end AbstractNode.
     *
     * @return the hCosts
     */
    public int gethCosts() {
        return hCosts;
    }

    /**
     * sets hCosts.
     *
     * @param hCosts the hCosts to set
     */
    protected void setHCosts(int hCosts) {
        this.hCosts = hCosts;
    }

    /**
     * calculates hCosts for this AbstractNode to a given end AbstractNode.
     * Uses Manhatten method.
     *
     * @param endAbstractNode
     */
    public abstract void setHCosts(AbstractNode endAbstractNode);


    /*
     * @return the movementPenalty
     */
    private int getMovementPenalty() {
        return movementPenalty;
    }

    /**
     * returns a String containing the coordinates, as well as h, f and g
     * costs.
     *
     * @return
     */
    @Override
    public String toString() {
        return "(" + getX() + ", " + getY() + "): h: " + gethCosts() + " g: " + getGCosts() + " f: " + getFCosts();
    }

    /**
     * returns whether the coordinates of AbstractNodes are equal.
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractNode other = (AbstractNode) obj;
        return this.x == other.x && this.y == other.y;
    }

    /**
     * returns hash code calculated with coordinates.
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + this.x;
        hash = 17 * hash + this.y;
        return hash;
    }

}
