package com.qi.qismorecommands;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qi.qismorecommands.command.VarCommand;
import com.qi.qismorecommands.command.GetSeedCommand;
import com.qi.qismorecommands.command.StringMakerCommand;
import com.qi.qismorecommands.command.ModSayCommand;

public class QisMoreCommands implements ModInitializer {
	public static final String MOD_ID = "qismorecommands";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // 注册变量命令
        VarCommand.register();
        GetSeedCommand.register();
        StringMakerCommand.register();
        ModSayCommand.register();

        System.out.println("QisMoreCommands 模组加载完成！");
    }
}