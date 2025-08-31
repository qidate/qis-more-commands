package com.qi.qismorecommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.command.CommandManager;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryKey;

public class GetSeedCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("getseed")
                    // 2 级权限
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> getSeed(context, null))
                    .then(CommandManager.argument("varName", StringArgumentType.string())
                            .executes(context -> getSeedAndStore(context)))
            );
        });
    }

    private static int getSeed(CommandContext<ServerCommandSource> context, String varName) {
        MinecraftServer server = context.getSource().getServer();

        // 在 1.21.1 中，种子信息存储在 server 的保存属性中
        long seed = server.getSaveProperties().getGeneratorOptions().getSeed();

        if (varName != null) {
            VarManager manager = VarManager.get(server);
            // 种子可能是 long 类型，但我们的变量系统只支持 int，所以进行转换
            manager.setVar(varName, (int) seed);
        }

        context.getSource().sendFeedback(() ->
                Text.literal("🌱 世界种子: " + seed), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int getSeedAndStore(CommandContext<ServerCommandSource> context) {
        String varName = StringArgumentType.getString(context, "varName");
        return getSeed(context, varName);
    }
}