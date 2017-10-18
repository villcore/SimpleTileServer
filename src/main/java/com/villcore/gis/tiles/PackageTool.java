package com.villcore.gis.tiles;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 切片文件打包工具, 将Mapnik生成的文件打包成索引文件与map文件
 */
public class PackageTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageTool.class);

    //int x, int y, long startPos, int len
    private static final int INDEX_BLOCK_LEN = 4 + 4 + 8 + 4;

    int zLevel = 9;
    int xStart = -1, xEnd = -1, yStart = -1, yEnd = -1;
    byte[] emptyBytes = new byte[0]; //TODO
    Path path = Paths.get("tiles9");

    public void generateLevelPackage(Path zDir, Path metaPath, Path indexPath, Path mapPath) throws IOException {
        if(!zDir.toFile().isDirectory()) {
            System.err.println(path.toAbsolutePath().toString() + " not a directory ...");
            return;
        }

        List<File> xFiles = Arrays.asList(zDir.toFile().listFiles());
        xFiles.sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return parseXLevel(o1.getName()) - parseXLevel(o2.getName());
            }
        });
        xFiles.forEach(xDir -> {
            if(!xDir.isDirectory()) {
                System.err.println(path.toAbsolutePath().toString() + " not a directory ...");
                return;
            }

            int xLevel = parseXLevel(xDir.getName());
            if(xStart < 0) {
                xStart = xLevel;
            }

            if(xLevel > xEnd) {
                xEnd = xLevel;
            }

            List<File> yFiles = Arrays.asList(xDir.listFiles());
            yFiles.sort(new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return parseYLevel(o1.getName()) - parseYLevel(o2.getName());
                }
            });
            yFiles.forEach(yFile -> {
                int yLevel = parseYLevel(yFile.getName());

                if(yStart < 0) {
                    yStart = yLevel;
                }

                if(yLevel > yEnd) {
                    yEnd = yLevel;
                }
            });
        });

        assert xEnd > xStart && xStart > 0;
        assert yEnd > yStart && yStart > 0;
        LOGGER.debug("xStart = {}, xEnd = {}, yStart = {}, yEnd = {}", new Object[]{
                xStart, xEnd, yStart, yEnd
        });

        RandomAccessFile metaFile = new RandomAccessFile(metaPath.toFile(), "rw");
        metaFile.seek(0);
        metaFile.writeInt(xStart);
        metaFile.writeInt(xEnd);
        metaFile.writeInt(yStart);
        metaFile.writeInt(yEnd);


        long totalFileLen = (xEnd - xStart + 1) * (yEnd - yStart + 1) * INDEX_BLOCK_LEN;

        RandomAccessFile indexFile = new RandomAccessFile(indexPath.toFile(), "rw");
        indexFile.setLength(totalFileLen);
        System.out.println(totalFileLen);
        indexFile.seek(0);

        RandomAccessFile mapFile = new RandomAccessFile(mapPath.toFile(), "rw");
        mapFile.seek(0);

        int mapPos = 0;

        for(int x = xStart; x <= xEnd; x++) {
            for(int  y = yStart; y <= yEnd; y++) {

                Path tilePath = Paths.get(zDir.toFile().getAbsolutePath(), getXFileName(x), getYFileName(y));
                File tileFile = tilePath.toFile();

                if(!tileFile.exists()) {
                    LOGGER.info("file [{}] doesn't exist ...", tileFile.getAbsolutePath().toString());
                }

                byte[] tileBytes = Files.readAllBytes(tilePath);

                byte[] block = new byte[INDEX_BLOCK_LEN];
                ByteBuffer blockBuffer = ByteBuffer.wrap(block);

                int xPos = x - xStart;
                int yPos = y - yStart;
                long position = getIndexPosition(xPos, yPos, yEnd - yStart + 1, INDEX_BLOCK_LEN);
                int byteLen = tileBytes.length;


                blockBuffer.putInt(x);
                blockBuffer.putInt(y);
                blockBuffer.putLong(mapPos);
                blockBuffer.putInt(byteLen);

                mapFile.write(tileBytes);
                mapPos += (long) tileBytes.length;

                indexFile.seek(position);
                indexFile.write(blockBuffer.array());

                LOGGER.debug("{} -> {},{},{},  ", new Object[]{tilePath.toString(), xPos, yPos});
                LOGGER.debug("x = {}, y = {}, pos = {}, len = {}", new Object[]{x, y, mapPos - tileBytes.length, byteLen});

            }
        }
    }

    private long getIndexPosition(int xPos, int yPos, int yOffset, int blockLen) {
        return (xPos *  (yOffset) + yPos) * blockLen;
    }

    private long getTilePosition(int xPos, int yPos, int blockLen) {
        return 0;
    }

    private String getYFileName(int y) {
        return String.valueOf(y) + ".png";
    }

    private String getXFileName(int x) {
        return String.valueOf(x);
    }

    private int parseYLevel(String name) {
        return Integer.valueOf(name.split("\\.")[0]);
    }

    private int parseXLevel(String name) {
        return Integer.valueOf(name);
    }

    public FilePosition getTilePosition(RandomAccessFile indexFile, int yOffset, int x, int y) throws IOException {
        long indexPos = getIndexPosition(x, y, yOffset, INDEX_BLOCK_LEN);
        byte[] bytes = new byte[INDEX_BLOCK_LEN];

        indexFile.seek(indexPos);
        indexFile.readFully(bytes);

        ByteBuffer indexBlockBuffer = ByteBuffer.wrap(bytes);
        long start = indexBlockBuffer.getLong(4 + 4);
        int len = indexBlockBuffer.getInt(4 + 4 + 8);

        LOGGER.debug("x = {}, y = {}, index pos = {}, start = {}, len = {}", new Object[]{x, y, indexPos, start, len});
        return new FilePosition(start, len);
    }

    public byte[] getTileBytes(RandomAccessFile mapFile, FilePosition filePosition) throws IOException {
        LOGGER.debug("file pos = {}, {}", filePosition.start, filePosition.len);
        byte[] bytes = new byte[filePosition.len];

        mapFile.seek(filePosition.start);
        mapFile.readFully(bytes);
        return bytes;
    }

    public static void main(String[] args) throws IOException {
        PackageTool packageTool = new PackageTool();
//        packageTool.generateLevelPackage(
//                Paths.get("E:\\tiles9"),
//                Paths.get("E:\\tiles9\\9.meta"),
//                Paths.get("E:\\tiles9\\9.index"),
//                Paths.get("E:\\tiles9\\9.map"));


        for(int i = 0; i <= 6; i++) {
            for(int j = 0; j <= 4; j++) {
                FilePosition filePosition = packageTool.getTilePosition(new RandomAccessFile(Paths.get("E:\\tiles9\\9.index").toFile(), "rw"), 5, i, j);
                byte[] bytes = packageTool.getTileBytes(new RandomAccessFile(Paths.get("E:\\tiles9\\9.map").toFile(), "rw"), filePosition);
                Files.write(Paths.get("E:\\tiles9\\" + i + "-" + j + ".png"), bytes, StandardOpenOption.CREATE_NEW);
            }
        }
        //System.out.println("a.png".split("\\.")[0]);
    }
}
