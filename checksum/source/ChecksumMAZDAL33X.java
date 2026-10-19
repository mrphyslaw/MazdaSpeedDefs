// SPDX-License-Identifier: MIT
// Copyright (c) 2026 mrphyslaw
package com.romraider.maps.checksum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/** Program checksum for the stock L33XEH000 layout. */
public final class ChecksumMAZDAL33X implements ChecksumManager {
    private static final int LENGTH = 0x100000;
    private static final int START = 0x48000;
    private static final int TARGET = 0x5AA55AA5;
    private static final int ID_OFFSET = 0x2EA96;
    private static final byte[] ID = "L33XEH000L3T60A0".getBytes(StandardCharsets.US_ASCII);
    // The first word is the sum correction; executable code begins at START + 8.
    private static final int CORRECTION = START;
    private static final byte[] LOOP_HASH = java.util.Base64.getDecoder().decode(
        "kF4xRxsWXBVsnXv/u+F4klbovwreYW3GwRIEeEKXJ0w=");

    @Override public void configure(Map<String, String> attributes) {
        if (attributes.size() != 1 || !"MAZDAL33X".equals(attributes.get("type")))
            throw new IllegalArgumentException("Unsupported Mazda L33X checksum configuration");
    }
    @Override public int getNumberOfChecksums() { return 1; }
    private static int word(byte[] data, int at) {
        return ((data[at] & 255) << 24) | ((data[at + 1] & 255) << 16)
            | ((data[at + 2] & 255) << 8) | (data[at + 3] & 255);
    }
    private static void checkProfile(byte[] data) {
        if (data == null || data.length != LENGTH)
            throw new IllegalArgumentException("Mazda L33X checksum requires an exact 1 MiB image");
        for (int i = 0; i < ID.length; ++i)
            if (data[ID_OFFSET + i] != ID[i])
                throw new IllegalArgumentException("Mazda L33X checksum software ID mismatch");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(data, 0xB912C, 64);
            if (!MessageDigest.isEqual(LOOP_HASH, digest.digest()))
                throw new IllegalArgumentException("Mazda L33X native checksum routine mismatch");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
        if (word(data, 0xB946C) != START || word(data, 0xB9460) != LENGTH
                || word(data, 0xB942C) != TARGET || word(data, START + 4) != 0xFFFFFFFF
                || word(data, START + 8) != 0x7FFC4F22)
            throw new IllegalArgumentException("Mazda L33X checksum layout mismatch");
    }
    private static int sum(byte[] data) {
        int total = 0;
        for (int at = START; at < LENGTH; at += 4) total += word(data, at);
        return total;
    }
    @Override public int validate(byte[] data) {
        checkProfile(data);
        return sum(data) == TARGET ? 1 : 0;
    }
    @Override public int update(byte[] data) {
        checkProfile(data);
        int current = sum(data);
        if (current == TARGET) return 0;
        int replacement = word(data, CORRECTION) + TARGET - current;
        data[CORRECTION] = (byte)(replacement >>> 24);
        data[CORRECTION + 1] = (byte)(replacement >>> 16);
        data[CORRECTION + 2] = (byte)(replacement >>> 8);
        data[CORRECTION + 3] = (byte)replacement;
        if (sum(data) != TARGET) throw new IllegalStateException("Mazda checksum correction failed");
        return 1;
    }
}
