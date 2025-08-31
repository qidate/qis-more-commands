package com.qi.qismorecommands.command;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class VarManager extends PersistentState {
    private static final String PERSISTENT_ID = "qis_more_commands_vars";
    private final Map<String, Object> variables = new HashMap<>();

    // 获取或创建变量管理器实例
    public static VarManager get(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return persistentStateManager.getOrCreate(new Type<>(VarManager::new, VarManager::fromNbt, null), PERSISTENT_ID);
    }

    // 从NBT数据读取
    public static VarManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        VarManager manager = new VarManager();
        NbtCompound varsNbt = nbt.getCompound("variables");

        for (String key : varsNbt.getKeys()) {
            NbtCompound varNbt = varsNbt.getCompound(key);
            String type = varNbt.getString("type");
            Object value = null;

            switch (type) {
                case "int":
                    value = varNbt.getInt("value");
                    break;
                case "string":
                    value = varNbt.getString("value");
                    break;
                case "boolean":
                    value = varNbt.getBoolean("value");
                    break;
            }

            if (value != null) {
                manager.variables.put(key, value);
            }
        }

        return manager;
    }

    // 保存到NBT数据 - 新的方法签名
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound varsNbt = new NbtCompound();

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            NbtCompound varNbt = new NbtCompound();
            Object value = entry.getValue();

            if (value instanceof Integer) {
                varNbt.putString("type", "int");
                varNbt.putInt("value", (Integer) value);
            } else if (value instanceof String) {
                varNbt.putString("type", "string");
                varNbt.putString("value", (String) value);
            } else if (value instanceof Boolean) {
                varNbt.putString("type", "boolean");
                varNbt.putBoolean("value", (Boolean) value);
            }

            varsNbt.put(entry.getKey(), varNbt);
        }

        nbt.put("variables", varsNbt);
        return nbt;
    }

    // 创建新变量
    public boolean createVar(String name, String type) {
        if (variables.containsKey(name)) {
            return false;
        }

        switch (type.toLowerCase()) {
            case "int":
                variables.put(name, 0);
                break;
            case "string":
                variables.put(name, "");
                break;
            case "boolean":
                variables.put(name, false);
                break;
            default:
                return false;
        }

        markDirty();
        return true;
    }

    // 设置变量值
    public boolean setVar(String name, Object value) {
        if (!variables.containsKey(name)) {
            return false;
        }

        Object currentValue = variables.get(name);
        if (currentValue.getClass() != value.getClass()) {
            return false;
        }

        variables.put(name, value);
        markDirty();
        return true;
    }

    // 获取变量值
    public Object getVar(String name) {
        return variables.get(name);
    }

    // 检查变量是否存在
    public boolean hasVar(String name) {
        return variables.containsKey(name);
    }

    // 删除变量
    public boolean deleteVar(String name) {
        if (!variables.containsKey(name)) {
            return false;
        }

        variables.remove(name);
        markDirty();
        return true;
    }

    // 获取所有变量
    public Map<String, Object> getAllVars() {
        return new HashMap<>(variables);
    }

    // 获取所有变量名的列表
    public List<String> getAllVarNames() {
        return new ArrayList<>(variables.keySet());
    }

    // 获取变量数量
    public int getVarCount() {
        return variables.size();
    }

    // 检查变量类型是否匹配
    public boolean isVarType(String name, Class<?> type) {
        if (!hasVar(name)) return false;
        return variables.get(name).getClass() == type;
    }

    // 获取变量类型字符串
    public String getVarType(String name) {
        if (!hasVar(name)) return "unknown";
        Object value = variables.get(name);
        if (value instanceof Integer) return "int";
        if (value instanceof String) return "string";
        if (value instanceof Boolean) return "boolean";
        return "unknown";
    }
}