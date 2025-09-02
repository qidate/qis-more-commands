package com.qi.qismorecommands

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.qi.qismorecommands.command.IfCommand
import com.qi.qismorecommands.command.VarCommand
import com.qi.qismorecommands.command.CmpCommand
import src.main.kotlin.command.StrCommand
import src.main.kotlin.command.ModTellrawCommand
import com.qi.qismorecommands.command.RunCommand

class QisMoreCommands : ModInitializer {
    override fun onInitialize() {
        // 注册变量命令
        VarCommand.register()
        IfCommand.register()
        CmpCommand.register()
        StrCommand.register()
        ModTellrawCommand.register()
        RunCommand.register()

        println("QisMoreCommands 模组加载完成！")
    }

    companion object {
        const val MOD_ID: String = "qismorecommands"

        // This logger is used to write text to the console and the log file.
        // It is considered best practice to use your mod id as the logger's name.
        // That way, it's clear which mod wrote info, warnings, and errors.
        val LOGGER: Logger? = LoggerFactory.getLogger(MOD_ID)
    }
}