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

import javax.swing.*;
import java.awt.*;

public class ConnectionPanel extends JPanel {

    private LD33Client client;

    private JLabel lblAddress;
    private JTextField addressField;
    private JButton btnConnect;

    public ConnectionPanel(LD33Client client) {
        this.client = client;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(640, 480));
        add(Box.createVerticalGlue());
        lblAddress = new JLabel("Server Address: ");
        lblAddress.setAlignmentX(CENTER_ALIGNMENT);
        add(lblAddress);
        addressField = new JTextField();
        addressField.setPreferredSize(new Dimension(256, 24));
        addressField.setMaximumSize(new Dimension(256, 24));
        addressField.setAlignmentX(CENTER_ALIGNMENT);
        addressField.addActionListener(event -> btnConnect.doClick());
        addressField.setText("seventh-root.com");
        add(addressField);
        lblAddress.setLabelFor(addressField);
        add(Box.createVerticalStrut(16));
        btnConnect = new JButton("Connect");
        btnConnect.setPreferredSize(new Dimension(64, 24));
        btnConnect.setAlignmentX(CENTER_ALIGNMENT);
        btnConnect.addActionListener(event -> {
            btnConnect.setEnabled(false);
            String address;
            int port;
            if (addressField.getText().contains(":")) {
                address = addressField.getText().split(":")[0];
                port = Integer.parseInt(addressField.getText().split(":")[1]);
            } else {
                address = addressField.getText();
                port = 37896;
            }
            new Thread(() -> client.connect(address, port)).start();
        });
        add(btnConnect);
        add(Box.createVerticalGlue());
    }

}
