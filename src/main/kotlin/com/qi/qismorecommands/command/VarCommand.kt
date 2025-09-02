package com.qi.qismorecommands.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.util.*

object VarCommand {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("var")
                    .requires { it.hasPermissionLevel(2) }
                    .then(
                        CommandManager.literal("new")
                            .then(
                                CommandManager.literal("global")
                                    .then(
                                        CommandManager.argument<String>("type", StringArgumentType.string())
                                            .then(
                                                CommandManager.argument<String>("name", StringArgumentType.string())
                                                    .executes { createNewVar(it, VarManager.VariableScope.GLOBAL, null) }
                                                    .then(
                                                        CommandManager.literal("con")
                                                            .then(
                                                                CommandManager.argument<String>("value", StringArgumentType.greedyString())
                                                                    .executes { createNewVarWithValue(it, VarManager.VariableScope.GLOBAL, null) }
                                                            )
                                                    )
                                                    .then(
                                                        CommandManager.literal("var")
                                                            .then(
                                                                CommandManager.argument<String>("sourceVar", StringArgumentType.string())
                                                                    .executes { createNewVarFromVar(it, VarManager.VariableScope.GLOBAL, null) }
                                                            )
                                                    )
                                            )
                                    )
                            )
                            .then(
                                CommandManager.literal("player")
                                    .then(
                                        CommandManager.argument<String>("player", StringArgumentType.string())
                                            .then(
                                                CommandManager.argument<String>("type", StringArgumentType.string())
                                                    .then(
                                                        CommandManager.argument<String>("name", StringArgumentType.string())
                                                            .executes { createNewVar(it, VarManager.VariableScope.PLAYER, StringArgumentType.getString(it, "player")) }
                                                            .then(
                                                                CommandManager.literal("con")
                                                                    .then(
                                                                        CommandManager.argument<String>("value", StringArgumentType.greedyString())
                                                                            .executes { createNewVarWithValue(it, VarManager.VariableScope.PLAYER, StringArgumentType.getString(it, "player")) }
                                                                    )
                                                            )
                                                            .then(
                                                                CommandManager.literal("var")
                                                                    .then(
                                                                        CommandManager.argument<String>("sourceVar", StringArgumentType.string())
                                                                            .executes { createNewVarFromVar(it, VarManager.VariableScope.PLAYER, StringArgumentType.getString(it, "player")) }
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
                    .then(
                        CommandManager.literal("set")
                            .then(
                                CommandManager.argument<String>("variable", StringArgumentType.string())
                                    .then(
                                        CommandManager.literal("con")
                                            .then(
                                                CommandManager.argument<String>("value", StringArgumentType.greedyString())
                                                    .executes { setVarWithValue(it) }
                                            )
                                    )
                                    .then(
                                        CommandManager.literal("var")
                                            .then(
                                                CommandManager.argument<String>("sourceVar", StringArgumentType.string())
                                                    .executes { setVarFromVar(it) }
                                            )
                                    )
                            )
                    )
                    .then(
                        CommandManager.literal("print")
                            .then(
                                CommandManager.argument<String>("variable", StringArgumentType.greedyString())
                                    .executes { printVar(it) }
                            )
                    )
                    .then(
                        CommandManager.literal("delete")
                            .then(
                                CommandManager.argument<String>("variable", StringArgumentType.greedyString())
                                    .executes { deleteVar(it) }
                            )
                    )
                    .then(
                        CommandManager.literal("list")
                            .executes { listAllVars(it, null) }
                            .then(
                                CommandManager.argument<String>("player", StringArgumentType.string())
                                    .executes { listAllVars(it, StringArgumentType.getString(it, "player")) }
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

    // 创建变量（无初始值）
    private fun createNewVar(context: CommandContext<ServerCommandSource>, scope: VarManager.VariableScope, playerName: String?): Int {
        val type = StringArgumentType.getString(context, "type")
        val name = StringArgumentType.getString(context, "name")
        val manager = VarManager.get(context.source.server)

        if (manager?.createVar(scope, playerName, name, type) == true) {
            val scopeText = getScopeText(scope, playerName)
            context.source.sendFeedback({ Text.literal("✅ 创建变量成功: $scopeText$name ($type)") }, false)
            return Command.SINGLE_SUCCESS
        } else {
            context.source.sendError(Text.literal("❌ 创建变量失败！可能已存在或类型不支持"))
            return 0
        }
    }

    // 创建变量并设置值
    private fun createNewVarWithValue(context: CommandContext<ServerCommandSource>, scope: VarManager.VariableScope, playerName: String?): Int {
        val type = StringArgumentType.getString(context, "type")
        val name = StringArgumentType.getString(context, "name")
        val valueStr = StringArgumentType.getString(context, "value")
        val manager = VarManager.get(context.source.server)

        // 先创建变量
        if (manager?.createVar(scope, playerName, name, type) != true) {
            context.source.sendError(Text.literal("❌ 创建变量失败！可能已存在或类型不支持"))
            return 0
        }

        // 转换并设置值
        val newValue = try {
            convertValue(type, valueStr)
        } catch (e: IllegalArgumentException) {
            context.source.sendError(Text.literal("❌ ${e.message}"))
            manager?.deleteVar(scope, playerName, name)
            return 0
        }

        if (manager?.setVar(scope, playerName, name, newValue) == true) {
            val scopeText = getScopeText(scope, playerName)
            context.source.sendFeedback({ Text.literal("✅ 创建并设置变量成功: $scopeText$name = $newValue") }, false)
            return Command.SINGLE_SUCCESS
        } else {
            context.source.sendError(Text.literal("❌ 设置变量值失败！"))
            manager?.deleteVar(scope, playerName, name)
            return 0
        }
    }

    // 从其他变量创建新变量
    private fun createNewVarFromVar(context: CommandContext<ServerCommandSource>, scope: VarManager.VariableScope, playerName: String?): Int {
        val type = StringArgumentType.getString(context, "type")
        val name = StringArgumentType.getString(context, "name")
        val sourceVarName = StringArgumentType.getString(context, "sourceVar")
        val manager = VarManager.get(context.source.server)

        // 解析源变量
        val sourceInfo = parseVariable(sourceVarName, context.source)
        val sourceValue = sourceInfo.scope?.let { manager?.getVar(it, sourceInfo.playerName, sourceInfo.varName) }

        if (sourceValue == null) {
            context.source.sendError(Text.literal("❌ 源变量不存在: $sourceVarName"))
            return 0
        }

        // 先创建变量
        if (manager?.createVar(scope, playerName, name, type) != true) {
            context.source.sendError(Text.literal("❌ 创建变量失败！可能已存在或类型不支持"))
            return 0
        }

        // 转换源值到目标类型
        val newValue = try {
            convertValue(type, sourceValue.toString())
        } catch (e: IllegalArgumentException) {
            context.source.sendError(Text.literal("❌ 类型转换失败: ${e.message}"))
            manager?.deleteVar(scope, playerName, name)
            return 0
        }

        if (manager?.setVar(scope, playerName, name, newValue) == true) {
            val scopeText = getScopeText(scope, playerName)
            context.source.sendFeedback({ Text.literal("✅ 从变量创建成功: $scopeText$name = $newValue (来自: $sourceVarName)") }, false)
            return Command.SINGLE_SUCCESS
        } else {
            context.source.sendError(Text.literal("❌ 设置变量值失败！"))
            manager?.deleteVar(scope, playerName, name)
            return 0
        }
    }

    // 设置变量值
    private fun setVarWithValue(context: CommandContext<ServerCommandSource>): Int {
        val variableName = StringArgumentType.getString(context, "variable")
        val valueStr = StringArgumentType.getString(context, "value")
        val manager = VarManager.get(context.source.server)

        // 解析变量信息
        val info = parseVariable(variableName, context.source)
        val currentValue = info.scope?.let { manager?.getVar(it, info.playerName, info.varName) }

        if (currentValue == null) {
            context.source.sendError(Text.literal("❌ 变量不存在: $variableName"))
            return 0
        }

        // 转换新值
        val newValue = try {
            val type = currentValue::class.simpleName?.lowercase(Locale.getDefault()) ?: "string"
            convertValue(type, valueStr)
        } catch (e: IllegalArgumentException) {
            context.source.sendError(Text.literal("❌ ${e.message}"))
            return 0
        }

        if (manager?.setVar(info.scope, info.playerName, info.varName, newValue) == true) {
            val scopeText = getScopeText(info.scope, info.playerName)
            context.source.sendFeedback({ Text.literal("✅ 设置变量成功: $scopeText${info.varName} = $newValue") }, false)
            return Command.SINGLE_SUCCESS
        } else {
            context.source.sendError(Text.literal("❌ 设置变量失败！类型不匹配"))
            return 0
        }
    }

    // 从其他变量设置值
    private fun setVarFromVar(context: CommandContext<ServerCommandSource>): Int {
        val targetVarName = StringArgumentType.getString(context, "variable")
        val sourceVarName = StringArgumentType.getString(context, "sourceVar")
        val manager = VarManager.get(context.source.server)

        // 解析目标变量
        val targetInfo = parseVariable(targetVarName, context.source)
        val targetCurrentValue = targetInfo.scope?.let { manager?.getVar(it, targetInfo.playerName, targetInfo.varName) }

        if (targetCurrentValue == null) {
            context.source.sendError(Text.literal("❌ 目标变量不存在: $targetVarName"))
            return 0
        }

        // 解析源变量
        val sourceInfo = parseVariable(sourceVarName, context.source)
        val sourceValue = sourceInfo.scope?.let { manager?.getVar(it, sourceInfo.playerName, sourceInfo.varName) }

        if (sourceValue == null) {
            context.source.sendError(Text.literal("❌ 源变量不存在: $sourceVarName"))
            return 0
        }

        if (manager?.setVar(targetInfo.scope, targetInfo.playerName, targetInfo.varName, sourceValue) == true) {
            val scopeText = getScopeText(targetInfo.scope, targetInfo.playerName)
            context.source.sendFeedback({ Text.literal("✅ 设置变量成功: $scopeText${targetInfo.varName} = $sourceValue") }, false)
            return Command.SINGLE_SUCCESS
        } else {
            context.source.sendError(Text.literal("❌ 设置变量失败！类型不匹配"))
            return 0
        }
    }

    private fun printVar(context: CommandContext<ServerCommandSource>): Int {
        val variableName = StringArgumentType.getString(context, "variable")
        val manager = VarManager.get(context.source.server)

        val info = parseVariable(variableName, context.source)
        val value = info.scope?.let { manager?.getVar(it, info.playerName, info.varName) }

        if (value == null) {
            context.source.sendError(Text.literal("❌ 变量不存在: $variableName"))
            return 0
        }

        val scopeText = info.scope?.let { getScopeText(it, info.playerName) }
        context.source.sendFeedback({
            Text.literal("📋 变量 $scopeText${info.varName} = $value (${value::class.simpleName})")
        }, false)

        return Command.SINGLE_SUCCESS
    }

    private fun deleteVar(context: CommandContext<ServerCommandSource>): Int {
        val variableName = StringArgumentType.getString(context, "variable")
        val manager = VarManager.get(context.source.server)

        val info = parseVariable(variableName, context.source)
        info.scope?.let {
            if (manager?.deleteVar(it, info.playerName, info.varName) == true) {
                val scopeText = getScopeText(info.scope, info.playerName)
                context.source.sendFeedback({ Text.literal("✅ 删除变量成功: $scopeText${info.varName}") }, false)
                return Command.SINGLE_SUCCESS
            } else {
                context.source.sendError(Text.literal("❌ 删除变量失败！变量不存在: $variableName"))
                return 0
            }
        }

        return 0
    }

    private fun listAllVars(context: CommandContext<ServerCommandSource>, playerName: String?): Int {
        val manager = VarManager.get(context.source.server)

        if (playerName == null) {
            // 列出全局变量
            val globalVars = manager?.getAllVarNames(VarManager.VariableScope.GLOBAL, null) ?: emptyList()
            if (globalVars.isEmpty()) {
                context.source.sendFeedback({ Text.literal("❌ 没有全局变量") }, false)
            } else {
                context.source.sendFeedback({ Text.literal("📋 全局变量:") }, false)
                globalVars.sortedBy { it }.forEach { varName ->
                    val value = manager?.getVar(VarManager.VariableScope.GLOBAL, null, varName)
                    context.source.sendFeedback({ Text.literal("  ▪ $varName = $value") }, false)
                }
            }
        } else {
            // 列出玩家变量
            val playerVars = manager?.getAllVarNames(VarManager.VariableScope.PLAYER, playerName) ?: emptyList()
            if (playerVars.isEmpty()) {
                context.source.sendFeedback({ Text.literal("❌ 玩家 $playerName 没有变量") }, false)
            } else {
                context.source.sendFeedback({ Text.literal("📋 玩家 $playerName 的变量:") }, false)
                playerVars.sortedBy { it }.forEach { varName ->
                    val value = manager?.getVar(VarManager.VariableScope.PLAYER, playerName, varName)
                    context.source.sendFeedback({ Text.literal("  ▪ $varName = $value") }, false)
                }
            }
        }

        return Command.SINGLE_SUCCESS
    }

    // 工具方法：值类型转换
    @Throws(IllegalArgumentException::class)
    private fun convertValue(type: String, valueStr: String): Any {
        return when (type.lowercase(Locale.getDefault())) {
            "int" -> valueStr.toIntOrNull() ?: throw IllegalArgumentException("数值格式错误: $valueStr")
            "string" -> valueStr
            "boolean" -> when {
                valueStr.equals("true", true) || valueStr == "1" -> true
                valueStr.equals("false", true) || valueStr == "0" -> false
                else -> throw IllegalArgumentException("布尔值必须是 true/false 或 1/0")
            }
            else -> throw IllegalArgumentException("不支持的变量类型: $type")
        }
    }

    // 工具方法：获取作用域文本
    private fun getScopeText(scope: VarManager.VariableScope, playerName: String?): String {
        return when (scope) {
            VarManager.VariableScope.GLOBAL -> ""
            VarManager.VariableScope.PLAYER -> "$playerName."
        }
    }
}