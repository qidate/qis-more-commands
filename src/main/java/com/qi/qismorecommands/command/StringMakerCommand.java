// StringMakerCommand.java
package com.qi.qismorecommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.command.CommandManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class StringMakerCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("stringmaker")
                    // 2 级权限
                    .requires(source -> source.hasPermissionLevel(2))

                    // join 命令
                    .then(CommandManager.literal("join")
                            .then(CommandManager.argument("targetVar", StringArgumentType.string())
                                    .then(CommandManager.literal("con")
                                            .then(CommandManager.argument("str1", StringArgumentType.string())
                                                    .then(CommandManager.argument("str2", StringArgumentType.string())
                                                            .executes(StringMakerCommand::joinStringsConToVar)))
                                    )
                                    .then(CommandManager.literal("var")
                                            .then(CommandManager.argument("var1", StringArgumentType.string())
                                                    .then(CommandManager.argument("var2", StringArgumentType.string())
                                                            .executes(StringMakerCommand::joinStringsVarToVar)))
                                    )
                                    .then(CommandManager.literal("mix")
                                            .then(CommandManager.literal("v2c")
                                                    .then(CommandManager.argument("var", StringArgumentType.string())
                                                            .then(CommandManager.argument("str", StringArgumentType.string())
                                                                    .executes(StringMakerCommand::joinV2CToVar)))
                                            )
                                            .then(CommandManager.literal("c2v")
                                                    .then(CommandManager.argument("str", StringArgumentType.string())
                                                            .then(CommandManager.argument("var", StringArgumentType.string())
                                                                    .executes(StringMakerCommand::joinC2VToVar)))
                                            )
                                    )
                            )
                    )

                    // case 命令 - 大小写转换
                    .then(CommandManager.literal("case")
                            // upper 大写转换
                            .then(CommandManager.literal("upper")
                                    .then(CommandManager.argument("targetVar", StringArgumentType.string())
                                            .then(CommandManager.literal("con")
                                                    .then(CommandManager.argument("str", StringArgumentType.greedyString())
                                                            .executes(StringMakerCommand::toUpperCaseConToVar)))
                                            .then(CommandManager.literal("var")
                                                    .then(CommandManager.argument("var", StringArgumentType.string())
                                                            .executes(StringMakerCommand::toUpperCaseVarToVar)))
                                    )
                            )
                            // lower 小写转换
                            .then(CommandManager.literal("lower")
                                    .then(CommandManager.argument("targetVar", StringArgumentType.string())
                                            .then(CommandManager.literal("con")
                                                    .then(CommandManager.argument("str", StringArgumentType.greedyString())
                                                            .executes(StringMakerCommand::toLowerCaseConToVar)))
                                            .then(CommandManager.literal("var")
                                                    .then(CommandManager.argument("var", StringArgumentType.string())
                                                            .executes(StringMakerCommand::toLowerCaseVarToVar)))
                                    )
                            )
                    )

                    .then(CommandManager.literal("random")
                            // letters 随机字母
                            .then(CommandManager.literal("letters")
                                    .then(CommandManager.argument("targetVar", StringArgumentType.string())
                                            .then(CommandManager.literal("con")
                                                    .then(CommandManager.argument("length", IntegerArgumentType.integer(1))
                                                            .executes(StringMakerCommand::randomLettersConToVar)))
                                            .then(CommandManager.literal("var")
                                                    .then(CommandManager.argument("lengthVar", StringArgumentType.string())
                                                            .executes(StringMakerCommand::randomLettersVarToVar)))
                                    )
                            )
                            // numbers 随机数字
                            .then(CommandManager.literal("numbers")
                                    .then(CommandManager.argument("targetVar", StringArgumentType.string())
                                            .then(CommandManager.literal("con")
                                                    .then(CommandManager.argument("length", IntegerArgumentType.integer(1))
                                                            .executes(StringMakerCommand::randomNumbersConToVar)))
                                            .then(CommandManager.literal("var")
                                                    .then(CommandManager.argument("lengthVar", StringArgumentType.string())
                                                            .executes(StringMakerCommand::randomNumbersVarToVar)))
                                    )
                            )
                            // mixed 随机混合
                            .then(CommandManager.literal("mixed")
                                    .then(CommandManager.argument("targetVar", StringArgumentType.string())
                                            .then(CommandManager.literal("con")
                                                    .then(CommandManager.argument("length", IntegerArgumentType.integer(1))
                                                            .executes(StringMakerCommand::randomMixedConToVar)))
                                            .then(CommandManager.literal("var")
                                                    .then(CommandManager.argument("lengthVar", StringArgumentType.string())
                                                            .executes(StringMakerCommand::randomMixedVarToVar)))
                                    )
                            )
                            // chars 自定义字符集
                            .then(CommandManager.literal("chars")
                                    .then(CommandManager.argument("targetVar", StringArgumentType.string())
                                            // con con
                                            .then(CommandManager.literal("con")
                                                    .then(CommandManager.argument("charset", StringArgumentType.string())
                                                            .then(CommandManager.literal("con")
                                                                    .then(CommandManager.argument("length", IntegerArgumentType.integer(1))
                                                                            .executes(StringMakerCommand::randomCharsConConToVar)))
                                                            .then(CommandManager.literal("var")
                                                                    .then(CommandManager.argument("lengthVar", StringArgumentType.string())
                                                                            .executes(StringMakerCommand::randomCharsConVarToVar)))
                                                    )
                                                    // var con
                                                    .then(CommandManager.literal("var")
                                                            .then(CommandManager.argument("charsetVar", StringArgumentType.string())
                                                                    .then(CommandManager.literal("con")
                                                                            .then(CommandManager.argument("length", IntegerArgumentType.integer(1))
                                                                                    .executes(StringMakerCommand::randomCharsVarConToVar)))
                                                                    .then(CommandManager.literal("var")
                                                                            .then(CommandManager.argument("lengthVar", StringArgumentType.string())
                                                                                    .executes(StringMakerCommand::randomCharsVarVarToVar)))
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )


                    .then(CommandManager.literal("num2cn")
                            // simple 简单转换
                            .then(CommandManager.literal("simple")
                                    .then(CommandManager.argument("targetVar", StringArgumentType.string())
                                            .then(CommandManager.literal("con")
                                                    .then(CommandManager.argument("number", StringArgumentType.greedyString())
                                                            .executes(StringMakerCommand::num2cnSimpleConToVar)))
                                            .then(CommandManager.literal("var")
                                                    .then(CommandManager.argument("numberVar", StringArgumentType.string())
                                                            .executes(StringMakerCommand::num2cnSimpleVarToVar)))
                                    )
                            )
                            // capital 大写转换
                            .then(CommandManager.literal("capital")
                                    .then(CommandManager.argument("targetVar", StringArgumentType.string())
                                            .then(CommandManager.literal("con")
                                                    .then(CommandManager.argument("number", StringArgumentType.greedyString())
                                                            .executes(StringMakerCommand::num2cnCapitalConToVar)))
                                            .then(CommandManager.literal("var")
                                                    .then(CommandManager.argument("numberVar", StringArgumentType.string())
                                                            .executes(StringMakerCommand::num2cnCapitalVarToVar)))
                                    )
                            )
                            // normal 标准单位转换
                            .then(CommandManager.literal("normal")
                                    .then(CommandManager.argument("targetVar", StringArgumentType.string())
                                            .then(CommandManager.literal("con")
                                                    .then(CommandManager.argument("number", StringArgumentType.greedyString())
                                                            .executes(StringMakerCommand::num2cnNormalConToVar)))
                                            .then(CommandManager.literal("var")
                                                    .then(CommandManager.argument("numberVar", StringArgumentType.string())
                                                            .executes(StringMakerCommand::num2cnNormalVarToVar)))
                                    )
                            )
                            // financial 财务大写转换
                            .then(CommandManager.literal("financial")
                                    .then(CommandManager.argument("targetVar", StringArgumentType.string())
                                            .then(CommandManager.literal("con")
                                                    .then(CommandManager.argument("number", StringArgumentType.greedyString())
                                                            .executes(StringMakerCommand::num2cnFinancialConToVar)))
                                            .then(CommandManager.literal("var")
                                                    .then(CommandManager.argument("numberVar", StringArgumentType.string())
                                                            .executes(StringMakerCommand::num2cnFinancialVarToVar)))
                                    )
                            )
                    )
            );
        });
    }

    // ========== JOIN 方法 ==========
    private static int joinStringsConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String str1 = StringArgumentType.getString(context, "str1");
        String str2 = StringArgumentType.getString(context, "str2");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.joinStrings(str1, str2);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("✅ 字符串合并成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int joinStringsVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String var1 = StringArgumentType.getString(context, "var1");
        String var2 = StringArgumentType.getString(context, "var2");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(var1) || !manager.hasVar(var2)) {
            context.getSource().sendError(Text.literal("❌ 源变量不存在"));
            return 0;
        }

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        Object val1 = manager.getVar(var1);
        Object val2 = manager.getVar(var2);
        String result = StringMaker.joinStrings(val1.toString(), val2.toString());

        manager.setVar(targetVar, result);
        context.getSource().sendFeedback(() ->
                Text.literal("✅ 变量合并成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // 变量在前，字符串在后 (v2c)
    private static int joinV2CToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String sourceVar = StringArgumentType.getString(context, "var");
        String str = StringArgumentType.getString(context, "str");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(sourceVar)) {
            context.getSource().sendError(Text.literal("❌ 源变量不存在: " + sourceVar));
            return 0;
        }

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        Object varValue = manager.getVar(sourceVar);
        String result = StringMaker.joinStrings(varValue.toString(), str);

        manager.setVar(targetVar, result);
        context.getSource().sendFeedback(() ->
                Text.literal("✅ 变量+字符串合并成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // 字符串在前，变量在后 (c2v)
    private static int joinC2VToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String str = StringArgumentType.getString(context, "str");
        String sourceVar = StringArgumentType.getString(context, "var");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(sourceVar)) {
            context.getSource().sendError(Text.literal("❌ 源变量不存在: " + sourceVar));
            return 0;
        }

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        Object varValue = manager.getVar(sourceVar);
        String result = StringMaker.joinStrings(str, varValue.toString());

        manager.setVar(targetVar, result);
        context.getSource().sendFeedback(() ->
                Text.literal("✅ 字符串+变量合并成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // ========== UPPER CASE 方法 ==========
    private static int toUpperCaseConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String str = StringArgumentType.getString(context, "str");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.toUpperCase(str);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("✅ 大写转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int toUpperCaseVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String sourceVar = StringArgumentType.getString(context, "var");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(sourceVar)) {
            context.getSource().sendError(Text.literal("❌ 源变量不存在: " + sourceVar));
            return 0;
        }

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        Object value = manager.getVar(sourceVar);
        String result = StringMaker.toUpperCase(value.toString());

        manager.setVar(targetVar, result);
        context.getSource().sendFeedback(() ->
                Text.literal("✅ 变量大写转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // ========== LOWER CASE 方法 ==========
    private static int toLowerCaseConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String str = StringArgumentType.getString(context, "str");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.toLowerCase(str);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("✅ 小写转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int toLowerCaseVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String sourceVar = StringArgumentType.getString(context, "var");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(sourceVar)) {
            context.getSource().sendError(Text.literal("❌ 源变量不存在: " + sourceVar));
            return 0;
        }

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        Object value = manager.getVar(sourceVar);
        String result = StringMaker.toLowerCase(value.toString());

        manager.setVar(targetVar, result);
        context.getSource().sendFeedback(() ->
                Text.literal("✅ 变量小写转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }


    // ========== RANDOM 方法 ==========
    // letters 方法
    private static int randomLettersConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        int length = IntegerArgumentType.getInteger(context, "length");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.randomLetters(length);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("✅ 随机字母生成成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int randomLettersVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String lengthVar = StringArgumentType.getString(context, "lengthVar");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(lengthVar)) {
            context.getSource().sendError(Text.literal("❌ 长度变量不存在: " + lengthVar));
            return 0;
        }

        Object lengthObj = manager.getVar(lengthVar);
        if (!(lengthObj instanceof Integer)) {
            context.getSource().sendError(Text.literal("❌ 长度变量必须是整数类型"));
            return 0;
        }

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        int length = (Integer) lengthObj;
        String result = StringMaker.randomLetters(length);

        manager.setVar(targetVar, result);
        context.getSource().sendFeedback(() ->
                Text.literal("✅ 随机字母生成成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // numbers 方法（类似 letters，使用 StringMaker.randomNumbers()）
    private static int randomNumbersConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        int length = IntegerArgumentType.getInteger(context, "length");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.randomNumbers(length);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("✅ 随机数字生成成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int randomNumbersVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String lengthVar = StringArgumentType.getString(context, "lengthVar");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(lengthVar)) {
            context.getSource().sendError(Text.literal("❌ 长度变量不存在: " + lengthVar));
            return 0;
        }

        Object lengthObj = manager.getVar(lengthVar);
        if (!(lengthObj instanceof Integer)) {
            context.getSource().sendError(Text.literal("❌ 长度变量必须是整数类型"));
            return 0;
        }

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        int length = (Integer) lengthObj;
        String result = StringMaker.randomNumbers(length);

        manager.setVar(targetVar, result);
        context.getSource().sendFeedback(() ->
                Text.literal("✅ 随机数字生成成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
        // 实现类似 randomLettersVarToVar，使用 StringMaker.randomNumbers()
    }

    // mixed 方法（类似 letters，使用 StringMaker.randomMixed()）
    private static int randomMixedConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        int length = IntegerArgumentType.getInteger(context, "length");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.randomMixed(length);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("✅ 混合随机生成成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int randomMixedVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String lengthVar = StringArgumentType.getString(context, "lengthVar");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(lengthVar)) {
            context.getSource().sendError(Text.literal("❌ 长度变量不存在: " + lengthVar));
            return 0;
        }

        Object lengthObj = manager.getVar(lengthVar);
        if (!(lengthObj instanceof Integer)) {
            context.getSource().sendError(Text.literal("❌ 长度变量必须是整数类型"));
            return 0;
        }

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        int length = (Integer) lengthObj;
        String result = StringMaker.randomMixed(length);

        manager.setVar(targetVar, result);
        context.getSource().sendFeedback(() ->
                Text.literal("✅ 混合随机生成成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // chars 自定义字符集方法
    private static int randomCharsConConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String charset = StringArgumentType.getString(context, "charset");
        int length = IntegerArgumentType.getInteger(context, "length");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.randomChars(charset, length);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("✅ 自定义随机字符串生成成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // randomCharsConVarToVar: 字符集为常量，长度为变量
    private static int randomCharsConVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String charset = StringArgumentType.getString(context, "charset");
        String lengthVar = StringArgumentType.getString(context, "lengthVar");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(lengthVar)) {
            context.getSource().sendError(Text.literal("❌ 长度变量不存在: " + lengthVar));
            return 0;
        }

        Object lengthObj = manager.getVar(lengthVar);
        if (!(lengthObj instanceof Integer)) {
            context.getSource().sendError(Text.literal("❌ 长度变量必须是整数类型"));
            return 0;
        }

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        int length = (Integer) lengthObj;
        String result = StringMaker.randomChars(charset, length);

        manager.setVar(targetVar, result);
        context.getSource().sendFeedback(() ->
                Text.literal("✅ 自定义随机字符串生成成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // randomCharsVarConToVar: 字符集为变量，长度为常量
    private static int randomCharsVarConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String charsetVar = StringArgumentType.getString(context, "charsetVar");
        int length = IntegerArgumentType.getInteger(context, "length");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(charsetVar)) {
            context.getSource().sendError(Text.literal("❌ 字符集变量不存在: " + charsetVar));
            return 0;
        }

        Object charsetObj = manager.getVar(charsetVar);
        String charset = charsetObj.toString();

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.randomChars(charset, length);

        manager.setVar(targetVar, result);
        context.getSource().sendFeedback(() ->
                Text.literal("✅ 自定义随机字符串生成成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // randomCharsVarVarToVar: 字符集和长度都为变量
    private static int randomCharsVarVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String charsetVar = StringArgumentType.getString(context, "charsetVar");
        String lengthVar = StringArgumentType.getString(context, "lengthVar");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(charsetVar)) {
            context.getSource().sendError(Text.literal("❌ 字符集变量不存在: " + charsetVar));
            return 0;
        }

        if (!manager.hasVar(lengthVar)) {
            context.getSource().sendError(Text.literal("❌ 长度变量不存在: " + lengthVar));
            return 0;
        }

        Object charsetObj = manager.getVar(charsetVar);
        String charset = charsetObj.toString();

        Object lengthObj = manager.getVar(lengthVar);
        if (!(lengthObj instanceof Integer)) {
            context.getSource().sendError(Text.literal("❌ 长度变量必须是整数类型"));
            return 0;
        }

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        int length = (Integer) lengthObj;
        String result = StringMaker.randomChars(charset, length);

        manager.setVar(targetVar, result);
        context.getSource().sendFeedback(() ->
                Text.literal("✅ 自定义随机字符串生成成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }



    // ========== NUM2CN 方法 ==========
// simple 简单转换
    private static int num2cnSimpleConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String number = StringArgumentType.getString(context, "number");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.numberToChineseSimple(number);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("🔢 简单中文数字转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int num2cnSimpleVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String numberVar = StringArgumentType.getString(context, "numberVar");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(numberVar)) {
            context.getSource().sendError(Text.literal("❌ 数字变量不存在: " + numberVar));
            return 0;
        }

        Object numberObj = manager.getVar(numberVar);
        String number = numberObj.toString();

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.numberToChineseSimple(number);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("🔢 简单中文数字转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // capital 大写转换
    private static int num2cnCapitalConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String number = StringArgumentType.getString(context, "number");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.numberToChineseCapital(number);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("🔢 大写中文数字转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int num2cnCapitalVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String numberVar = StringArgumentType.getString(context, "numberVar");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(numberVar)) {
            context.getSource().sendError(Text.literal("❌ 数字变量不存在: " + numberVar));
            return 0;
        }

        Object numberObj = manager.getVar(numberVar);
        String number = numberObj.toString();

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.numberToChineseCapital(number);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("🔢 大写中文数字转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // normal 标准单位转换
    private static int num2cnNormalConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String number = StringArgumentType.getString(context, "number");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.numberToChineseNormal(number);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("🔢 标准中文数字转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int num2cnNormalVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String numberVar = StringArgumentType.getString(context, "numberVar");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(numberVar)) {
            context.getSource().sendError(Text.literal("❌ 数字变量不存在: " + numberVar));
            return 0;
        }

        Object numberObj = manager.getVar(numberVar);
        String number = numberObj.toString();

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.numberToChineseNormal(number);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("🔢 标准中文数字转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    // financial 财务大写转换
    private static int num2cnFinancialConToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String number = StringArgumentType.getString(context, "number");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.numberToChineseFinancial(number);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("💰 财务中文数字转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int num2cnFinancialVarToVar(CommandContext<ServerCommandSource> context) {
        String targetVar = StringArgumentType.getString(context, "targetVar");
        String numberVar = StringArgumentType.getString(context, "numberVar");

        MinecraftServer server = context.getSource().getServer();
        VarManager manager = VarManager.get(server);

        if (!manager.hasVar(numberVar)) {
            context.getSource().sendError(Text.literal("❌ 数字变量不存在: " + numberVar));
            return 0;
        }

        Object numberObj = manager.getVar(numberVar);
        String number = numberObj.toString();

        if (!manager.hasVar(targetVar)) {
            manager.createVar(targetVar, "string");
        }

        String result = StringMaker.numberToChineseFinancial(number);
        manager.setVar(targetVar, result);

        context.getSource().sendFeedback(() ->
                Text.literal("💰 财务中文数字转换成功: " + targetVar + " = " + result), false);
        return Command.SINGLE_SUCCESS;
    }
}

