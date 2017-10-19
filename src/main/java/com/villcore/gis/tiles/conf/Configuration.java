//package com.villcore.gis.tiles.conf;
//
//import org.apache.commons.cli.*;
//import org.apache.commons.configuration.PropertiesConfiguration;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Properties;
//
//public class Configuration {
//    public static void load() throws ParseException {
//        String confFileName = "conf_file";
//
//        //district
//        //map_tiles
//        //empty.png
//        //listenPort
//        //index cache
//        //map cache
//
//        String consumerTaskSizeName = "consumer_task_size";
//        String redisTaskSizeName = "redis_task_size";
//        String hbaseTaskSizeName = "hbase_task_size";
//        String absPathName = "abs_path";
//
//        Option confOpt = Option.builder(confFileName).required(true).longOpt("configuration file path").numberOfArgs(1).type(String.class).build();
//        Option consumerSizeOpt = Option.builder(consumerTaskSizeName).required(true).longOpt("kafka consumer task size").numberOfArgs(1).type(String.class).build();
//        Option redisSizeOpt = Option.builder(redisTaskSizeName).required(true).longOpt("redis task size").numberOfArgs(1).type(String.class).build();
//        Option hbaseSizeOpt = Option.builder(hbaseTaskSizeName).required(true).longOpt("hbase task size").numberOfArgs(1).type(String.class).build();
//        Option absPathOpt = Option.builder(absPathName).required(true).longOpt("conf path is absolute path").numberOfArgs(1).type(String.class).build();
//
//        Options options = new Options();
//        options.addOption(confOpt);
//        options.addOption(consumerSizeOpt);
//        options.addOption(redisSizeOpt);
//        options.addOption(hbaseSizeOpt);
//        options.addOption(absPathOpt);
//
//        CommandLineParser parser = new DefaultParser();
//        CommandLine cmd = parser.parse(options, args);
//
//        String confilePath = cmd.getParsedOptionValue(confFileName).toString();
//        boolean isAbsolutePath = Boolean.valueOf(cmd.getParsedOptionValue(absPathName).toString());
//        int consumerTaskSize = Integer.valueOf(cmd.getParsedOptionValue(consumerTaskSizeName).toString());
//        int redisTaskSize = Integer.valueOf(cmd.getParsedOptionValue(redisTaskSizeName).toString());
//        int hbaseTaskSize = Integer.valueOf(cmd.getParsedOptionValue(hbaseTaskSizeName).toString());
//
//        String confAbsolutePath = null;
//        if (isAbsolutePath) {
//            confAbsolutePath = confilePath;
//        } else {
//            confAbsolutePath = new File("").getAbsolutePath() + File.separator + confilePath;
//        }
//
//        //读取配置文件, kafka, redis, hbase
//        //Configurations configs = new Configurations();
//        File properityFile = new File(confAbsolutePath);
//        if (!properityFile.exists()) {
//            System.out.printf("conf_file [%s] not exist...\n", confAbsolutePath);
//            return;
//        } else {
//            try (InputStream is = new FileInputStream(properityFile)) {
//                Properties prop = new Properties();
//                prop.load(is);
//                for (Map.Entry<Object, Object> entry : prop.entrySet()) {
//                    System.out.printf("%s -> %s\n", entry.getKey(), entry.getValue());
//                }
//            } catch (IOException e) {
//                LOG.error(e.getMessage(), e);
//            }
//        }
//
//        org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(properityFile);
//        //consumer queue size
//        int saveQueueSize = config.getInt("save.queue.size", 10 * 1000);
//        String os = System.getProperty("os.name");
//        if(os.toLowerCase().startsWith("win")){
//            System.setProperty("hadoop.home.dir",
//                    Objects.requireNonNull(config.getString("hadoop.home.dir"),
//                            "windows hadoop.home.dir not set..."));
//        }
//    }
//}
