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

package com.seventh_root.ld33.common.encrypt;

import javax.crypto.Cipher;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class EncryptionManager {

    private KeyPairGenerator keyPairGenerator;
    private KeyPair keyPair;

    public KeyPair getKeyPair() throws NoSuchAlgorithmException {
        if (keyPairGenerator == null) {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
        }
        if (keyPair == null) keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    public String decrypt(byte[] message) throws GeneralSecurityException, UnsupportedEncodingException {
        return decrypt(message, getKeyPair().getPrivate().getEncoded());
    }

    public String decrypt(byte[] message, byte[] encodedPrivateKey) throws GeneralSecurityException, UnsupportedEncodingException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encodedPrivateKey);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        PrivateKey privateKey = keyFactory.generatePrivate(spec);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(message);
        return new String(decrypted, "UTF8");
    }

    public byte[] encrypt(String message) throws GeneralSecurityException, UnsupportedEncodingException {
        return encrypt(message, getKeyPair().getPublic().getEncoded());
    }

    public byte[] encrypt(String message, byte[] encodedPublicKey) throws GeneralSecurityException, UnsupportedEncodingException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedPublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(spec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message.getBytes("UTF8"));
    }

}
