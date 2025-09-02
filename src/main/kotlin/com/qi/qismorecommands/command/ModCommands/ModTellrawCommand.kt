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

object ModTellrawCommand {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher: CommandDispatcher<ServerCommandSource>, _, _ ->
            dispatcher.register(
                CommandManager.literal("modtellraw")
                    .requires { it.hasPermissionLevel(2) }
                    .then(
                        CommandManager.literal("con")
                            .then(
                                CommandManager.argument<String>("message", StringArgumentType.greedyString())
                                    .executes { executeTellrawWithString(it) }
                            )
                    )
                    .then(
                        CommandManager.literal("var")
                            .then(
                                CommandManager.argument<String>("variable", StringArgumentType.string())
                                    .executes { executeTellrawWithVariable(it) }
                            )
                    )
            )
        }
    }

    // 直接使用字符串执行tellraw
    private fun executeTellrawWithString(context: CommandContext<ServerCommandSource>): Int {
        val message = StringArgumentType.getString(context, "message")

        try {
            // 直接执行原版tellraw命令
            context.source.server.commandManager.executeWithPrefix(
                context.source.withSilent(),
                "tellraw $message"
            )
            context.source.sendFeedback({ Text.literal("✅ 消息已发送") }, false)
            return Command.SINGLE_SUCCESS
        } catch (e: Exception) {
            context.source.sendError(Text.literal("❌ 执行tellraw命令失败: ${e.message}"))
            return 0
        }
    }

    // 使用变量执行tellraw
    private fun executeTellrawWithVariable(context: CommandContext<ServerCommandSource>): Int {
        val variableName = StringArgumentType.getString(context, "variable")
        val manager = VarManager.get(context.source.server)

        // 解析变量
        val info = parseVariable(variableName, context.source)
        val value = info.scope?.let { manager?.getVar(it, info.playerName, info.varName) }

        if (value == null) {
            context.source.sendError(Text.literal("❌ 变量不存在: $variableName"))
            return 0
        }

        if (value !is String) {
            context.source.sendError(Text.literal("❌ 变量类型不是字符串: $variableName"))
            return 0
        }

        try {
            // 直接执行原版tellraw命令
            context.source.server.commandManager.executeWithPrefix(
                context.source.withSilent(),
                "tellraw $value"
            )
            context.source.sendFeedback({ Text.literal("✅ 消息已发送（来自变量: $variableName）") }, false)
            return Command.SINGLE_SUCCESS
        } catch (e: Exception) {
            context.source.sendError(Text.literal("❌ 执行tellraw命令失败: ${e.message}"))
            return 0
        }
    }

    // 解析变量名（复用VarCommand中的方法）
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
}