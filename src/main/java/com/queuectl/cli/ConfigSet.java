package com.queuectl.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import com.queuectl.util.ConfigStore;

@Command(name="set",description="Set configuration key/value")
public class ConfigSet implements Runnable{
    @Parameters(index="0",description="Config key") private String key;
    @Parameters(index="1",description="Config value") private String value;

    @Override
    public void run(){
        ConfigStore.set(key,value);
        System.out.println("Set "+key+"="+value);
    }
}

