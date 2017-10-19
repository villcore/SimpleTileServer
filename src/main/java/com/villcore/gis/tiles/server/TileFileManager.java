package com.villcore.gis.tiles.server;

import com.villcore.gis.tiles.FilePosition;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TileFileManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TileFileManager.class);

    private static final byte[] ZERO_BYTES_ARRAY = new byte[0];
    private static final int INDEX_BLOCK_LEN = 4 + 4 + 8 + 4;

    private Path curRoot;
    private int zLevel;

    private Path metaPath;
    private Path indexPath;
    private Path mapPath;

    private int xStart;
    private int xEnd;
    private int yStart;
    private int yEnd;

    private ByteBuffer indexByteBuffer;
    private ByteBuffer mapByteBuffer;

    private boolean indexCache = true;
    private boolean mapCache = true;

    public TileFileManager(int zLevel, Path curRoot) {
        this.zLevel = zLevel;
        this.curRoot = curRoot;

        this.metaPath = Paths.get(curRoot.toString(), zLevel + ".meta");
        this.indexPath = Paths.get(curRoot.toString(), zLevel + ".index");
        this.mapPath = Paths.get(curRoot.toString(), zLevel + ".map");
    }

    public void init() throws IOException {
        readMeta();
        readIndex();
        readMap();
    }

    public byte[] getTileBytes(int xLevel, int yLevel) throws IOException {
        System.out.println(correct(xLevel, yLevel));
        if (!correct(xLevel, yLevel)) {
            return ZERO_BYTES_ARRAY;
        }


        FilePosition filePosition = getTilePosition(xLevel - xStart, yLevel - yStart);
        return getTileToBytes(filePosition);
    }

    public ByteBuf getTileByteBuf(int xLevel, int yLevel) throws IOException {
        if (!correct(xLevel, yLevel)) {
            return Unpooled.wrappedBuffer(ZERO_BYTES_ARRAY);
        }

        int x = xLevel - xStart;
        int y = yLevel - yStart;
        FilePosition filePosition = getTilePosition(x, y);
        return Unpooled.wrappedBuffer(getTileToBuffer(filePosition));
    }

    private byte[] getTileToBytes(FilePosition filePosition) {
        byte[] bytes = new byte[filePosition.len];
        mapByteBuffer.position((int) filePosition.start);
        mapByteBuffer.get(bytes);
        mapByteBuffer.position(0);
        return bytes;
    }

    private ByteBuffer getTileToBuffer(FilePosition filePosition) {
        int start = (int) filePosition.start;
        int end = (int) (filePosition.start + filePosition.len);
        ByteBuffer byteBuffer = mapByteBuffer.duplicate();
        byteBuffer.position(start).limit(end);
        return byteBuffer;
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

//        LOGGER.debug("start = {}, len = {}", indexByteBuffer.position(), indexByteBuffer.limit());
        LOGGER.debug("X = {}, Y = {}", x, y);

        return new FilePosition(indexByteBuffer.getLong((int) (indexPos + 4 + 4)), indexByteBuffer.getInt((int) (indexPos + 4 + 4 + 8)));
    }

    private long getIndexPosition(int xPos, int yPos, int blockLen) {
        return (xPos * (yEnd - yStart + 1) + yPos) * blockLen;
    }

    private boolean correct(int xLevel, int yLevel) {
        if (!(xLevel >= xStart && xLevel <= xEnd)) {
            return false;
        }
        if (!(yLevel >= yStart && yLevel <= yEnd)) {
            return false;
        }
        return true;
    }

    private void readMeta() throws IOException {
        RandomAccessFile metaFile = new RandomAccessFile(metaPath.toFile(), "r");
        this.xStart = metaFile.readInt();
        this.xEnd = metaFile.readInt();
        this.yStart = metaFile.readInt();
        this.yEnd = metaFile.readInt();

        LOGGER.debug("read meta, xStart = {}, xEnd = {}, yStart = {}, yEnd = {}", new Object[]{
                xStart, xEnd, yStart, yEnd
        });
    }

    private void readIndex() throws IOException {
        RandomAccessFile indexFile = new RandomAccessFile(indexPath.toFile(), "r");
        FileChannel indexFileChannel = indexFile.getChannel();
        this.indexByteBuffer = indexFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, indexFile.length());
        LOGGER.debug("file size = {}, indexBuffer pos = {}, limit = {}", indexFile.length(), indexByteBuffer.position(), indexByteBuffer.limit());
        if(indexCache) {
            this.indexByteBuffer = cacheByteBuffer(this.indexByteBuffer);
        }
    }

    private void readMap() throws IOException {
        RandomAccessFile mapFile = new RandomAccessFile(mapPath.toFile(), "r");
        FileChannel indexFileChannel = mapFile.getChannel();
        this.mapByteBuffer = indexFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, mapFile.length());
        if (mapCache) {
            this.mapByteBuffer = cacheByteBuffer(this.mapByteBuffer);
        }
    }

    private ByteBuffer cacheByteBuffer(ByteBuffer src) {
        int capcity = src.capacity();
        ByteBuffer memByteBuffer = ByteBuffer.allocateDirect(capcity);

        memByteBuffer.put(src);
        return memByteBuffer;
    }

    public void close() {

    }
}
