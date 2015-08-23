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

package com.seventh_root.ld33.common.economy;

import java.util.HashMap;
import java.util.Map;

public class EconomyManager {

    private Map<String, Integer> resourceCosts;
    private Map<String, Integer> timeCosts;

    public EconomyManager() {
        resourceCosts = new HashMap<>();
        resourceCosts.put("wall", 10);
        resourceCosts.put("flag", 100);
        timeCosts = new HashMap<>();
        timeCosts.put("wall", 2);
        timeCosts.put("flag", 0);
    }

    public int getResourceCost(String unitType) {
        return resourceCosts.get(unitType);
    }

    public int getTimeCost(String unitType) {
        return timeCosts.get(unitType);
    }

}
