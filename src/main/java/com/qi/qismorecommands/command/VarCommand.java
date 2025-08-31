package com.qi.qismorecommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.command.CommandManager;

import java.util.List;

public class VarCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // 主命令 /var
            dispatcher.register(CommandManager.literal("var")
                    // 2 级权限
                    .requires(source -> source.hasPermissionLevel(2))

                    // /var new 系列命令
                    .then(CommandManager.literal("new")
                            .then(CommandManager.argument("type", StringArgumentType.string())
                                    .then(CommandManager.argument("name", StringArgumentType.string())
                                            .executes(context -> createNewVar(context)) // 无初始值
                                            .then(CommandManager.literal("con")
                                                    .then(CommandManager.argument("value", StringArgumentType.greedyString())
                                                            .executes(context -> createNewVarWithValue(context)))
                                            )
                                            .then(CommandManager.literal("var")
                                                    .then(CommandManager.argument("sourceVar", StringArgumentType.string())
                                                            .executes(context -> createNewVarFromVar(context)))
                                            )
                                    ))
                    )

                    // /var set 系列命令
                    .then(CommandManager.literal("set")
                            .then(CommandManager.argument("name", StringArgumentType.string())
                                    .then(CommandManager.literal("con")
                                            .then(CommandManager.argument("value", StringArgumentType.greedyString())
                                                    .executes(context -> setVarWithValue(context)))
                                    )
                                    .then(CommandManager.literal("var")
                                            .then(CommandManager.argument("sourceVar", StringArgumentType.string())
                                                    .executes(context -> setVarFromVar(context)))
                                    )
                            )
                    )

                    // 其他命令
                    .then(CommandManager.literal("print")
                            .then(CommandManager.argument("name", StringArgumentType.string())
                                    .executes(VarCommand::printVar))
                    )
                    .then(CommandManager.literal("delete")
                            .then(CommandManager.argument("name", StringArgumentType.string())
                                    .executes(VarCommand::deleteVar))
                    )
                    .then(CommandManager.literal("list")
                            .executes(VarCommand::listAllVars)
                    )
            );
        });
    }

    // 创建变量（无初始值）
    private static int createNewVar(CommandContext<ServerCommandSource> context) {
        String type = StringArgumentType.getString(context, "type");
        String name = StringArgumentType.getString(context, "name");
        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (manager.createVar(name, type)) {
            context.getSource().sendFeedback(() ->
                    Text.literal("✅ 创建变量成功: " + name + " (" + type + ")"), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.literal("❌ 创建变量失败！可能已存在或类型不支持"));
            return 0;
        }
    }

    // 创建变量并设置值
    private static int createNewVarWithValue(CommandContext<ServerCommandSource> context) {
        String type = StringArgumentType.getString(context, "type");
        String name = StringArgumentType.getString(context, "name");
        String valueStr = StringArgumentType.getString(context, "value");
        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        // 先创建变量
        if (!manager.createVar(name, type)) {
            context.getSource().sendError(Text.literal("❌ 创建变量失败！可能已存在或类型不支持"));
            return 0;
        }

        // 然后设置值
        Object newValue;
        try {
            switch (type.toLowerCase()) {
                case "int":
                    newValue = Integer.parseInt(valueStr);
                    break;
                case "string":
                    newValue = valueStr;
                    break;
                case "boolean":
                    if (valueStr.equalsIgnoreCase("true") || valueStr.equals("1")) {
                        newValue = true;
                    } else if (valueStr.equalsIgnoreCase("false") || valueStr.equals("0")) {
                        newValue = false;
                    } else {
                        context.getSource().sendError(Text.literal("❌ 布尔值必须是 true/false 或 1/0"));
                        manager.deleteVar(name); // 回滚创建
                        return 0;
                    }
                    break;
                default:
                    context.getSource().sendError(Text.literal("❌ 不支持的变量类型"));
                    manager.deleteVar(name); // 回滚创建
                    return 0;
            }
        } catch (NumberFormatException e) {
            context.getSource().sendError(Text.literal("❌ 数值格式错误: " + valueStr));
            manager.deleteVar(name); // 回滚创建
            return 0;
        }

        if (manager.setVar(name, newValue)) {
            context.getSource().sendFeedback(() ->
                    Text.literal("✅ 创建并设置变量成功: " + name + " = " + newValue + " (" + type + ")"), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.literal("❌ 设置变量值失败！类型不匹配"));
            manager.deleteVar(name); // 回滚创建
            return 0;
        }
    }

    // 从其他变量创建新变量
    private static int createNewVarFromVar(CommandContext<ServerCommandSource> context) {
        String type = StringArgumentType.getString(context, "type");
        String name = StringArgumentType.getString(context, "name");
        String sourceName = StringArgumentType.getString(context, "sourceVar");
        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(sourceName)) {
            context.getSource().sendError(Text.literal("❌ 源变量不存在: " + sourceName));
            return 0;
        }

        // 先创建变量
        if (!manager.createVar(name, type)) {
            context.getSource().sendError(Text.literal("❌ 创建变量失败！可能已存在或类型不支持"));
            return 0;
        }

        // 获取源变量值并转换类型
        Object sourceValue = manager.getVar(sourceName);
        Object newValue;

        try {
            switch (type.toLowerCase()) {
                case "int":
                    if (sourceValue instanceof Integer) {
                        newValue = sourceValue;
                    } else if (sourceValue instanceof String) {
                        newValue = Integer.parseInt((String) sourceValue);
                    } else {
                        newValue = ((Boolean) sourceValue) ? 1 : 0;
                    }
                    break;
                case "string":
                    newValue = sourceValue.toString();
                    break;
                case "boolean":
                    if (sourceValue instanceof Boolean) {
                        newValue = sourceValue;
                    } else if (sourceValue instanceof Integer) {
                        newValue = (Integer) sourceValue != 0;
                    } else {
                        newValue = Boolean.parseBoolean(sourceValue.toString());
                    }
                    break;
                default:
                    context.getSource().sendError(Text.literal("❌ 不支持的变量类型"));
                    manager.deleteVar(name); // 回滚创建
                    return 0;
            }
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("❌ 类型转换失败: " + e.getMessage()));
            manager.deleteVar(name); // 回滚创建
            return 0;
        }

        if (manager.setVar(name, newValue)) {
            context.getSource().sendFeedback(() ->
                    Text.literal("✅ 从变量创建成功: " + name + " = " + newValue + " (来自: " + sourceName + ")"), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.literal("❌ 设置变量值失败！"));
            manager.deleteVar(name); // 回滚创建
            return 0;
        }
    }

    // 设置变量值
    private static int setVarWithValue(CommandContext<ServerCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        String valueStr = StringArgumentType.getString(context, "value");
        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(name)) {
            context.getSource().sendError(Text.literal("❌ 变量不存在: " + name));
            return 0;
        }

        Object currentValue = manager.getVar(name);
        Object newValue;

        try {
            switch (currentValue) {
                case Integer i -> newValue = Integer.parseInt(valueStr);
                case String s -> newValue = valueStr;
                case Boolean b -> {
                    if (valueStr.equalsIgnoreCase("true") || valueStr.equals("1")) {
                        newValue = true;
                    } else if (valueStr.equalsIgnoreCase("false") || valueStr.equals("0")) {
                        newValue = false;
                    } else {
                        context.getSource().sendError(Text.literal("❌ 布尔值必须是 true/false 或 1/0"));
                        return 0;
                    }
                }
                case null, default -> {
                    context.getSource().sendError(Text.literal("❌ 不支持的变量类型"));
                    return 0;
                }
            }
        } catch (NumberFormatException e) {
            context.getSource().sendError(Text.literal("❌ 数值格式错误: " + valueStr));
            return 0;
        }

        if (manager.setVar(name, newValue)) {
            context.getSource().sendFeedback(() ->
                    Text.literal("✅ 设置变量成功: " + name + " = " + newValue), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.literal("❌ 设置变量失败！类型不匹配"));
            return 0;
        }
    }

    // 从其他变量设置值
    private static int setVarFromVar(CommandContext<ServerCommandSource> context) {
        String targetName = StringArgumentType.getString(context, "name");
        String sourceName = StringArgumentType.getString(context, "sourceVar");
        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetName)) {
            context.getSource().sendError(Text.literal("❌ 目标变量不存在: " + targetName));
            return 0;
        }

        if (!manager.hasVar(sourceName)) {
            context.getSource().sendError(Text.literal("❌ 源变量不存在: " + sourceName));
            return 0;
        }

        Object sourceValue = manager.getVar(sourceName);
        if (manager.setVar(targetName, sourceValue)) {
            context.getSource().sendFeedback(() ->
                    Text.literal("✅ 设置变量成功: " + targetName + " = " + sourceValue), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.literal("❌ 设置变量失败！类型不匹配"));
            return 0;
        }
    }

    private static int printVar(CommandContext<ServerCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(name)) {
            context.getSource().sendError(Text.literal("❌ 变量不存在: " + name));
            return 0;
        }

        Object value = manager.getVar(name);
        context.getSource().sendFeedback(() ->
                Text.literal("📋 变量 " + name + " = " + value + " (" + value.getClass().getSimpleName() + ")"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int deleteVar(CommandContext<ServerCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (manager.deleteVar(name)) {
            context.getSource().sendFeedback(() ->
                    Text.literal("✅ 删除变量成功: " + name), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendError(Text.literal("❌ 删除变量失败！变量不存在: " + name));
            return 0;
        }
    }

    private static int listAllVars(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        List<String> varNames = manager.getAllVarNames();
        int varCount = manager.getVarCount();

        if (varCount == 0) {
            context.getSource().sendFeedback(() ->
                    Text.literal("❌ 没有创建任何变量"), false);
            return Command.SINGLE_SUCCESS;
        }

        // 发送变量数量信息
        context.getSource().sendFeedback(() ->
                Text.literal("📋 已创建 " + varCount + " 个变量:"), false);

        // 按字母顺序排序并列出所有变量
        varNames.sort(String::compareToIgnoreCase);

        for (String varName : varNames) {
            Object value = manager.getVar(varName);
            context.getSource().sendFeedback(() ->
                    Text.literal("  ▪ " + varName + " = " + value + " (" + value.getClass().getSimpleName() + ")"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}