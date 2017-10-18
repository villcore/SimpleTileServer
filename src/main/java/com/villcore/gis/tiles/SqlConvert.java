package com.villcore.gis.tiles;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlConvert {
    public static void main(String[] args) throws IOException {
        Path src = Paths.get("E:\\亦庄部署\\pms_yz\\pms_yz\\刀闸、接地刀闸.sql");
        System.out.println(src.toFile().length());
        Path dst = Paths.get("E:\\亦庄部署\\pms_yz\\pms_yz\\刀闸、接地刀闸_correct.sql");

        Pattern pattern = Pattern.compile(",\\s{1}\\.\\d{1,2},");

        String input = " .09";
        //Matcher matcher = pattern.matcher(", .09, xvewer, .09,");

        String test = ", .06,220197, 189.3, 189.5, 10.26, 18.33, 6.8, .06, 27.7, 197, 103, .06,";


        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(src.toFile()), "GBK"));
             BufferedWriter bw = new BufferedWriter(new FileWriter(dst.toFile(), true))) {
            String line;
            StringBuilder sb = new StringBuilder();

            int lineNum = 0;
            while((line = br.readLine()) != null) {
                line = line.replace("to_date", "str_to_date");
//                System.out.println(line);
//            Files.lines(src).forEach(line -> {
//                try {
//                    System.out.println(line);
                Matcher matcher = pattern.matcher(line);

                int s = 0;
                int e = line.length();
                while(matcher.find()) {
                    int s2 = matcher.start();
                    int e2 = matcher.end();
                    System.out.println(s2 + " -> " + e2);
                    String newStr = " " + matcher.group().replace(" ", "0");
                    //System.out.println(newStr);
                    sb.append(line.substring(s, s2));
                    sb.append(newStr);
                    s = e2;
                }

                if(sb.length() == 0) {
                    sb.append(line);
                } else {
                    sb.append(line.substring(s, e));
                }
//                System.out.println(sb.toString());
//
//                int a = 0;
//                if(a == 0) {
//                    return;
//                }
              bw.write(sb.toString());
                bw.newLine();
                sb.setLength(0);
//                    //bw.newLine();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
                System.out.println(lineNum++);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
