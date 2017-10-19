package com.villcore.gis.tiles.util;

import com.villcore.gis.tiles.PackageTool;

import java.io.IOException;
import java.nio.file.Paths;

public class PackageTilesTool {
    public static void main(String[] args) throws IOException {
        //args = new String[]{"E:\\map_tiles", "tiles12"};

        if(args.length != 2) {
            System.err.println("please input root, tileDir ...");
            return;
        }

        String dirName = args[1];
        String root = Paths.get(args[0], dirName).toString();

        System.out.println("root : " + root.toString());
        System.out.println("tileDir : " + dirName);

//        String dirName = "tiles12";
//        String root = "E:\\map_tiles\\" + dirName;
        int zLevel = Integer.valueOf(dirName.replace("tiles", ""));

        PackageTool packageTool = new PackageTool();

        packageTool.generateLevelPackage(
                Paths.get(root),
                Paths.get(root, zLevel + ".meta"),
                Paths.get(root, zLevel + ".index"),
                Paths.get(root, zLevel + ".map"));
    }
}
