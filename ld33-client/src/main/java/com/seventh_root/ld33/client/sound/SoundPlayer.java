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

package com.seventh_root.ld33.client.sound;

import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;

public class SoundPlayer {

    private final List<SourceDataLine> lines = Collections.synchronizedList(new ArrayList<>());
    private Set<String> looping = new HashSet<>();

    public void loop(String path) {
        setLooping(path, true);
        while (isLooping(path)) {
            play(getClass().getResourceAsStream(path));
        }
    }

    public void play(InputStream inputStream) {
        try (final AudioInputStream in = getAudioInputStream(inputStream)) {
            final AudioFormat outFormat = getOutFormat(in.getFormat());
            final Info info = new Info(SourceDataLine.class, outFormat);
            try (final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                if (line != null) {
                    lines.add(line);
                    line.open(outFormat);
                    line.start();
                    stream(getAudioInputStream(outFormat, in), line);
                    line.drain();
                    line.stop();
                    lines.remove(line);
                }
            }
        } catch (UnsupportedAudioFileException
                | LineUnavailableException
                | IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public void stopAll() {
        synchronized (lines) {
            for (Iterator<SourceDataLine> iterator = lines.iterator(); iterator.hasNext(); ) {
                SourceDataLine line = iterator.next();
                line.stop();
                iterator.remove();
            }
        }
    }

    private AudioFormat getOutFormat(AudioFormat inFormat) {
        final int ch = inFormat.getChannels();
        final float rate = inFormat.getSampleRate();
        return new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
    }

    private void stream(AudioInputStream in, SourceDataLine line)
            throws IOException {
        final byte[] buffer = new byte[4096];
        for (int n = 0; n != -1; n = in.read(buffer, 0, buffer.length)) {
            line.write(buffer, 0, n);
        }
    }

    private boolean isLooping(String path) {
        return looping.contains(path);
    }

    private void setLooping(String path, boolean looping) {
        if (looping) this.looping.add(path); else this.looping.remove(path);
    }

}
