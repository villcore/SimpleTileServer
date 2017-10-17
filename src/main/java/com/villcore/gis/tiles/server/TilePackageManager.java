package com.villcore.gis.tiles.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

    private TileFileManager getTileFileManger(int zLevel) {
        return tileFileManagers.get(zLevel);
    }

    private void buildTileFileManagers(Path rootDir) {
        //TODO 遍历rootDir
        //zLevel, meta, index, map
    }

    private byte[] emptyBytes(Path emptyPath) throws IOException {
        return Files.readAllBytes(emptyPath);
    }

    public byte[] getTile(int zLevel, int xLevel, int yLevel) {
        if(zLevel >= minZLevel && zLevel <= maxZLevel) {
            TileFileManager tileFileManager = getTileFileManger(zLevel);
            byte[] tileBytes = tileFileManager.getTile(xLevel, yLevel);

            if(tileBytes.length == 0) {
                return this.empty;
            } else {
                return tileBytes;
            }
        }
        return this.empty;
    }
}
