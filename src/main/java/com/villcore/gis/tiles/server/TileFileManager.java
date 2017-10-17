package com.villcore.gis.tiles.server;

import com.villcore.gis.tiles.FilePosition;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class TileFileManager {

    private static final byte[] ZERO_BYTES_ARRAY = new byte[0];
    private static final int INDEX_BLOCK_LEN = 4 + 4 + 8 + 4;

    private Path curRoot;

    private Path metaPath;
    private Path indexPath;
    private Path mapPath;

    private int xStart;
    private int xEnd;
    private int yStart;
    private int yEnd;

    private MappedByteBuffer indexByteBuffer;
    private MappedByteBuffer mapByteBuffer;

    public byte[] getTile(int xLevel, int yLevel) throws IOException {
        if(!correct(xLevel, yLevel)) {
            return ZERO_BYTES_ARRAY;
        }

        FilePosition filePosition = getTilePosition(xLevel, yLevel);
        return getTileToBytes(filePosition);
    }

    private byte[] getTileToBytes(FilePosition filePosition) {
        byte[] bytes = new byte[filePosition.len];
        mapByteBuffer.position((int) filePosition.start);
        mapByteBuffer.get(bytes);
        mapByteBuffer.position(0);
        return bytes;
    }

    private FilePosition getTilePosition(int x, int y) throws IOException {
        long indexPos = getIndexPosition(x, y, INDEX_BLOCK_LEN);
//        byte[] bytes = new byte[INDEX_BLOCK_LEN];
//
//        indexByteBuffer.position((int) indexPos);
//        indexByteBuffer.get(bytes);
//        indexByteBuffer.position(0);
//
//        ByteBuffer indexBlockBuffer = ByteBuffer.wrap(bytes);



        return new FilePosition(indexByteBuffer.getLong((int) (indexPos + 4 + 4)), indexByteBuffer.getInt((int) (indexPos + 4 + 4 + 8)));
    }

    private long getIndexPosition(int xPos, int yPos, int blockLen) {
        return xPos * yPos * blockLen;
    }

    public void getTile(FilePosition filePosition, ByteBuffer byteBuffer) {
    }

    public ByteBuffer getTileToBuffer(FilePosition filePosition) {
        int start = (int) filePosition.start;
        int end = (int) (filePosition.start + filePosition.len);
        ByteBuffer byteBuffer = mapByteBuffer.duplicate();
        byteBuffer.position(start).limit(end);
        return byteBuffer;
    }

    private boolean correct(int xLevel, int yLevel) {
        if(!(xLevel <= xStart && xLevel <= xEnd)) {
            return false;
        }
        if(!(yLevel <= yStart && yLevel <= yEnd)) {
            return false;
        }
        return true;
    }

    private void init() throws IOException {
        readMap();
        readIndex();
        readMap();
    }

    private void readMeta() throws IOException {
        RandomAccessFile metaFile = new RandomAccessFile(metaPath.toFile(), "r");
        this.xStart = metaFile.readInt();
        this.xEnd = metaFile.readInt();
        this.yStart = metaFile.readInt();
        this.yEnd = metaFile.readInt();
    }

    private void readIndex() throws IOException {
        RandomAccessFile indexFile = new RandomAccessFile(indexPath.toFile(), "r");
        FileChannel indexFileChannel = indexFile.getChannel();
        this.indexByteBuffer = indexFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, indexFile.length());
    }

    private void readMap() throws IOException {
        RandomAccessFile mapFile = new RandomAccessFile(mapPath.toFile(), "r");
        FileChannel indexFileChannel = mapFile.getChannel();
        this.mapByteBuffer = indexFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, mapFile.length());
    }
}
