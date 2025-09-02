package com.qi.qismorecommands.command

import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale
import java.util.function.BiFunction
import java.util.function.Supplier

class VarManager : PersistentState() {
    // 全局变量
    private val globalVariables: MutableMap<String?, Any?> = HashMap<String?, Any?>()

    // 玩家变量: Map<玩家名, Map<变量名, 值>>
    private val playerVariables: MutableMap<String?, MutableMap<String?, Any?>> =
        HashMap<String?, MutableMap<String?, Any?>>()

    // 保存到NBT数据
    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound {
        // 保存全局变量
        val globalVarsNbt = NbtCompound()
        for (entry in globalVariables.entries) {
            if (entry.value != null) {
                val varNbt: NbtCompound = writeValueToNbt(entry.value)
                if (!varNbt.getKeys().isEmpty()) {
                    globalVarsNbt.put(entry.key, varNbt)
                }
            }
        }
        nbt.put("globalVariables", globalVarsNbt)

        // 保存玩家变量
        val playerVarsNbt = NbtCompound()
        for (playerEntry in playerVariables.entries) {
            val playerNbt = NbtCompound()
            for (varEntry in playerEntry.value.entries) {
                if (varEntry.value != null) {
                    val varNbt: NbtCompound = writeValueToNbt(varEntry.value)
                    if (!varNbt.getKeys().isEmpty()) {
                        playerNbt.put(varEntry.key, varNbt)
                    }
                }
            }
            if (!playerNbt.getKeys().isEmpty()) {
                playerVarsNbt.put(playerEntry.key, playerNbt)
            }
        }
        nbt.put("playerVariables", playerVarsNbt)

        return nbt
    }

    // 解析变量名
    fun parseVariableName(fullName: String): VariableInfo {
        // 处理 global. 前缀的变量
        if (fullName.startsWith("global.")) {
            return VariableInfo(null, fullName, VariableScope.GLOBAL)
        }

        // 处理玩家变量：player.varname 或 playername.varname
        if (fullName.contains(".")) {
            val parts: Array<String?> = fullName.split("\\.".toRegex(), limit = 2).toTypedArray()
            // 如果第一部分是 "player"，则是明确格式的玩家变量
            if ("player" == parts[0] && parts.size > 1) {
                val subParts: Array<String?> = parts[1]!!.split("\\.".toRegex(), limit = 2).toTypedArray()
                if (subParts.size == 2) {
                    return VariableInfo(subParts[0], subParts[1], VariableScope.PLAYER)
                }
            }
            // 否则是传统格式的玩家变量
            return VariableInfo(parts[0], parts[1], VariableScope.PLAYER)
        }

        // 默认全局变量
        return VariableInfo(null, fullName, VariableScope.GLOBAL)
    }

    // 创建变量
    @JvmOverloads
    fun createVar(
        scope: VariableScope,
        playerName: String?,
        name: String,
        type: String,
        defaultValue: Any? = null
    ): Boolean {
        // 如果是全局变量且变量名包含点，自动添加 global. 前缀
        var name = name
        if (scope == VariableScope.GLOBAL && name.contains(".") && !name.startsWith("global.")) {
            name = "global." + name
        }

        val value = when (type.lowercase(Locale.getDefault())) {
            "int" -> if (defaultValue != null) defaultValue else 0
            "string" -> {
                val strValue: String? = if (defaultValue != null) defaultValue.toString() else ""
                strValue
            }

            "boolean" -> if (defaultValue != null) defaultValue else false
            else -> null
        }

        if (value == null) return false

        when (scope) {
            VariableScope.GLOBAL -> {
                if (globalVariables.containsKey(name)) return false
                globalVariables.put(name, value)
            }

            VariableScope.PLAYER -> {
                val playerVars = playerVariables.computeIfAbsent(playerName) { k: String? -> HashMap<String?, Any?>() }
                if (playerVars.containsKey(name)) return false
                playerVars.put(name, value)
            }
        }

        markDirty()
        return true
    }

    // 获取变量值
    fun getVar(scope: VariableScope, playerName: String?, name: String?): Any? {
        return when (scope) {
            VariableScope.GLOBAL -> globalVariables.get(name)
            VariableScope.PLAYER -> {
                val playerVars = playerVariables.get(playerName)
                if (playerVars != null) playerVars.get(name) else null
            }
        }
    }

    // 设置变量值
    fun setVar(scope: VariableScope, playerName: String?, name: String?, value: Any): Boolean {
        val existingValue = getVar(scope, playerName, name)
        if (existingValue == null || existingValue.javaClass != value.javaClass) {
            return false
        }

        when (scope) {
            VariableScope.GLOBAL -> globalVariables.put(name, value)
            VariableScope.PLAYER -> {
                val playerVars = playerVariables.computeIfAbsent(playerName) { k: String? -> HashMap<String?, Any?>() }
                playerVars.put(name, value)
            }
        }

        markDirty()
        return true
    }

    // 检查变量是否存在
    fun hasVar(scope: VariableScope, playerName: String?, name: String?): Boolean {
        return when (scope) {
            VariableScope.GLOBAL -> globalVariables.containsKey(name)
            VariableScope.PLAYER -> {
                val playerVars = playerVariables.get(playerName)
                playerVars != null && playerVars.containsKey(name)
            }
        }
    }

    // 删除变量
    fun deleteVar(scope: VariableScope, playerName: String?, name: String?): Boolean {
        val removed = when (scope) {
            VariableScope.GLOBAL -> globalVariables.remove(name) != null
            VariableScope.PLAYER -> {
                val playerVars = playerVariables.get(playerName)
                playerVars != null && playerVars.remove(name) != null
            }
        }

        if (removed) markDirty()
        return removed
    }

    // 获取所有变量名
    fun getAllVarNames(scope: VariableScope, playerName: String?): MutableList<String?> {
        return when (scope) {
            VariableScope.GLOBAL -> ArrayList<String?>(globalVariables.keys)
            VariableScope.PLAYER -> {
                val playerVars = playerVariables.get(playerName)
                if (playerVars != null) ArrayList<String?>(playerVars.keys) else ArrayList<String?>()
            }
        }
    }

    // 获取变量数量
    fun getVarCount(scope: VariableScope, playerName: String?): Int {
        return when (scope) {
            VariableScope.GLOBAL -> globalVariables.size
            VariableScope.PLAYER -> {
                val playerVars = playerVariables.get(playerName)
                if (playerVars != null) playerVars.size else 0
            }
        }
    }

    // 检查变量类型是否匹配
    fun isVarType(name: String?, type: Class<*>?): Boolean {
        if (!hasVar(VariableScope.GLOBAL, null, name)) return false
        return globalVariables.get(name)!!.javaClass == type
    }

    // 获取变量类型字符串
    fun getVarType(name: String?): String {
        if (!hasVar(VariableScope.GLOBAL, null, name)) return "unknown"
        val value = globalVariables.get(name)
        if (value is Int) return "int"
        if (value is String) return "string"
        if (value is Boolean) return "boolean"
        return "unknown"
    }

    // 枚举：变量作用域
    enum class VariableScope {
        GLOBAL, PLAYER
    }

    // 变量信息类
    class VariableInfo(@JvmField val playerName: String?, @JvmField val varName: String?, @JvmField val scope: VariableScope?)
    companion object {
        private const val PERSISTENT_ID = "qis_more_commands_vars"

        // 获取或创建变量管理器实例
        @JvmStatic
        fun get(server: MinecraftServer): VarManager? {
            val persistentStateManager = server.getWorld(World.OVERWORLD)!!.getPersistentStateManager()
            return persistentStateManager.getOrCreate<VarManager?>(
                Type<VarManager?>(
                    Supplier { VarManager() },
                    BiFunction { nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup? ->
                        fromNbt(
                            nbt!!,
                            registryLookup
                        )
                    },
                    null
                ), PERSISTENT_ID
            )
        }

        // 从NBT数据读取
        fun fromNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup?): VarManager {
            val manager = VarManager()

            // 加载全局变量
            val globalVarsNbt = nbt.getCompound("globalVariables")
            for (key in globalVarsNbt.getKeys()) {
                manager.globalVariables.put(key, readValueFromNbt(globalVarsNbt.getCompound(key)))
            }

            // 加载玩家变量
            val playerVarsNbt = nbt.getCompound("playerVariables")
            for (playerName in playerVarsNbt.getKeys()) {
                val playerNbt = playerVarsNbt.getCompound(playerName)
                val playerVars: MutableMap<String?, Any?> = HashMap<String?, Any?>()
                for (varName in playerNbt.getKeys()) {
                    playerVars.put(varName, readValueFromNbt(playerNbt.getCompound(varName)))
                }
                manager.playerVariables.put(playerName, playerVars)
            }

            return manager
        }

        // 从NBT读取值
        private fun readValueFromNbt(nbt: NbtCompound): Any? {
            val type = nbt.getString("type")
            return when (type) {
                "int" -> nbt.getInt("value")
                "string" -> nbt.getString("value")
                "boolean" -> nbt.getBoolean("value")
                else -> null
            }
        }

        // 写入值到NBT
        private fun writeValueToNbt(value: Any?): NbtCompound {
            val nbt = NbtCompound()

            if (value is Int) {
                nbt.putString("type", "int")
                nbt.putInt("value", value)
            } else if (value is String) {
                nbt.putString("type", "string")
                val stringValue = value
                nbt.putString("value", if (stringValue != null) stringValue else "")
            } else if (value is Boolean) {
                nbt.putString("type", "boolean")
                nbt.putBoolean("value", value)
            }

            return nbt
        }
    }
}