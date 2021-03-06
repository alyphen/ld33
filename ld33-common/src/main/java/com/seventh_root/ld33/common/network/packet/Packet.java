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

package com.seventh_root.ld33.common.network.packet;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public abstract class Packet implements Serializable {

    public abstract int getId();

    public void write(ByteBuf buf) throws UnsupportedEncodingException {
        buf.writeInt(getId());
    }

    public void writeString(ByteBuf buf, String string) throws UnsupportedEncodingException {
        buf.writeBytes((string + "\0").getBytes("utf8"));
    }

}
