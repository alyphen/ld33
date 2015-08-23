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
import com.seventh_root.ld33.common.network.packet.serverbound.ChatMessageServerBoundPacket;

import javax.swing.*;
import java.awt.*;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;

public class ChatPanel extends JPanel {

    private LD33Client client;

    private JTextArea textArea;

    public ChatPanel(LD33Client client) {
        this.client = client;
        setLayout(new BorderLayout());
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, CENTER);
        JTextField textField = new JTextField();
        textField.addActionListener(event -> {
            if (textField.getText().length() > 0) {
                client.sendPacket(new ChatMessageServerBoundPacket(textField.getText()));
                textField.setText("");
            }
        });
        add(textField, SOUTH);
        setPreferredSize(new Dimension(640, 128));
        setMinimumSize(new Dimension(640, 128));
    }

    public void append(String line) {
        textArea.append((textArea.getText().length() == 0 ? "" : System.getProperty("line.separator")) + line);
    }

}
