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

package com.seventh_root.ld33.client;

import com.seventh_root.ld33.common.network.packet.serverbound.UnitPurchaseServerBoundPacket;
import com.seventh_root.ld33.common.world.Tile;

public class ShopItem {

    private LD33Client client;

    private String textureIdentifier;
    private String name;
    private String displayName;

    public ShopItem(LD33Client client, String textureIdentifier, String name, String displayName) {
        this.client = client;
        this.textureIdentifier = textureIdentifier;
        this.name = name;
        this.displayName = displayName;
    }

    public String getTextureIdentifier() {
        return textureIdentifier;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void buy(Tile tile) {
        new Thread(() -> client.getSoundPlayer().play(getClass().getResourceAsStream("/place.ogg"))).start();
        client.sendPacket(new UnitPurchaseServerBoundPacket(tile.getX(), tile.getY(), getName()));
        if (client.getPlayer().getResources() >= client.getEconomyManager().getResourceCost(getName()))
            client.getPlayer().setResources(client.getPlayer().getResources() - client.getEconomyManager().getResourceCost(getName()));
    }

}
