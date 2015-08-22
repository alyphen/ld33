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

import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;

public class PlayerLoginServerBoundPacket extends ServerBoundPacket {

    private String playerName;
    private byte[] encryptedPassword;
    private boolean signUp;

    public PlayerLoginServerBoundPacket(String playerName, byte[] encryptedPassword, boolean signUp) {
        this.playerName = playerName;
        this.encryptedPassword = encryptedPassword;
        this.signUp = signUp;
    }

    @Override
    public int getId() {
        return 1;
    }

    public String getPlayerName() {
        return playerName;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

    public boolean isSignUp() {
        return signUp;
    }

    @Override
    public void write(ByteBuf buf) throws UnsupportedEncodingException {
        super.write(buf);
        writeString(buf, getPlayerName());
        buf.writeInt(getEncryptedPassword().length);
        buf.writeBytes(getEncryptedPassword());
        buf.writeBoolean(isSignUp());
    }
}
