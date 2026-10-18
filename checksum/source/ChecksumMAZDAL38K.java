// SPDX-License-Identifier: MIT
// Copyright (c) 2026 mrphyslaw
package com.romraider.maps.checksum;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Exact L38KEH000 program-region checksum, independently traced in the ROM. */
public final class ChecksumMAZDAL38K implements ChecksumManager {
    private static final int LENGTH = 0x100000;
    private static final int START = 0x48000;
    private static final int TARGET = 0x5AA55AA5;
    private static final int ID_OFFSET = 0x2A7D8;
    private static final byte[] ID = "L38KEH000L3T6021".getBytes(StandardCharsets.US_ASCII);
    private boolean requireInjectorCapacityPatch;

    @Override public void configure(Map<String, String> attributes) {
        // Coverage is fixed. An optional profile guard only narrows compatibility.
        String guard = attributes.get("requirepatch");
        if (guard != null && !guard.equals("INJECTORCAPACITY_V1"))
            throw new IllegalArgumentException("Unknown Mazda patch profile");
        requireInjectorCapacityPatch = guard != null;
    }
    @Override public int getNumberOfChecksums() { return 1; }

    private static void checkIdentity(byte[] data) {
        if (data == null || data.length != LENGTH)
            throw new IllegalArgumentException("Mazda L38KEH checksum requires an exact 1 MiB image");
        for (int i = 0; i < ID.length; ++i)
            if (data[ID_OFFSET + i] != ID[i])
                throw new IllegalArgumentException("Mazda L38KEH checksum software ID mismatch");
    }
    private static int word(byte[] data, int address) {
        return ((data[address] & 255) << 24) | ((data[address + 1] & 255) << 16)
            | ((data[address + 2] & 255) << 8) | (data[address + 3] & 255);
    }
    private static int sum(byte[] data) {
        int result = 0;
        for (int a = START; a < LENGTH; a += 4) result += word(data, a);
        return result;
    }
    private void checkProfile(byte[] data) {
        checkIdentity(data);
        if (requireInjectorCapacityPatch &&
            ((data[0xD0438] & 255) != 0xF1 || (data[0xD0439] & 255) != 0x02))
            throw new IllegalArgumentException("This definition requires the L38K injector-capacity multiplier patch");
    }
    @Override public int validate(byte[] data) {
        checkProfile(data);
        return sum(data) == TARGET ? 1 : 0;
    }
    @Override public int update(byte[] data) {
        checkProfile(data);
        int current = sum(data);
        if (current == TARGET) return 0;
        int replacement = word(data, START) + (TARGET - current);
        data[START] = (byte)(replacement >>> 24);
        data[START + 1] = (byte)(replacement >>> 16);
        data[START + 2] = (byte)(replacement >>> 8);
        data[START + 3] = (byte)replacement;
        if (sum(data) != TARGET) throw new IllegalStateException("Mazda checksum correction failed");
        return 1;
    }
}
