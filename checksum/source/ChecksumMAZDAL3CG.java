// SPDX-License-Identifier: MIT
// Copyright (c) 2026 mrphyslaw
package com.romraider.maps.checksum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/** Native code-sum and application-marker validation for L3CGEE000.
 * Calibration edits leave the verified code sum unchanged. There is no
 * established automatic correction location; invalid code is never repaired.
 */
public final class ChecksumMAZDAL3CG implements ChecksumManager {
    private static final int LENGTH = 0x100000;
    private static final byte[] ID = "L3CGEE000L3T6060".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] LOOP_HASH = java.util.Base64.getDecoder().decode("UMMV/wbuupfcknjPXvWjtiFNVNFqfq5HDchZEOs+jio=");
    @Override public void configure(Map<String,String> attributes) {
        if (attributes.size()!=1 || !"MAZDAL3CG".equals(attributes.get("type")))
            throw new IllegalArgumentException("Unsupported Mazda L3CG checksum configuration");
    }
    @Override public int getNumberOfChecksums() {return 1;}
    private static int word(byte[] data,int at) {
        return ((data[at]&255)<<24)|((data[at+1]&255)<<16)|((data[at+2]&255)<<8)|(data[at+3]&255);
    }
    private static int half(byte[] data,int at) {return ((data[at]&255)<<8)|(data[at+1]&255);}
    private static void profile(byte[] data) {
        if(data==null || data.length!=LENGTH)
            throw new IllegalArgumentException("Mazda L3CG requires an exact 1 MiB image");
        for(int i=0;i<ID.length;i++)if(data[0x476c+i]!=ID[i])
            throw new IllegalArgumentException("Mazda L3CG software ID mismatch");
        try {
            MessageDigest hash=MessageDigest.getInstance("SHA-256");
            hash.update(data,0x12d68,0x48);
            hash.update(data,0x12db0,0x1a);
            hash.update(data,0x12ee0,0xa8);
            if(!MessageDigest.isEqual(LOOP_HASH,hash.digest()))
                throw new IllegalArgumentException("Mazda L3CG native checksum routine mismatch");
        }catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    private static boolean valid(byte[] data) {
        int sum=0;
        for(int at=0x4000;at<0xc0000;at+=4)
            if(at<0x1c000 || at>=0x20000)sum+=word(data,at);
        return sum==0x5aa55aa5 && half(data,0x4000)==0x5aa5
            && half(data,0x1bffe)==0xa55a && half(data,0x20000)==0x5aa5
            && half(data,0xffffe)==0xa55a;
    }
    @Override public int validate(byte[] data) {profile(data);return valid(data)?1:0;}
    @Override public int update(byte[] data) {
        profile(data);
        if(!valid(data))throw new IllegalArgumentException("Mazda L3CG code checksum or application marker is invalid; automatic repair is unavailable");
        return 0;
    }
}
