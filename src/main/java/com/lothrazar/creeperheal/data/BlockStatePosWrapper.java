package com.lothrazar.creeperheal.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

public class BlockStatePosWrapper {
    private BlockPos blockPos;
    private BlockState blockState;
    // 保存原始的NBT标签，用于稍后修复BlockState
    public net.minecraft.nbt.CompoundTag nbtTag;

    public BlockStatePosWrapper() {
    }

    public BlockStatePosWrapper(BlockPos blockPos, BlockState blockState) {
        this.blockPos = blockPos;
        this.blockState = blockState;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public BlockState getBlockState() {
        return blockState;
    }
    
    public void setBlockState(BlockState blockState) {
        this.blockState = blockState;
    }

    /**
     * Gets the tile entity tag (backward compatibility method)
     * @return null (we no longer save tile entity data)
     */
    public net.minecraft.nbt.CompoundTag getTileEntityTag() {
        return null;
    }

    public void writeToNBT(CompoundTag tag) {
        if (nbtTag != null) {
          tag.put("nbt", nbtTag);
        }
        // 使用xyz坐标直接保存，而不是转换成long
        tag.putInt("x", blockPos.getX());
        tag.putInt("y", blockPos.getY());
        tag.putInt("z", blockPos.getZ());
        // 保存Block的资源位置
        net.minecraft.resources.ResourceLocation blockLoc = blockState.getBlock().builtInRegistryHolder().key().location();
        tag.putString("block", blockLoc.toString());
        // 保存BlockState的序列化数据
        // 使用Minecraft内置的序列化器
        net.minecraft.nbt.CompoundTag blockStateTag = new net.minecraft.nbt.CompoundTag();
        blockStateTag.putString("Name", blockLoc.toString());
        // 保存BlockState的属性
        net.minecraft.nbt.CompoundTag propertiesTag = new net.minecraft.nbt.CompoundTag();
        for (net.minecraft.world.level.block.state.properties.Property<?> property : blockState.getProperties()) {
            String name = property.getName();
            String value = blockState.getValue(property).toString();
            propertiesTag.putString(name, value);
        }
        blockStateTag.put("Properties", propertiesTag);
        tag.put("blockState", blockStateTag);
    }

    public void readFromNBT(CompoundTag tag, Level level) {
        // 读取原始的NBT标签（如果存在）
        if (tag.contains("nbt", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
          nbtTag = tag.getCompound("nbt");
        }
        // 优先从xyz坐标读取，支持旧格式的long坐标
        if (tag.contains("x") && tag.contains("y") && tag.contains("z")) {
            // 使用xyz坐标直接构建BlockPos
            int x = tag.getInt("x");
            int y = tag.getInt("y");
            int z = tag.getInt("z");
            this.blockPos = new BlockPos(x, y, z);
        } else if (tag.contains("pos")) {
            // 兼容旧格式，从long坐标读取
            this.blockPos = BlockPos.of(tag.getLong("pos"));
        } else {
            // 无效的坐标数据，使用默认位置
            this.blockPos = BlockPos.ZERO;
        }
        try {
            if (tag.contains("blockState") && level != null) {
                // 读取BlockState序列化数据
                net.minecraft.nbt.CompoundTag blockStateTag = tag.getCompound("blockState");
                String blockName = blockStateTag.getString("Name");
                net.minecraft.resources.ResourceLocation blockLoc = net.minecraft.resources.ResourceLocation.tryParse(blockName);
                if (blockLoc != null) {
                    // 从注册表中获取Block
                    net.minecraft.world.level.block.Block block = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BLOCK).get(blockLoc);
                    if (block != null) {
                        // 获取Block的默认状态
                        net.minecraft.world.level.block.state.BlockState finalState = block.defaultBlockState();
                        // 读取并应用属性
                        if (blockStateTag.contains("Properties")) {
                            net.minecraft.nbt.CompoundTag propertiesTag = blockStateTag.getCompound("Properties");
                            for (String propName : propertiesTag.getAllKeys()) {
                                try {
                                    String propValue = propertiesTag.getString(propName);
                                    // 获取Block的属性定义
                                    net.minecraft.world.level.block.state.properties.Property<?> property = block.getStateDefinition().getProperty(propName);
                                    if (property != null) {
                                        // 解析属性值
                                        java.util.Optional<?> optParsedValue = property.getValue(propValue);
                                        if (optParsedValue.isPresent()) {
                                            // 使用反射来设置属性值，避免类型安全问题
                                            try {
                                                java.lang.reflect.Method setValueMethod = net.minecraft.world.level.block.state.BlockState.class.getMethod("setValue", net.minecraft.world.level.block.state.properties.Property.class, Object.class);
                                                finalState = (net.minecraft.world.level.block.state.BlockState) setValueMethod.invoke(finalState, property, optParsedValue.get());
                                            } catch (Exception ex) {
                                                // 忽略反射错误
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    // 忽略单个属性的错误
                                }
                            }
                        }
                        this.blockState = finalState;
                    } else {
                        this.blockState = Blocks.STONE.defaultBlockState();
                    }
                } else {
                    this.blockState = Blocks.STONE.defaultBlockState();
                }
            } else if (tag.contains("block")) {
                // 兼容旧格式
                String blockName = tag.getString("block");
                net.minecraft.resources.ResourceLocation blockLoc = net.minecraft.resources.ResourceLocation.tryParse(blockName);
                if (blockLoc != null && level != null) {
                    net.minecraft.world.level.block.Block block = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BLOCK).get(blockLoc);
                    if (block != null) {
                        this.blockState = block.defaultBlockState();
                    } else {
                        this.blockState = Blocks.STONE.defaultBlockState();
                    }
                } else {
                    this.blockState = Blocks.STONE.defaultBlockState();
                }
            } else {
                this.blockState = Blocks.STONE.defaultBlockState();
            }
        } catch (Exception e) {
            this.blockState = Blocks.STONE.defaultBlockState();
        }
    }
}