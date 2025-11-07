package com.queuectl.util;

import java.io.*;
import java.util.Properties;

public class ConfigStore {
    private static final String FILE;
    static {
        String jarDir;
        try {
            jarDir = new java.io.File(
                    ConfigStore.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParentFile().getAbsolutePath();
        } catch (Exception e) {
            jarDir = new java.io.File(".").getAbsolutePath();
        }
        FILE = new java.io.File(jarDir).getParent() + java.io.File.separator + "config.properties";
        //System.out.println("[DEBUG] Looking for config file at: " + FILE);

    }

    private static Properties load() {
        Properties p=new Properties();
        try(FileInputStream in=new FileInputStream(FILE)){
            p.load(in);
        }catch(IOException ignored) {}
        return p;
    }

    private static void save(Properties p){
        try(FileOutputStream out=new FileOutputStream(FILE)){
            p.store(out,null);
        }catch(IOException ignored) {}
    }
    public static String get(String key){
        Properties p=load();
        return p.getProperty(key);
    }

    public static void set(String key,String value){
        Properties p=load();
        p.setProperty(key,value);
        save(p);
    }
}
