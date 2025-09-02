package com.qi.qismorecommands.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * 重写的If命令：简化为条件判断→执行命令
 */
object IfCommand {
    fun register() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>?, registryAccess: CommandRegistryAccess?, environment: CommandManager.RegistrationEnvironment? ->
            val ifCommand = CommandManager.literal("if")
                .requires(Predicate { source: ServerCommandSource? -> source!!.hasPermissionLevel(2) })
            // con 条件分支
            ifCommand.then(
                CommandManager.literal("con")
                    .then(
                        CommandManager.argument<String?>("condition", StringArgumentType.string())
                            .then(buildThenBranch("con"))
                    )
            )

            // var 条件分支
            ifCommand.then(
                CommandManager.literal("var")
                    .then(
                        CommandManager.argument<String?>("conditionVar", StringArgumentType.string())
                            .then(buildThenBranch("var"))
                    )
            )
            dispatcher!!.register(ifCommand)
        })
    }

    /**
     * 构建then分支
     */
    private fun buildThenBranch(conditionSourceType: String?): LiteralArgumentBuilder<ServerCommandSource?> {
        val thenBranch = CommandManager.literal("then")

        // then con 分支
        thenBranch.then(
            CommandManager.literal("con")
                .then(
                    CommandManager.argument<String?>("thenCommand", StringArgumentType.string())
                        .executes(Command { ctx: CommandContext<ServerCommandSource?>? ->
                            executeIfLogic(
                                ctx!!, conditionSourceType,
                                "con", StringArgumentType.getString(ctx, "thenCommand"),
                                null, null
                            )
                        })
                        .then(buildElseBranch(conditionSourceType, "con", "thenCommand"))
                )
        )

        // then var 分支
        thenBranch.then(
            CommandManager.literal("var")
                .then(
                    CommandManager.argument<String?>("thenVar", StringArgumentType.string())
                        .executes(Command { ctx: CommandContext<ServerCommandSource?>? ->
                            executeIfLogic(
                                ctx!!, conditionSourceType,
                                "var", StringArgumentType.getString(ctx, "thenVar"),
                                null, null
                            )
                        })
                        .then(buildElseBranch(conditionSourceType, "var", "thenVar"))
                )
        )

        return thenBranch
    }

    /**
     * 构建else分支
     */
    private fun buildElseBranch(
        conditionSourceType: String?, thenSourceType: String?, thenArgName: String?
    ): LiteralArgumentBuilder<ServerCommandSource?> {
        val elseBranch = CommandManager.literal("else")

        // else con 分支
        elseBranch.then(
            CommandManager.literal("con")
                .then(
                    CommandManager.argument<String?>("elseCommand", StringArgumentType.string())
                        .executes(Command { ctx: CommandContext<ServerCommandSource?>? ->
                            executeIfLogic(
                                ctx!!, conditionSourceType,
                                thenSourceType, getArgumentValue(ctx, thenSourceType, thenArgName)!!,
                                "con", StringArgumentType.getString(ctx, "elseCommand")
                            )
                        })
                )
        )

        // else var 分支
        elseBranch.then(
            CommandManager.literal("var")
                .then(
                    CommandManager.argument<String?>("elseVar", StringArgumentType.string())
                        .executes(Command { ctx: CommandContext<ServerCommandSource?>? ->
                            executeIfLogic(
                                ctx!!, conditionSourceType,
                                thenSourceType, getArgumentValue(ctx, thenSourceType, thenArgName)!!,
                                "var", StringArgumentType.getString(ctx, "elseVar")
                            )
                        })
                )
        )

        return elseBranch
    }

    /**
     * 核心逻辑：判断条件→执行then/else命令
     */
    private fun executeIfLogic(
        ctx: CommandContext<ServerCommandSource?>,
        conditionSourceType: String?,
        thenSourceType: String?, thenCommand: String,
        elseSourceType: String?, elseCommand: String?
    ): Int {
        val server = ctx.getSource()!!.getServer()
        val varManager = VarManager.Companion.get(server)

        // 解析条件
        val conditionResult = parseCondition(ctx, conditionSourceType, varManager!!)

        // 根据条件结果选择执行的命令
        var commandToExecute: String? = null
        if (conditionResult) {
            commandToExecute = getCommandContent(ctx, thenSourceType, thenCommand, varManager)
        } else if (elseCommand != null) {
            commandToExecute = getCommandContent(ctx, elseSourceType, elseCommand, varManager)
        }

        // 执行命令
        if (commandToExecute != null) {
            // 去掉可能的前缀 "/"
            val cleanCommand = if (commandToExecute.startsWith("/")) commandToExecute.substring(1) else commandToExecute

            server.getCommandManager().executeWithPrefix(ctx.getSource(), cleanCommand)
            ctx.getSource()!!.sendFeedback(Supplier { Text.literal("✅ 执行命令: " + cleanCommand) }, false)
        }

        return Command.SINGLE_SUCCESS
    }

    /**
     * 解析条件
     */
    private fun parseCondition(
        ctx: CommandContext<ServerCommandSource?>,
        conditionSourceType: String?,
        varManager: VarManager
    ): Boolean {
        val conditionValue: String?

        if ("con" == conditionSourceType) {
            conditionValue = StringArgumentType.getString(ctx, "condition")
        } else {
            val varName = StringArgumentType.getString(ctx, "conditionVar")
            val varInfo = varManager.parseVariableName(varName)
            val varValue = varManager.getVar(varInfo.scope!!, varInfo.playerName, varInfo.varName)

            if (varValue == null) {
                ctx.getSource()!!.sendError(Text.literal("❌ 条件变量不存在: " + varName))
                return false
            }
            conditionValue = varValue.toString()
        }

        // 解析布尔值
        return "true".equals(conditionValue, ignoreCase = true) || "1" == conditionValue
    }

    /**
     * 获取命令内容
     */
    private fun getCommandContent(
        ctx: CommandContext<ServerCommandSource?>,
        sourceType: String?, content: String,
        varManager: VarManager
    ): String? {
        if ("con" == sourceType) {
            return content
        } else if ("var" == sourceType) {
            val varInfo = varManager.parseVariableName(content)
            val varValue = varManager.getVar(varInfo.scope!!, varInfo.playerName, varInfo.varName)

            if (varValue == null) {
                ctx.getSource()!!.sendError(Text.literal("❌ 命令变量不存在: " + content))
                return null
            }
            return varValue.toString()
        }
        return null
    }

    /**
     * 从上下文中获取参数值
     */
    private fun getArgumentValue(
        ctx: CommandContext<ServerCommandSource?>,
        sourceType: String?, argName: String?
    ): String? {
        if ("con" == sourceType) {
            return StringArgumentType.getString(ctx, argName)
        } else if ("var" == sourceType) {
            return StringArgumentType.getString(ctx, argName)
        }
        return null
    }
}