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
import com.seventh_root.ld33.common.network.packet.serverbound.UnitMoveServerBoundPacket;
import com.seventh_root.ld33.common.world.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.sql.SQLException;

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

    public WorldPanel(LD33Client client) {
        this.client = client;
        world = new World(2000, 2000);
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
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics2D.translate(-cameraX, -cameraY);
        backGraphics.translate(-cameraX, -cameraY);
        frontGraphics.translate(-cameraX, -cameraY);
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
                        } else if (unit instanceof Wall) {
                            frontGraphics.drawImage(client.getTextureManager().getTexture("tower"), (x * 64) + unit.getXOffset(), (y * 64) + unit.getYOffset() - 128, null);
                        }
                    }
                }
            }
        }
        if (client.getShopPanel().getSelectedItem() == null) {
            backGraphics.setColor(new Color(1F, 0F, 0F, 0.5F));
        } else {
            backGraphics.setColor(new Color(0F, 0F, 1F, 0.5F));
        }
        backGraphics.fillRect(((cameraX - (int) getLocationOnScreen().getX() + (int) MouseInfo.getPointerInfo().getLocation().getX()) / 64) * 64, ((cameraY - (int) getLocationOnScreen().getY() + (int) MouseInfo.getPointerInfo().getLocation().getY()) / 64) * 64, 64, 64);
        if (client.getShopPanel().getSelectedItem() == null) {
            backGraphics.setColor(Color.RED);
        } else {
            backGraphics.setColor(Color.BLUE);
        }
        backGraphics.drawRect(((cameraX - (int) getLocationOnScreen().getX() + (int) MouseInfo.getPointerInfo().getLocation().getX()) / 64) * 64, ((cameraY - (int) getLocationOnScreen().getY() + (int) MouseInfo.getPointerInfo().getLocation().getY()) / 64) * 64, 64, 64);
        graphics2D.translate(cameraX, cameraY);
        backGraphics.translate(cameraX, cameraY);
        frontGraphics.translate(cameraX, cameraY);
        graphics.drawImage(backImage, 0, 0, null);
        graphics.drawImage(frontImage, 0, 0, null);
        backGraphics.dispose();
        frontGraphics.dispose();
        backImage.flush();
        frontImage.flush();
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
