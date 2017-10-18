package com.villcore.gis.tiles.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 切片图片管理, 负责维护目录下所有级别切片的的查找与读取
 * 线程安全的
 */
public class TilePackageManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TilePackageManager.class);

    private int minZLevel;
    private int maxZLevel;

    List<TileFileManager> tileFileManagers = new ArrayList<>();

    private byte[] empty;
    private Path emptyPath;

    private Path root;

    public TilePackageManager(Path root, Path emptyPath) {
        this.root = root;
        this.emptyPath = emptyPath;
    }

    public void init() throws IOException {
        empty = readEmptyBytes(emptyPath);
        scanTilePackage();
    }

    private void scanTilePackage() throws FileNotFoundException {
        File rootFile = root.toFile();
        if(!rootFile.exists()) {
            throw new FileNotFoundException();
        }

        if(!rootFile.isDirectory()) {
            throw new IllegalArgumentException("root path not a directory ...");
        }

        List<File> tilePackageFiles = Arrays.asList(rootFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.getName().contains(".");
            }
        }));

        tilePackageFiles.sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return parseZLevel(o1.getName()) - parseZLevel(o2.getName());
            }
        });

        minZLevel = parseZLevel(tilePackageFiles.get(0).getName());
        maxZLevel = parseZLevel(tilePackageFiles.get(tilePackageFiles.size() - 1).getName());

        tilePackageFiles.forEach(zLevelFile -> {
            try {
                System.out.println(zLevelFile.getName());
                tileFileManagers.add(createTileFileManager(zLevelFile));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }

    private TileFileManager createTileFileManager(File zLevelFile) throws IOException {
        TileFileManager tileFileManager = new TileFileManager(parseZLevel(zLevelFile.getName()), zLevelFile.toPath());
        tileFileManager.init();
        return tileFileManager;
    }

    private byte[] readEmptyBytes(Path emptyPath) throws IOException {
        File rootFile = root.toFile();
        if(!rootFile.exists()) {
            throw new FileNotFoundException();
        }

        if(!rootFile.isDirectory()) {
            throw new IllegalArgumentException("root path [" + rootFile.toString() + "] not a directory ...");
        }
        return Files.readAllBytes(emptyPath);
    }

    private int parseZLevel(String fileName) {
        return Integer.valueOf(fileName.replace("tiles", ""));
    }

    private TileFileManager getTileFileManger(int zLevel) {
        return tileFileManagers.get(zLevel);
    }

    private byte[] emptyBytes(Path emptyPath) throws IOException {
        return Files.readAllBytes(emptyPath);
    }

    public byte[] getTileToBytes(int zLevel, int xLevel, int yLevel) throws IOException {
        if(zLevel >= minZLevel && zLevel <= maxZLevel) {
            int zIndex = zLevel - minZLevel;
            if(zIndex >= tileFileManagers.size()) {
                return this.empty;
            }

            TileFileManager tileFileManager = getTileFileManger(zIndex);
            byte[] tileBytes = tileFileManager.getTileBytes(xLevel, yLevel);

            if(tileBytes.length == 0) {
                return this.empty;
            } else {
                return tileBytes;
            }
        }
        return this.empty;
    }

    public ByteBuf getTileToByteBuf(int zLevel, int xLevel, int yLevel) throws IOException {
        if(zLevel >= minZLevel && zLevel <= maxZLevel) {
            int zIndex = zLevel - minZLevel;
            if(zIndex >= tileFileManagers.size()) {
                return Unpooled.wrappedBuffer(this.empty);
            }

            TileFileManager tileFileManager = getTileFileManger(zIndex);
            ByteBuf byteBuf = tileFileManager.getTileByteBuf(xLevel, yLevel);

            if(byteBuf.capacity() == 0) {
                Unpooled.wrappedBuffer(this.empty);
            } else {
                return Unpooled.wrappedBuffer(byteBuf);
            }
        }
        return Unpooled.wrappedBuffer(this.empty);
    }
}
