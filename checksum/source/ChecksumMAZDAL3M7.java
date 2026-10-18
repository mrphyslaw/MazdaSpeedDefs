// SPDX-License-Identifier: MIT
// Copyright (c) 2026 mrphyslaw
package com.romraider.maps.checksum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

/** Program checksum for the stock L3M7EF000 layout. */
public final class ChecksumMAZDAL3M7 implements ChecksumManager {
    private static final int LENGTH = 0x100000;
    private static final int START = 0x3F000;
    private static final int TARGET = 0x5AA55AA5;
    // Reserved padding immediately before the fixed eight-byte image trailer.
    // The beginning of the covered range contains instructions, not a sum word.
    private static final int CORRECTION = LENGTH - 12;
    private static final int ID_OFFSET = 0x2A2CC;
    private static final byte[] ID = "L3M7EF000L3T6061".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] LOOP_HASH = Base64.getDecoder().decode(
        "kF4xRxsWXBVsnXv/u+F4klbovwreYW3GwRIEeEKXJ0w=");

    @Override public void configure(Map<String, String> attributes) {
        if (attributes.size() != 1 || !"MAZDAL3M7".equals(attributes.get("type")))
            throw new IllegalArgumentException("Unsupported Mazda L3M7 checksum configuration");
    }
    @Override public int getNumberOfChecksums() { return 1; }
    private static int word(byte[] data, int at) {
        return ((data[at] & 255) << 24) | ((data[at + 1] & 255) << 16)
            | ((data[at + 2] & 255) << 8) | (data[at + 3] & 255);
    }
    private static void checkProfile(byte[] data) {
        if (data == null || data.length != LENGTH)
            throw new IllegalArgumentException("Mazda L3M7 checksum requires an exact 1 MiB image");
        for (int i = 0; i < ID.length; ++i)
            if (data[ID_OFFSET + i] != ID[i])
                throw new IllegalArgumentException("Mazda L3M7 checksum software ID mismatch");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(data, 0xA7EB0, 64);
            if (!MessageDigest.isEqual(LOOP_HASH, digest.digest()))
                throw new IllegalArgumentException("Mazda L3M7 native checksum routine mismatch");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
        if (word(data, 0xA81F0) != START || word(data, 0xA81E4) != LENGTH
                || word(data, 0xA81B0) != TARGET
                || word(data, LENGTH - 16) != 0xFFFFFFFF
                || word(data, LENGTH - 8) != 0xAA55AA55
                || word(data, LENGTH - 4) != 0x0000A55A)
            throw new IllegalArgumentException("Mazda L3M7 checksum layout mismatch");
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
