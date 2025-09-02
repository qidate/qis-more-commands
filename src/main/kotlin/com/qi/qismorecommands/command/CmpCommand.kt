package com.qi.qismorecommands.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object CmpCommand {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("cmp")
                    .requires { it.hasPermissionLevel(2) }
                    .then(
                        CommandManager.argument<String>("targetVar", StringArgumentType.string())
                            .then(
                                CommandManager.argument<String>("operator", StringArgumentType.string())
                                    .then(
                                        CommandManager.literal("con")
                                            .then(
                                                CommandManager.argument<String>("num1", StringArgumentType.string())
                                                    .then(
                                                        CommandManager.argument<String>("num2", StringArgumentType.string())
                                                            .executes { compareCon(it) }
                                                    )
                                            )
                                    )
                                    .then(
                                        CommandManager.literal("var")
                                            .then(
                                                CommandManager.argument<String>("var1", StringArgumentType.string())
                                                    .then(
                                                        CommandManager.argument<String>("var2", StringArgumentType.string())
                                                            .executes { compareVar(it) }
                                                    )
                                            )
                                    )
                                    .then(
                                        CommandManager.literal("v2c")
                                            .then(
                                                CommandManager.argument<String>("var", StringArgumentType.string())
                                                    .then(
                                                        CommandManager.argument<String>("num", StringArgumentType.string())
                                                            .executes { compareVarToCon(it) }
                                                    )
                                            )
                                    )
                                    .then(
                                        CommandManager.literal("c2v")
                                            .then(
                                                CommandManager.argument<String>("num", StringArgumentType.string())
                                                    .then(
                                                        CommandManager.argument<String>("var", StringArgumentType.string())
                                                            .executes { compareConToVar(it) }
                                                    )
                                            )
                                    )
                            )
                    )
            )
        }
    }

    private fun compareCon(context: CommandContext<ServerCommandSource>): Int {
        val targetVarName = StringArgumentType.getString(context, "targetVar")
        val operator = StringArgumentType.getString(context, "operator")
        val num1Str = StringArgumentType.getString(context, "num1")
        val num2Str = StringArgumentType.getString(context, "num2")

        return try {
            val num1 = num1Str.toDouble()
            val num2 = num2Str.toDouble()
            val result = performComparison(operator, num1, num2)
            setBooleanResult(context, targetVarName, result)
        } catch (e: NumberFormatException) {
            context.source.sendError(Text.literal("❌ 数字格式错误: ${e.message}"))
            0
        } catch (e: IllegalArgumentException) {
            context.source.sendError(Text.literal("❌ ${e.message}"))
            0
        }
    }

    private fun compareVar(context: CommandContext<ServerCommandSource>): Int {
        val targetVarName = StringArgumentType.getString(context, "targetVar")
        val operator = StringArgumentType.getString(context, "operator")
        val var1Name = StringArgumentType.getString(context, "var1")
        val var2Name = StringArgumentType.getString(context, "var2")

        val manager = VarManager.get(context.source.server)
        val source = context.source

        try {
            // 获取第一个变量值
            val var1Info = parseVariable(var1Name, source)
            val value1 = var1Info.scope?.let { manager?.getVar(it, var1Info.playerName, var1Info.varName) }
                ?: throw IllegalArgumentException("变量不存在: $var1Name")

            // 获取第二个变量值
            val var2Info = parseVariable(var2Name, source)
            val value2 = var2Info.scope?.let { manager?.getVar(it, var2Info.playerName, var2Info.varName) }
                ?: throw IllegalArgumentException("变量不存在: $var2Name")

            // 转换为数字
            val num1 = value1.toString().toDouble()
            val num2 = value2.toString().toDouble()

            val result = performComparison(operator, num1, num2)
            return setBooleanResult(context, targetVarName, result)
        } catch (e: NumberFormatException) {
            context.source.sendError(Text.literal("❌ 变量值不是有效的数字"))
            return 0
        } catch (e: IllegalArgumentException) {
            context.source.sendError(Text.literal("❌ ${e.message}"))
            return 0
        }
    }

    private fun compareVarToCon(context: CommandContext<ServerCommandSource>): Int {
        val targetVarName = StringArgumentType.getString(context, "targetVar")
        val operator = StringArgumentType.getString(context, "operator")
        val varName = StringArgumentType.getString(context, "var")
        val numStr = StringArgumentType.getString(context, "num")

        val manager = VarManager.get(context.source.server)
        val source = context.source

        try {
            // 获取变量值
            val varInfo = parseVariable(varName, source)
            val value = varInfo.scope?.let { manager?.getVar(it, varInfo.playerName, varInfo.varName) }
                ?: throw IllegalArgumentException("变量不存在: $varName")

            // 转换为数字
            val num1 = value.toString().toDouble()
            val num2 = numStr.toDouble()

            val result = performComparison(operator, num1, num2)
            return setBooleanResult(context, targetVarName, result)
        } catch (e: NumberFormatException) {
            context.source.sendError(Text.literal("❌ 数字格式错误"))
            return 0
        } catch (e: IllegalArgumentException) {
            context.source.sendError(Text.literal("❌ ${e.message}"))
            return 0
        }
    }

    private fun compareConToVar(context: CommandContext<ServerCommandSource>): Int {
        val targetVarName = StringArgumentType.getString(context, "targetVar")
        val operator = StringArgumentType.getString(context, "operator")
        val numStr = StringArgumentType.getString(context, "num")
        val varName = StringArgumentType.getString(context, "var")

        val manager = VarManager.get(context.source.server)
        val source = context.source

        try {
            // 获取变量值
            val varInfo = parseVariable(varName, source)
            val value = varInfo.scope?.let { manager?.getVar(it, varInfo.playerName, varInfo.varName) }
                ?: throw IllegalArgumentException("变量不存在: $varName")

            // 转换为数字
            val num1 = numStr.toDouble()
            val num2 = value.toString().toDouble()

            val result = performComparison(operator, num1, num2)
            return setBooleanResult(context, targetVarName, result)
        } catch (e: NumberFormatException) {
            context.source.sendError(Text.literal("❌ 数字格式错误"))
            return 0
        } catch (e: IllegalArgumentException) {
            context.source.sendError(Text.literal("❌ ${e.message}"))
            return 0
        }
    }

    private fun performComparison(operator: String, num1: Double, num2: Double): Boolean {
        return when (operator.lowercase()) {
            "g" -> num1 > num2
            "greater" -> num1 > num2
            ">" -> num1 > num2
            "l" -> num1 < num2
            "less" -> num1 < num2
            "<" -> num1 < num2
            "e" -> num1 == num2
            "equal" -> num1 == num2
            "=" -> num1 == num2
            "ge" -> num1 >= num2
            ">=" -> num1 >= num2
            "le" -> num1 <= num2
            "<=" -> num1 <= num2
            else -> throw IllegalArgumentException("不支持的比较运算符: $operator (支持: g/l/e/ge/le)")
        }
    }

    private fun setBooleanResult(context: CommandContext<ServerCommandSource>, targetVarName: String, result: Boolean): Int {
        val manager = VarManager.get(context.source.server)
        val source = context.source

        // 解析目标变量
        val targetInfo = parseVariable(targetVarName, source)

        // 检查目标变量是否存在，如果不存在则创建
        if (!manager!!.hasVar(targetInfo.scope!!, targetInfo.playerName, targetInfo.varName)) {
            if (!manager.createVar(targetInfo.scope, targetInfo.playerName, targetInfo.varName!!, "boolean", result)) {
                context.source.sendError(Text.literal("❌ 创建目标变量失败"))
                return 0
            }
        } else {
            // 设置变量值
            if (!manager.setVar(targetInfo.scope, targetInfo.playerName, targetInfo.varName, result)) {
                context.source.sendError(Text.literal("❌ 设置变量值失败"))
                return 0
            }
        }

        val scopeText = getScopeText(targetInfo.scope, targetInfo.playerName)
        context.source.sendFeedback({
            Text.literal("✅ 比较结果已保存到变量: $scopeText${targetInfo.varName} = $result")
        }, false)

        return Command.SINGLE_SUCCESS
    }

    // 复用 VarCommand 中的解析方法
    private fun parseVariable(variableName: String, source: ServerCommandSource): VarManager.VariableInfo {
        val manager = VarManager.get(source.server)
        val info = manager?.parseVariableName(variableName) ?: VarManager.VariableInfo(null, variableName, VarManager.VariableScope.GLOBAL)

        // 如果没有指定玩家名，默认使用当前玩家
        if (info.scope == VarManager.VariableScope.PLAYER && info.playerName == null) {
            source.player?.let {
                return VarManager.VariableInfo(it.name.string, info.varName, VarManager.VariableScope.PLAYER)
            }
        }
        return info
    }

    // 复用 VarCommand 中的作用域文本方法
    private fun getScopeText(scope: VarManager.VariableScope, playerName: String?): String {
        return when (scope) {
            VarManager.VariableScope.GLOBAL -> ""
            VarManager.VariableScope.PLAYER -> "$playerName."
        }
    }
}