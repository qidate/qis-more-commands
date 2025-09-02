package src.main.kotlin.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.qi.qismorecommands.command.VarManager
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object StrCommand {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher: CommandDispatcher<ServerCommandSource>, _, _ ->
            dispatcher.register(
                CommandManager.literal("str")
                    .requires { it.hasPermissionLevel(2) }
                    .then(
                        CommandManager.literal("concat")
                            .then(
                                CommandManager.argument<String>("resultVar", StringArgumentType.string())
                                    .then(
                                        CommandManager.literal("con")
                                            .then(
                                                CommandManager.argument<String>("string1", StringArgumentType.string())
                                                    .then(
                                                        CommandManager.argument<String>("string2", StringArgumentType.string())
                                                            .executes { concatStrings(it) }
                                                    )
                                            )
                                    )
                                    .then(
                                        CommandManager.literal("var")
                                            .then(
                                                CommandManager.argument<String>("var1", StringArgumentType.string())
                                                    .then(
                                                        CommandManager.argument<String>("var2", StringArgumentType.string())
                                                            .executes { concatVars(it) }
                                                    )
                                            )
                                    )
                                    .then(
                                        CommandManager.literal("v2c")
                                            .then(
                                                CommandManager.argument<String>("var", StringArgumentType.string())
                                                    .then(
                                                        CommandManager.argument<String>("string", StringArgumentType.string())
                                                            .executes { concatVarAndString(it) }
                                                    )
                                            )
                                    )
                                    .then(
                                        CommandManager.literal("c2v")
                                            .then(
                                                CommandManager.argument<String>("string", StringArgumentType.string())
                                                    .then(
                                                        CommandManager.argument<String>("var", StringArgumentType.string())
                                                            .executes { concatStringAndVar(it) }
                                                    )
                                            )
                                    )
                            )
                    )

                    .then(
                        CommandManager.literal("length")
                            .then(
                                CommandManager.argument<String>("resultVar", StringArgumentType.string())
                                    .then(
                                        CommandManager.literal("con")
                                            .then(
                                                CommandManager.argument<String>("string", StringArgumentType.greedyString())
                                                    .executes { getStringLength(it) }
                                            )
                                    )
                                    .then(
                                        CommandManager.literal("var")
                                            .then(
                                                CommandManager.argument<String>("var", StringArgumentType.string())
                                                    .executes { getVarLength(it) }
                                            )
                                    )
                            )
                    )
            )
        }
    }

    // 解析变量名（支持 player.var 格式）
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

    // 拼接两个字符串常量
    private fun concatStrings(context: CommandContext<ServerCommandSource>): Int {
        val resultVarName = StringArgumentType.getString(context, "resultVar")
        val string1 = StringArgumentType.getString(context, "string1")
        val string2 = StringArgumentType.getString(context, "string2")

        return setResultVariable(context, resultVarName, string1 + string2)
    }

    // 拼接两个变量
    private fun concatVars(context: CommandContext<ServerCommandSource>): Int {
        val resultVarName = StringArgumentType.getString(context, "resultVar")
        val var1Name = StringArgumentType.getString(context, "var1")
        val var2Name = StringArgumentType.getString(context, "var2")
        val manager = VarManager.get(context.source.server)

        // 获取第一个变量值
        val var1Info = parseVariable(var1Name, context.source)
        val var1Value = var1Info.scope?.let { manager?.getVar(it, var1Info.playerName, var1Info.varName) }
        if (var1Value == null) {
            context.source.sendError(Text.literal("❌ 变量不存在: $var1Name"))
            return 0
        }

        // 获取第二个变量值
        val var2Info = parseVariable(var2Name, context.source)
        val var2Value = var2Info.scope?.let { manager?.getVar(it, var2Info.playerName, var2Info.varName) }
        if (var2Value == null) {
            context.source.sendError(Text.literal("❌ 变量不存在: $var2Name"))
            return 0
        }

        return setResultVariable(context, resultVarName, var1Value.toString() + var2Value.toString())
    }

    // 拼接变量和字符串常量
    private fun concatVarAndString(context: CommandContext<ServerCommandSource>): Int {
        val resultVarName = StringArgumentType.getString(context, "resultVar")
        val varName = StringArgumentType.getString(context, "var")
        val string = StringArgumentType.getString(context, "string")
        val manager = VarManager.get(context.source.server)

        // 获取变量值
        val varInfo = parseVariable(varName, context.source)
        val varValue = varInfo.scope?.let { manager?.getVar(it, varInfo.playerName, varInfo.varName) }
        if (varValue == null) {
            context.source.sendError(Text.literal("❌ 变量不存在: $varName"))
            return 0
        }

        return setResultVariable(context, resultVarName, varValue.toString() + string)
    }

    // 拼接字符串常量和变量
    private fun concatStringAndVar(context: CommandContext<ServerCommandSource>): Int {
        val resultVarName = StringArgumentType.getString(context, "resultVar")
        val string = StringArgumentType.getString(context, "string")
        val varName = StringArgumentType.getString(context, "var")
        val manager = VarManager.get(context.source.server)

        // 获取变量值
        val varInfo = parseVariable(varName, context.source)
        val varValue = varInfo.scope?.let { manager?.getVar(it, varInfo.playerName, varInfo.varName) }
        if (varValue == null) {
            context.source.sendError(Text.literal("❌ 变量不存在: $varName"))
            return 0
        }

        return setResultVariable(context, resultVarName, string + varValue.toString())
    }

    // 设置结果变量（支持字符串和整数）
    private fun setResultVariable(context: CommandContext<ServerCommandSource>, resultVarName: String, result: Any): Int {
        val manager = VarManager.get(context.source.server)
        val resultInfo = parseVariable(resultVarName, context.source)

        // 确定变量类型
        val varType = if (result is Int) "int" else "string"

        // 检查结果变量是否存在，如果不存在则创建
        if (resultInfo.scope?.let { manager?.hasVar(it, resultInfo.playerName, resultInfo.varName) } != true) {
            if (manager?.createVar(resultInfo.scope!!, resultInfo.playerName, resultInfo.varName!!, varType) != true) {
                context.source.sendError(Text.literal("❌ 创建结果变量失败: $resultVarName"))
                return 0
            }
        }

        // 设置结果值
        if (manager?.setVar(resultInfo.scope!!, resultInfo.playerName, resultInfo.varName!!, result) == true) {
            val scopeText = getScopeText(resultInfo.scope!!, resultInfo.playerName)
            context.source.sendFeedback({ Text.literal("✅ 操作成功: $scopeText${resultInfo.varName} = $result") }, false)
            return Command.SINGLE_SUCCESS
        } else {
            context.source.sendError(Text.literal("❌ 设置结果变量失败: $resultVarName"))
            return 0
        }
    }

    // 工具方法：获取作用域文本
    private fun getScopeText(scope: VarManager.VariableScope, playerName: String?): String {
        return when (scope) {
            VarManager.VariableScope.GLOBAL -> ""
            VarManager.VariableScope.PLAYER -> "$playerName."
        }
    }

    // 获取字符串常量长度
    private fun getStringLength(context: CommandContext<ServerCommandSource>): Int {
        val resultVarName = StringArgumentType.getString(context, "resultVar")
        val string = StringArgumentType.getString(context, "string")

        return setResultVariable(context, resultVarName, string.length)
    }

    // 获取变量值的长度
    private fun getVarLength(context: CommandContext<ServerCommandSource>): Int {
        val resultVarName = StringArgumentType.getString(context, "resultVar")
        val varName = StringArgumentType.getString(context, "var")
        val manager = VarManager.get(context.source.server)

        // 获取变量值
        val varInfo = parseVariable(varName, context.source)
        val varValue = varInfo.scope?.let { manager?.getVar(it, varInfo.playerName, varInfo.varName) }
        if (varValue == null) {
            context.source.sendError(Text.literal("❌ 变量不存在: $varName"))
            return 0
        }

        return setResultVariable(context, resultVarName, varValue.toString().length)
    }
}