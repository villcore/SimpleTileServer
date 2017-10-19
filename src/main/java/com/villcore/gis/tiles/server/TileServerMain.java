package com.villcore.gis.tiles.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.villcore.gis.tiles.server.TileServer.start;

public class TileServerMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(TileServerMain.class);

    public static void main(String[] args) {
        if(args.length != 4) {
            System.err.println("args = [tileRootPath emptyImagePath, listenPort, district] ...");
        }


        Path tileRoot = Paths.get(args[0]);
        Path emptyImage = Paths.get(args[1]);
        String listenPort = args[2];
        String district = args[3];

//        Path tileRoot = Paths.get("E:\\map_tiles\\");
//        Path emptyImage = Paths.get("E:\\map_tiles\\empty.png");
//        String listenPort = "8082";
//        String district = "beijing";

        try {
            TilePackageManager tilePackageManager = new TilePackageManager(tileRoot, emptyImage);
            tilePackageManager.init();

            //TODO 配置信息需要从文件中读取
            LOGGER.debug("tile server starting ..., listen port [{}] ...", listenPort);
            TileServer.start(district, Integer.valueOf(listenPort), tilePackageManager);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
