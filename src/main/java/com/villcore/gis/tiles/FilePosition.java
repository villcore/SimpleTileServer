package com.villcore.gis.tiles;

public class FilePosition {
    public final long start;
    public final int len;

    public FilePosition(long start, int len) {
        this.start = start;
        this.len = len;
    }
}
