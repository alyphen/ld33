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
import com.seventh_root.ld33.common.network.packet.serverbound.PlayerLoginServerBoundPacket;

import javax.swing.*;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class LoginPanel extends JPanel {

    private LD33Client client;

    private JLabel lblUserName;
    private JTextField userNameField;
    private JLabel lblPassword;
    private JPasswordField passwordField;
    private JButton btnLogin;
    private JButton btnSignUp;
    private JLabel lblStatus;

    public LoginPanel(LD33Client client) {
        this.client = client;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(640, 480));
        add(Box.createVerticalGlue());
        lblUserName = new JLabel("Username: ");
        lblUserName.setAlignmentX(CENTER_ALIGNMENT);
        add(lblUserName);
        userNameField = new JTextField();
        userNameField.setPreferredSize(new Dimension(256, 24));
        userNameField.setMaximumSize(new Dimension(256, 24));
        userNameField.setAlignmentX(CENTER_ALIGNMENT);
        userNameField.addActionListener(event -> btnLogin.doClick());
        add(userNameField);
        lblUserName.setLabelFor(userNameField);
        add(Box.createVerticalStrut(16));
        lblPassword = new JLabel("Password: ");
        lblPassword.setAlignmentX(CENTER_ALIGNMENT);
        add(lblPassword);
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(256, 24));
        passwordField.setMaximumSize(new Dimension(256, 24));
        passwordField.setAlignmentX(CENTER_ALIGNMENT);
        passwordField.addActionListener(event -> btnLogin.doClick());
        add(passwordField);
        lblPassword.setLabelFor(passwordField);
        add(Box.createVerticalStrut(16));
        btnLogin = new JButton("Login");
        btnLogin.setPreferredSize(new Dimension(64, 24));
        btnLogin.setAlignmentX(CENTER_ALIGNMENT);
        btnLogin.addActionListener(event -> {
            btnLogin.setEnabled(false);
            btnSignUp.setEnabled(false);
            try {
                client.setPlayerName(userNameField.getText());
                client.sendPacket(
                        new PlayerLoginServerBoundPacket(
                                userNameField.getText(),
                                client.getEncryptionManager().encrypt(new String(passwordField.getPassword()), client.getServerPublicKey()),
                                false
                        )
                );
            } catch (GeneralSecurityException | UnsupportedEncodingException exception) {
                exception.printStackTrace();
            }
        });
        add(btnLogin);
        add(Box.createVerticalStrut(16));
        btnSignUp = new JButton("Sign up");
        btnSignUp.setPreferredSize(new Dimension(64, 24));
        btnSignUp.setAlignmentX(CENTER_ALIGNMENT);
        btnSignUp.addActionListener(event -> {
            btnLogin.setEnabled(false);
            btnSignUp.setEnabled(false);
            try {
                client.setPlayerName(userNameField.getText());
                client.sendPacket(
                        new PlayerLoginServerBoundPacket(
                                userNameField.getText(),
                                client.getEncryptionManager().encrypt(new String(passwordField.getPassword()), client.getServerPublicKey()),
                                true
                        )
                );
            } catch (GeneralSecurityException | UnsupportedEncodingException exception) {
                exception.printStackTrace();
            }
        });
        add(btnSignUp);
        add(Box.createVerticalStrut(16));
        lblStatus = new JLabel("");
        lblStatus.setAlignmentX(CENTER_ALIGNMENT);
        add(lblStatus);
        add(Box.createVerticalGlue());
    }

    public void setStatusMessage(String message) {
        lblStatus.setText(message);
    }

    public void reEnableLoginButtons() {
        btnSignUp.setEnabled(true);
        btnLogin.setEnabled(true);
    }

}
