package com.queuectl.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import com.queuectl.util.ConfigStore;

@Command(name="get",description="Get configuration value")
public class ConfigGet implements Runnable{
    @Parameters(index="0",description="Config key") private String key;

    @Override
    public void run(){
        String val=ConfigStore.get(key);
        if(val==null)
            System.out.println(key+" not found");
        else
            System.out.println(key+"="+val);
    }
}