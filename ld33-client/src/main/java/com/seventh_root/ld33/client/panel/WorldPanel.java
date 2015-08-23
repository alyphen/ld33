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
import com.seventh_root.ld33.common.network.packet.serverbound.UnitMoveServerBoundPacket;
import com.seventh_root.ld33.common.world.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.logging.Level.WARNING;
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

public class WorldPanel extends JPanel {

    private World world;
    private int cameraX;
    private int cameraY;
    private Map<String, BufferedImage> textures;
    private Point mousePoint;
    private Unit selectedUnit;
    private int currentDragonFrame;
    private long millisSinceLastDragonFrameChange;
    private int currentFlagFrame;
    private long millisSinceLastFlagFrameChange;
    private long lastTickTime;

    public WorldPanel(LD33Client client) {
        world = new World(2000, 2000);
        cameraX = 0;
        cameraY = 0;
        textures = new HashMap<>();
        try {
            BufferedImage dragonImage = ImageIO.read(getClass().getResourceAsStream("/dragon.png"));
            textures.put("dragon_down_1", dragonImage.getSubimage(0, 0, 64, 64));
            textures.put("dragon_down_2", dragonImage.getSubimage(64, 0, 64, 64));
            textures.put("dragon_left_1", dragonImage.getSubimage(0, 64, 64, 64));
            textures.put("dragon_left_2", dragonImage.getSubimage(64, 64, 64, 64));
            textures.put("dragon_right_1", dragonImage.getSubimage(0, 128, 64, 64));
            textures.put("dragon_right_2", dragonImage.getSubimage(64, 128, 64, 64));
            textures.put("dragon_up_1", dragonImage.getSubimage(0, 192, 64, 64));
            textures.put("dragon_up_2", dragonImage.getSubimage(64, 192, 64, 64));
            BufferedImage flagImage = ImageIO.read(getClass().getResourceAsStream("/flag.png"));
            textures.put("flag_1", flagImage.getSubimage(0, 0, 128, 128));
            textures.put("flag_2", flagImage.getSubimage(128, 0, 128, 128));
            textures.put("flag_3", flagImage.getSubimage(256, 0, 128, 128));
            textures.put("flag_4", flagImage.getSubimage(384, 0, 128, 128));
            textures.put("grass", ImageIO.read(getClass().getResourceAsStream("/grass.png")));
            textures.put("tower", ImageIO.read(getClass().getResourceAsStream("/tower.png")));
            textures.put("tower_wall_down", ImageIO.read(getClass().getResourceAsStream("/tower_wall_down.png")));
            textures.put("tower_wall_down_left", ImageIO.read(getClass().getResourceAsStream("/tower_wall_down_left.png")));
            textures.put("tower_wall_down_left_right", ImageIO.read(getClass().getResourceAsStream("/tower_wall_down_left_right.png")));
            textures.put("tower_wall_down_right", ImageIO.read(getClass().getResourceAsStream("/tower_wall_down_right.png")));
            textures.put("tower_wall_left", ImageIO.read(getClass().getResourceAsStream("/tower_wall_left.png")));
            textures.put("tower_wall_right", ImageIO.read(getClass().getResourceAsStream("/tower_wall_right.png")));
            textures.put("tower_wall_up", ImageIO.read(getClass().getResourceAsStream("/tower_wall_up.png")));
            textures.put("tower_wall_up_down_left", ImageIO.read(getClass().getResourceAsStream("/tower_wall_up_down_left.png")));
            textures.put("tower_wall_up_down_left_right", ImageIO.read(getClass().getResourceAsStream("/tower_wall_up_down_left_right.png")));
            textures.put("tower_wall_up_down_right", ImageIO.read(getClass().getResourceAsStream("/tower_wall_up_down_right.png")));
            textures.put("tower_wall_up_left", ImageIO.read(getClass().getResourceAsStream("/tower_wall_up_left.png")));
            textures.put("tower_wall_up_left_right", ImageIO.read(getClass().getResourceAsStream("/tower_wall_up_left_right.png")));
            textures.put("tower_wall_up_right", ImageIO.read(getClass().getResourceAsStream("/tower_wall_up_right.png")));
            textures.put("wall_hor", ImageIO.read(getClass().getResourceAsStream("/wall_hor.png")));
            textures.put("wall_ver", ImageIO.read(getClass().getResourceAsStream("/wall_ver.png")));
        } catch (IOException exception) {
            client.getLogger().log(WARNING, "Failed to load one or more textures", exception);
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (isRightMouseButton(event)) {
                    client.sendPacket(new UnitMoveServerBoundPacket(
                            getSelectedUnit(),
                            (cameraX + event.getX()) / 64,
                            (cameraY + event.getY()) / 64
                    ));
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
                if (isLeftMouseButton(event)) {
                    int dx = event.getX() - (int) mousePoint.getX();
                    int dy = event.getY() - (int) mousePoint.getY();
                    cameraX -= dx;
                    cameraY -= dy;
                    mousePoint = event.getPoint();
                }
            }
        });
        setDoubleBuffered(true);
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
        for (int x = cameraX / 64; x < (cameraX / 64) + (getWidth() / 64) + 2; x++) {
            for (int y = cameraY / 64; y < (cameraY / 64) + (getHeight() / 64) + 2; y++) {
                Tile tile = world.getTileAt(x, y);
                if (tile != null) {
                    backGraphics.drawImage(textures.get("grass"), x * 64, y * 64, null);
                    Unit unit = tile.getUnit();
                    if (unit != null) {
                        if (unit instanceof Dragon) {
                            BufferedImage texture;
                            if (unit.getDX() > 0) {
                                texture = currentDragonFrame == 0 ? textures.get("dragon_right_1") : textures.get("dragon_right_2");
                            } else if (unit.getDX() < 0) {
                                texture = currentDragonFrame == 0 ? textures.get("dragon_left_1") : textures.get("dragon_left_2");
                            } else if (unit.getDY() > 0) {
                                texture = currentDragonFrame == 0 ? textures.get("dragon_down_1") : textures.get("dragon_down_2");
                            } else if (unit.getDY() < 0) {
                                texture = currentDragonFrame == 0 ? textures.get("dragon_up_1") : textures.get("dragon_up_2");
                            } else {
                                texture = currentDragonFrame == 0 ? textures.get("dragon_down_1") : textures.get("dragon_down_2");
                            }
                            frontGraphics.drawImage(texture, (x * 64) + unit.getXOffset(), (y * 64) + unit.getYOffset(), null);
                        } else if (unit instanceof Wall) {
                            frontGraphics.drawImage(textures.get("tower"), (x * 64) + unit.getXOffset(), (y * 64) + unit.getYOffset(), null);
                        }
                    }
                }
            }
        }
        backGraphics.setColor(new Color(1F, 0F, 0F, 0.5F));
        backGraphics.fillRect(((cameraX - (int) getLocationOnScreen().getX() + (int) MouseInfo.getPointerInfo().getLocation().getX()) / 64) * 64, ((cameraY - (int) getLocationOnScreen().getY() + (int) MouseInfo.getPointerInfo().getLocation().getY()) / 64) * 64, 64, 64);
        backGraphics.setColor(Color.RED);
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
        getWorld().onTick();
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
