// ModSayCommand.java
package com.qi.qismorecommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.command.CommandManager;

public class ModSayCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("modsay")
                    .requires(source -> source.hasPermissionLevel(2)) // 需要2级权限
                    .then(CommandManager.literal("con")
                            .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                    .executes(ModSayCommand::modsayCon))
                    )
                    .then(CommandManager.literal("var")
                            .then(CommandManager.argument("varName", StringArgumentType.string())
                                    .executes(ModSayCommand::modsayVar))
                    )
            );
        });
    }

    // 处理常量消息
    private static int modsayCon(CommandContext<ServerCommandSource> context) {
        String message = StringArgumentType.getString(context, "message");

        // 直接执行原版 say 命令
        MinecraftServer server = context.getSource().getServer();
        server.getCommandManager().executeWithPrefix(
                context.getSource().withSilent().withLevel(2),
                "say " + message
        );

        return Command.SINGLE_SUCCESS;
    }

    // 处理变量消息
    private static int modsayVar(CommandContext<ServerCommandSource> context) {
        String varName = StringArgumentType.getString(context, "varName");
        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(varName)) {
            context.getSource().sendError(Text.literal("❌ 变量不存在: " + varName));
            return 0;
        }

        Object value = manager.getVar(varName);
        String message = value.toString();

        // 直接执行原版 say 命令
        server.getCommandManager().executeWithPrefix(
                context.getSource().withSilent().withLevel(2),
                "say " + message
        );

        return Command.SINGLE_SUCCESS;
    }
}