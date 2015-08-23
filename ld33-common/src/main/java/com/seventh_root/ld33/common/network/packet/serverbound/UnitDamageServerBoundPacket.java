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

package com.seventh_root.ld33.common.network.packet.serverbound;

import java.util.UUID;

public class UnitDamageServerBoundPacket extends ServerBoundPacket {

    private String unitUUID;
    private int unitHealth;

    public UnitDamageServerBoundPacket(String unitUUID, int unitHealth) {
        this.unitUUID = unitUUID;
        this.unitHealth = unitHealth;
    }

    @Override
    public int getId() {
        return 11;
    }

    public UUID getUnitUUID() {
        return UUID.fromString(unitUUID);
    }

    public int getUnitHealth() {
        return unitHealth;
    }

}
