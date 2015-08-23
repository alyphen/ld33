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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static java.awt.Color.BLACK;

public class ShopPanel extends JPanel {

    private LD33Client client;

    private List<ShopItem> shopItems;
    private ShopItem selectedItem;

    public ShopPanel(LD33Client client) {
        this.client = client;
        shopItems = new ArrayList<>();
        shopItems.add(new ShopItem(client, "wall_hor", "wall", "Wall"));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                for (int i = 0; i < shopItems.size(); i++) {
                    ShopItem shopItem = shopItems.get(i);
                    int x = 16;
                    int y = 16 + (i * 208);
                    int width = getWidth() - 32;
                    int height = 160;
                    if (event.getX() > x && event.getY() > y && event.getX() < x + width && event.getY() < y + height) {
                        setSelectedItem(shopItem);
                    }
                }
                repaint();
            }
        });
        setMinimumSize(new Dimension(256, 480));
        setPreferredSize(new Dimension(256, 480));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        graphics.setColor(BLACK);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < shopItems.size(); i++) {
            ShopItem shopItem = shopItems.get(i);
            int x = 16;
            int y = 16 + (i * 208);
            if (shopItem == getSelectedItem()) {
                graphics.setColor(Color.BLUE);
                graphics.fillRoundRect(x - 8, y - 8, getWidth() - 16, 176, 16, 16);
            }
            graphics.setColor(Color.DARK_GRAY);
            graphics.fillRoundRect(x, y, getWidth() - 32, 160, 16, 16);
            graphics.drawImage(client.getTextureManager().getTexture(shopItem.getTextureIdentifier()), x + 16, y + 16, null);
            graphics.drawImage(client.getTextureManager().getTexture("resources"), x + 96, y + 48, null);
            graphics.drawImage(client.getTextureManager().getTexture("time"), x + 96, y + 80, null);
            graphics.setColor(Color.WHITE);
            graphics.drawString(Integer.toString(client.getEconomyManager().getResourceCost(shopItem.getName())), x + 120, y + 48 + graphics.getFontMetrics().getMaxAscent());
            graphics.drawString(Integer.toString(client.getEconomyManager().getTimeCost(shopItem.getName())), x + 120, y + 80 + graphics.getFontMetrics().getMaxAscent());
            graphics.drawString(shopItem.getDisplayName(), x + 120, y + 16 + graphics.getFontMetrics().getMaxAscent());
        }
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRoundRect(16, getHeight() - 32, getWidth() - 32, 64, 16, 16);
        graphics.drawImage(client.getTextureManager().getTexture("resources"), 24, getHeight() - 24, null);
        graphics.setColor(Color.WHITE);
        if (client.getPlayer() != null) graphics.drawString(Integer.toString(client.getPlayer().getResources()), 48, getHeight() - 24 + graphics.getFontMetrics().getMaxAscent());
    }

    public ShopItem getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(ShopItem selectedItem) {
        this.selectedItem = selectedItem;
    }

    public void onTick() {
        repaint();
    }

}
