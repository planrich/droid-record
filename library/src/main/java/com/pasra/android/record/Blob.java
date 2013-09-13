package com.pasra.android.record;

/**
 * Created by rich on 9/13/13.
 */
public class Blob {

    private byte[] mBytes;

    public Blob(byte[] mBytes) {
        this.mBytes = mBytes;
    }

    public byte[] array() {
        return mBytes;
    }
}
