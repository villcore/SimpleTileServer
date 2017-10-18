package com.villcore.gis;

import com.villcore.gis.tiles.server.TileFileManager;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TileFileManagerTest {
    @Test
    public void fileManagerInitTest() throws IOException {
        Path root = Paths.get("E:\\tiles9");
        int zLevel = 9;

        TileFileManager tileFileManager = new TileFileManager(zLevel, root);
        tileFileManager.init();

        for(int i = 418; i <= 424; i++) {
            int j = 192;

            System.out.println(i + ", " + j);
            byte[] bytes = tileFileManager.getTileBytes(i, j);
            if(bytes.length > 0) {
                Files.write(Paths.get("E:\\tiles9\\" + i + "-" + j + ".png"), bytes);
            }
        }
    }

    @Test
    public void mapFileTest() throws IOException {
        RandomAccessFile map = new RandomAccessFile(new File("E:\\tiles9\\9.map"), "r");

        map.seek(109851);
        byte[] bytes = new byte[24301];
        map.readFully(bytes);
        Files.write(Paths.get("E:\\tiles9\\3.1.png"), bytes);
    }
}
