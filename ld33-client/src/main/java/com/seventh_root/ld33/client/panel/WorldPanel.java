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

package com.seventh_root.ld33.client.panel;

import com.seventh_root.ld33.client.LD33Client;
import com.seventh_root.ld33.client.ShopItem;
import com.seventh_root.ld33.client.texture.TextureManager;
import com.seventh_root.ld33.common.network.packet.serverbound.UnitMoveServerBoundPacket;
import com.seventh_root.ld33.common.player.Player;
import com.seventh_root.ld33.common.world.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.Random;

import static java.lang.Math.*;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.swing.SwingUtilities.*;

public class WorldPanel extends JPanel {
    
    private LD33Client client;

    private World world;
    private int cameraX;
    private int cameraY;
    private Point mousePoint;
    private Unit selectedUnit;
    private int currentDragonFrame;
    private long millisSinceLastDragonFrameChange;
    private int currentFlagFrame;
    private long millisSinceLastFlagFrameChange;
    private long lastTickTime;

    public WorldPanel(LD33Client client, int worldWidth, int worldHeight) {
        this.client = client;
        world = new World(worldWidth, worldHeight);
        cameraX = 0;
        cameraY = 0;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (isRightMouseButton(event)) {
                    client.sendPacket(new UnitMoveServerBoundPacket(
                            getSelectedUnit(),
                            (cameraX + event.getX()) / 64,
                            (cameraY + event.getY()) / 64
                    ));
                    Tile tile = getWorld().getTileAt((cameraX + event.getX()) / 64, (cameraY + event.getY()) / 64);
                    if (tile != null) {
                        if (tile.getUnit() != null) {
                            if (!tile.getUnit().getPlayerUUID().toString().equals(client.getPlayer().getUUID().toString())) {
                                new Thread(() -> client.getSoundPlayer().play(getClass().getResourceAsStream("/burn.ogg"))).start();
                            }
                        }
                    }
                } else if (isLeftMouseButton(event)) {
                    ShopItem selectedShopItem = client.getShopPanel().getSelectedItem();
                    if (selectedShopItem != null) {
                        selectedShopItem.buy(getWorld().getTileAt((cameraX + event.getX()) / 64, (cameraY + event.getY()) / 64));
                        client.getShopPanel().setSelectedItem(null);
                        client.getShopPanel().repaint();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent event) {
                mousePoint = event.getPoint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                if (isMiddleMouseButton(event)) {
                    int dx = event.getX() - (int) mousePoint.getX();
                    int dy = event.getY() - (int) mousePoint.getY();
                    cameraX -= dx;
                    cameraY -= dy;
                    mousePoint = event.getPoint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent event) {
                Tile tile = getWorld().getTileAt((cameraX + event.getX()) / 64, (cameraY + event.getY()) / 64);
                if (tile != null) {
                    Unit unit = tile.getUnit();
                    if (unit != null) {
                        try {
                            Player player = unit.getPlayer();
                            if (player != null) {
                                setToolTipText("Owner: " + player.getName() + " " +
                                                "Health: " + unit.getHealth() + "/" + unit.getMaxHealth() + " " +
                                                "Tile: " + tile.getX() + ", " + tile.getY() +
                                                (unit.getTimeToComplete() > 0 ? " Seconds till completion: " + (unit.getTimeToComplete() / 1000) : "")
                                );
                            } else {
                                setToolTipText("Could not retrieve player information for this unit.");
                            }
                        } catch (SQLException exception) {
                            client.getLogger().log(SEVERE, "Failed to get player for unit", exception);
                        }
                    } else {
                        setToolTipText(null);
                    }
                } else {
                    setToolTipText(null);
                }
            }
        });
        setDoubleBuffered(true);
        setMinimumSize(new Dimension(640, 480));
        setPreferredSize(new Dimension(640, 480));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        BufferedImage backImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D backGraphics = backImage.createGraphics();
        BufferedImage frontImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D frontGraphics = frontImage.createGraphics();
        BufferedImage particleImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D particleGraphics = particleImage.createGraphics();
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics2D.translate(-cameraX, -cameraY);
        backGraphics.translate(-cameraX, -cameraY);
        frontGraphics.translate(-cameraX, -cameraY);
        particleGraphics.translate(-cameraX, -cameraY);
        for (int y = cameraY / 64; y < (cameraY / 64) + (getHeight() / 64) + 2; y++) {
            for (int x = cameraX / 64; x < (cameraX / 64) + (getWidth() / 64) + 2; x++) {
                Tile tile = world.getTileAt(x, y);
                if (tile != null) {
                    backGraphics.drawImage(client.getTextureManager().getTexture("grass"), x * 64, y * 64, null);
                    Unit unit = tile.getUnit();
                    if (unit != null) {
                        if (unit instanceof Dragon) {
                            BufferedImage texture;
                            if (unit.getDX() > 0) {
                                texture = currentDragonFrame == 0 ? client.getTextureManager().getTexture("dragon_right_1") : client.getTextureManager().getTexture("dragon_right_2");
                            } else if (unit.getDX() < 0) {
                                texture = currentDragonFrame == 0 ? client.getTextureManager().getTexture("dragon_left_1") : client.getTextureManager().getTexture("dragon_left_2");
                            } else if (unit.getDY() > 0) {
                                texture = currentDragonFrame == 0 ? client.getTextureManager().getTexture("dragon_down_1") : client.getTextureManager().getTexture("dragon_down_2");
                            } else if (unit.getDY() < 0) {
                                texture = currentDragonFrame == 0 ? client.getTextureManager().getTexture("dragon_up_1") : client.getTextureManager().getTexture("dragon_up_2");
                            } else {
                                texture = currentDragonFrame == 0 ? client.getTextureManager().getTexture("dragon_down_1") : client.getTextureManager().getTexture("dragon_down_2");
                            }
                            frontGraphics.drawImage(texture, (x * 64) + unit.getXOffset(), (y * 64) + unit.getYOffset(), null);
                            particleGraphics.setColor(Color.BLACK);
                            if (unit.getAttackTarget() != null && ((abs(unit.getAttackTarget().getTile().getX() - unit.getTile().getX()) == 1 && unit.getAttackTarget().getTile().getY() == unit.getTile().getY()) || (abs(unit.getAttackTarget().getTile().getY() - unit.getTile().getY()) == 1 && unit.getAttackTarget().getTile().getX() == unit.getTile().getX()))) {
                                int xStart = min(unit.getTile().getX(), unit.getAttackTarget().getTile().getX());
                                int xEnd = max(unit.getTile().getX(), unit.getAttackTarget().getTile().getX());
                                int yStart = min(unit.getTile().getY(), unit.getAttackTarget().getTile().getY());
                                int yEnd = max(unit.getTile().getY(), unit.getAttackTarget().getTile().getY());
                                Random random = new Random();
                                for (int pX = (xStart * 64) + 28; pX < (xEnd * 64) + 34; pX++) {
                                    for (int pY = (yStart * 64) + 28; pY < (yEnd * 64) + 34; pY++) {
                                        switch (random.nextInt(3)) {
                                            case 0:
                                                particleGraphics.setColor(Color.RED);
                                                break;
                                            case 1:
                                                particleGraphics.setColor(Color.ORANGE);
                                                break;
                                            case 2:
                                                particleGraphics.setColor(Color.YELLOW);
                                                break;
                                        }
                                        particleGraphics.fillOval(pX - 3 + random.nextInt(3), pY - 3 + random.nextInt(3), 4, 4);
                                    }
                                }
                            }
                        } else if (unit instanceof Wall) {
                            TextureManager textureManager = client.getTextureManager();
                            BufferedImage texture = textureManager.getTexture("tower");
                            int offset = 128;
                            Tile upTile = unit.getTile().getAdjacent(0, -1);
                            boolean up = upTile != null && upTile.getUnit() != null && upTile.getUnit() instanceof Wall && upTile.getUnit().isComplete();
                            Tile downTile = unit.getTile().getAdjacent(0, 1);
                            boolean down = downTile != null && downTile.getUnit() != null && downTile.getUnit() instanceof Wall && downTile.getUnit().isComplete();
                            Tile leftTile = unit.getTile().getAdjacent(-1, 0);
                            boolean left = leftTile != null && leftTile.getUnit() != null && leftTile.getUnit() instanceof Wall && leftTile.getUnit().isComplete();
                            Tile rightTile = unit.getTile().getAdjacent(1, 0);
                            boolean right = rightTile != null && rightTile.getUnit() != null && rightTile.getUnit() instanceof Wall && rightTile.getUnit().isComplete();
                            if (unit.isComplete()) {
                                if (up) {
                                    if (down) {
                                        if (left) {
                                            if (right) {
                                                texture = textureManager.getTexture("tower_wall_up_down_left_right");
                                            } else {
                                                texture = textureManager.getTexture("tower_wall_up_down_left");
                                            }
                                        } else if (right) {
                                            texture = textureManager.getTexture("tower_wall_up_down_right");
                                        } else {
                                            texture = textureManager.getTexture("wall_ver");
                                            offset = 64;
                                        }
                                    } else if (left) {
                                        if (right) {
                                            texture = textureManager.getTexture("tower_wall_up_left_right");
                                        } else {
                                            texture = textureManager.getTexture("tower_wall_up_left");
                                        }
                                    } else if (right) {
                                        texture = textureManager.getTexture("tower_wall_up_right");
                                    } else {
                                        texture = textureManager.getTexture("tower_wall_up");
                                    }
                                } else if (down) {
                                    if (left) {
                                        if (right) {
                                            texture = textureManager.getTexture("tower_wall_down_left_right");
                                        } else {
                                            texture = textureManager.getTexture("tower_wall_down_left");
                                        }
                                    } else if (right) {
                                        texture = textureManager.getTexture("tower_wall_down_right");
                                    } else {
                                        texture = textureManager.getTexture("tower_wall_down");
                                    }
                                } else if (left) {
                                    if (right) {
                                        texture = textureManager.getTexture("wall_hor");
                                        offset = 64;
                                    } else {
                                        texture = textureManager.getTexture("tower_wall_left");
                                    }
                                } else if (right) {
                                    texture = textureManager.getTexture("tower_wall_right");
                                }
                            } else {
                                texture = textureManager.getTexture("wall_in_progress");
                                offset = 64;
                            }
                            frontGraphics.drawImage(texture, (x * 64) + unit.getXOffset(), (y * 64) + unit.getYOffset() - offset, null);
                        } else if (unit instanceof Flag) {
                            BufferedImage texture = null;
                            if (currentFlagFrame == 0)
                                texture = client.getTextureManager().getTexture("flag_1");
                            else if (currentFlagFrame == 1)
                                texture = client.getTextureManager().getTexture("flag_2");
                            else if (currentFlagFrame == 2)
                                texture = client.getTextureManager().getTexture("flag_3");
                            else if (currentFlagFrame == 3)
                                texture = client.getTextureManager().getTexture("flag_4");
                            if (texture != null)
                                frontGraphics.drawImage(texture, (x * 64), (y * 64) - 64, null);
                        }
                    }
                }
            }
        }
        int mouseTileX = ((cameraX - (int) getLocationOnScreen().getX() + (int) MouseInfo.getPointerInfo().getLocation().getX()) / 64);
        int mouseTileY = ((cameraY - (int) getLocationOnScreen().getY() + (int) MouseInfo.getPointerInfo().getLocation().getY()) / 64);
        Tile mouseTile = getWorld().getTileAt(mouseTileX, mouseTileY);
        if (mouseTile != null) {
            if (mouseTile.getUnit() == null) {
                if (client.getShopPanel().getSelectedItem() == null) {
                    backGraphics.setColor(new Color(0F, 1F, 0F, 0.5F));
                } else {
                    backGraphics.setColor(new Color(0F, 0F, 1F, 0.5F));
                }
            } else {
                backGraphics.setColor(new Color(1F, 0F, 0F, 0.5F));
            }
            backGraphics.fillRect(mouseTileX * 64, mouseTileY * 64, 64, 64);
            if (mouseTile.getUnit() == null) {
                if (client.getShopPanel().getSelectedItem() == null) {
                    backGraphics.setColor(Color.GREEN);
                } else {
                    backGraphics.setColor(Color.BLUE);
                }
            } else {
                backGraphics.setColor(Color.RED);
            }
            backGraphics.drawRect(((cameraX - (int) getLocationOnScreen().getX() + (int) MouseInfo.getPointerInfo().getLocation().getX()) / 64) * 64, ((cameraY - (int) getLocationOnScreen().getY() + (int) MouseInfo.getPointerInfo().getLocation().getY()) / 64) * 64, 64, 64);
        }
        graphics2D.translate(cameraX, cameraY);
        backGraphics.translate(cameraX, cameraY);
        frontGraphics.translate(cameraX, cameraY);
        particleGraphics.translate(cameraX, cameraY);
        graphics.drawImage(backImage, 0, 0, null);
        graphics.drawImage(frontImage, 0, 0, null);
        graphics.drawImage(particleImage, 0, 0, null);
        backGraphics.dispose();
        frontGraphics.dispose();
        particleGraphics.dispose();
        backImage.flush();
        frontImage.flush();
        particleImage.flush();
    }

    public World getWorld() {
        return world;
    }

    public void onTick() {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastTickTime;
        millisSinceLastDragonFrameChange += timeDiff;
        if (millisSinceLastDragonFrameChange > 500) {
            cycleDragonFrame();
            millisSinceLastDragonFrameChange = 0;
        }
        millisSinceLastFlagFrameChange += timeDiff;
        if (millisSinceLastFlagFrameChange > 500) {
            cycleFlagFrame();
            millisSinceLastFlagFrameChange = 0;
        }
        lastTickTime = currentTime;
        try {
            getWorld().onTick();
        } catch (SQLException exception) {
            client.getLogger().log(WARNING, "Failed to update unit in database (but this is the client, so it should be fine)", exception);
        }
        repaint();
    }

    public void cycleDragonFrame() {
        currentDragonFrame = currentDragonFrame == 0 ? 1 : 0;
    }

    public void cycleFlagFrame() {
        currentFlagFrame += 1;
        if (currentFlagFrame > 3) {
            currentFlagFrame = 0;
        }
    }

    public void setCameraFocus(Unit unit) {
        cameraX = (unit.getTile().getX() * 64) - (getWidth() / 2) + 32;
        cameraY = (unit.getTile().getY() * 64) - (getHeight() / 2) + 32;
    }

    public void setSelectedUnit(Unit selectedUnit) {
        this.selectedUnit = selectedUnit;
    }

    public Unit getSelectedUnit() {
        return selectedUnit;
    }
}
