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

package com.seventh_root.ld33.client.texture;

import com.seventh_root.ld33.client.LD33Client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.logging.Level.WARNING;

public class TextureManager {

    private Map<String, BufferedImage> textures;

    public TextureManager(LD33Client client) {
        textures = new HashMap<>();
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
            textures.put("resources", ImageIO.read(getClass().getResourceAsStream("/resources.png")));
            textures.put("time", ImageIO.read(getClass().getResourceAsStream("/time.png")));
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
            textures.put("wall_in_progress", ImageIO.read(getClass().getResourceAsStream("/wall_in_progress.png")));
            textures.put("wall_ver", ImageIO.read(getClass().getResourceAsStream("/wall_ver.png")));
        } catch (IOException exception) {
            client.getLogger().log(WARNING, "Failed to load one or more textures", exception);
        }
    }

    public BufferedImage getTexture(String identifier) {
        return textures.get(identifier);
    }

}
