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
                    //TODO dragon movement
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
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics2D.translate(-cameraX, -cameraY);
        for (int x = cameraX / 64; x < (cameraX / 64) + (getWidth() / 64) + 2; x++) {
            for (int y = cameraY / 64; y < (cameraY / 64) + (getHeight() / 64) + 2; y++) {
                Tile tile = world.getTileAt(x, y);
                if (tile != null) {
                    graphics.drawImage(textures.get("grass"), x * 64, y * 64, null);
                    Unit unit = tile.getUnit();
                    if (unit != null) {
                        if (unit instanceof Dragon) {
                            graphics.drawImage(textures.get("dragon_down_1"), (x * 64) + unit.getXOffset(), (y * 64) + unit.getYOffset(), null);
                        } else if (unit instanceof Wall) {
                            graphics.drawImage(textures.get("tower"), (x * 64) + unit.getXOffset(), (y * 64) + unit.getYOffset(), null);
                        }
                    }
                }
            }
        }
        graphics2D.translate(cameraX, cameraY);
    }

    public World getWorld() {
        return world;
    }

    public void onTick() {
        repaint();
    }

    public void setCameraFocus(Unit unit) {
        cameraX = (unit.getTile().getX() * 64) - (getWidth() / 2) + 32;
        cameraY = (unit.getTile().getY() * 64) - (getHeight() / 2) + 32;
    }

}
