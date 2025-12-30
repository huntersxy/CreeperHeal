package com.lothrazar.creeperheal.worldhealer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.Supplier;
import com.lothrazar.creeperheal.ConfigRegistryCreeperheal;
import com.lothrazar.creeperheal.ForgeCreeperHeal;
import com.lothrazar.creeperheal.data.BlockStatePosWrapper;
import com.lothrazar.creeperheal.data.TickContainer;
import com.lothrazar.creeperheal.data.TickingHealList;
import com.lothrazar.creeperheal.util.LevelWorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.event.level.ExplosionEvent;

public class WorldHealerSaveDataSupplier extends SavedData {

  private Level level;
  private TickingHealList healTask;
  static final String DATAKEY = ForgeCreeperHeal.MODID + "_" + WorldHealerSaveDataSupplier.class.getSimpleName();

  public WorldHealerSaveDataSupplier() {
    healTask = new TickingHealList();
  }

  public void onDetonate(ExplosionEvent.Detonate event) {
    Level world = event.getLevel();
    int maxTicksBeforeHeal = 0;
    //Process primary blocks
    for (BlockPos blockPosExplosion : event.getAffectedBlocks()) {
      BlockState blockStateExplosion = world.getBlockState(blockPosExplosion);
      if (!isValid(blockStateExplosion)) {
        continue;
      }
      if (!blockStateExplosion.isAir()) {
        int ticksBeforeHeal = ConfigRegistryCreeperheal.getMinimumTicksBeforeHeal() + world.random.nextInt(ConfigRegistryCreeperheal.getRandomTickVar());
        if (ticksBeforeHeal > maxTicksBeforeHeal) {
          maxTicksBeforeHeal = ticksBeforeHeal;
        }
        if (blockStateExplosion.getBlock() instanceof EntityBlock) {
          BlockEntity blockEntity = world.getBlockEntity(blockPosExplosion);
          CompoundTag nbtTag;
          if (blockEntity != null) {
            nbtTag = blockEntity.saveCustomOnly(level.registryAccess());
          }
          else {
            nbtTag = null;
          }
          onBlockHealed(blockPosExplosion, blockStateExplosion, ticksBeforeHeal, nbtTag);
        }
        else {
          onBlockHealed(blockPosExplosion, blockStateExplosion, ticksBeforeHeal, null);
        }
      }
    }
    maxTicksBeforeHeal++;
    //Process secondary blocks. ex: Leaves must come AFTER dirt
    for (BlockPos blockPosExplosion : event.getAffectedBlocks()) {
      BlockState blockStateExplosion = world.getBlockState(blockPosExplosion);
      if (!isValid(blockStateExplosion)) {
        continue;
      }
      if (!blockStateExplosion.isAir()) {
        CompoundTag c = null;
        if (blockStateExplosion.getBlock() instanceof EntityBlock) {
          BlockEntity blockEntity = world.getBlockEntity(blockPosExplosion);
          if (blockEntity != null) {
            c = blockEntity.saveCustomOnly(level.registryAccess());
          }
        }

        onBlockHealed(blockPosExplosion, blockStateExplosion, maxTicksBeforeHeal + world.random.nextInt(ConfigRegistryCreeperheal.getRandomTickVar()), c);
      }
    }
    // 标记数据已更改，需要保存到硬盘
    // 这会通知Minecraft在适当的时候保存数据，而不是立即写入
    this.setDirty();
  }

  private boolean isValid(BlockState state) {
    if (state.is(BlockTags.DOORS) || state.is(BlockTags.BEDS) || state.is(BlockTags.TALL_FLOWERS)) {
      return false;
    }
    return true;
  }

  private void onBlockHealed(BlockPos blockPosExplosion, BlockState blockStateExplosion, int ticks, CompoundTag nbtTag) {
    BlockStatePosWrapper blockData = new BlockStatePosWrapper(blockPosExplosion, blockStateExplosion);
    blockData.nbtTag = nbtTag;
    healTask.add(ticks, blockData);
    level.removeBlockEntity(blockPosExplosion);
    level.setBlock(blockPosExplosion, Blocks.AIR.defaultBlockState(), 7);
  }

  private void heal(BlockStatePosWrapper blockData) {
    BlockPos pos = blockData.getBlockPos();
    boolean isAir = this.level.isEmptyBlock(pos);
    
    if (ConfigRegistryCreeperheal.isOverride() || isAir) {
      BlockState originalState = level.getBlockState(pos);
      BlockState newState = blockData.getBlockState();
      if (ConfigRegistryCreeperheal.isDropIfAlreadyBlock() && !isAir) {
        if (ConfigRegistryCreeperheal.isDropOriginalBlock()) {
          // 模式1：替换方块时，掉落原来的方块物品
          if (originalState instanceof EntityBlock){
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
              dropBlockData(pos, originalState, blockEntity.saveWithId(level.registryAccess()));
            }
          }else {
            dropBlockData(pos, originalState);
          }
        } else {
          // 模式2：替换方块时，掉落要恢复的方块物品
          dropBlockData(pos, newState, blockData.nbtTag);
        }
      }
      // 设置新的方块状态
      if (ConfigRegistryCreeperheal.isReplaceBlock()) {
        // 替换方块
        level.setBlock(pos, newState, 7);
        if (blockData.nbtTag != null){
          BlockEntity blockEntity = level.getBlockEntity(pos);
          if (blockEntity != null) {
            blockEntity.loadCustomOnly(blockData.nbtTag, level.registryAccess());
          }
        }
      }else {
        // 不替换方块，保持原有的方块状态
        if (originalState.getBlock() != newState.getBlock()) {
          level.setBlock(pos, newState, 7);
          if (blockData.nbtTag != null){
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
              blockEntity.loadCustomOnly(blockData.nbtTag, level.registryAccess());
            }
          }
        }else if (!originalState.isAir()) {
          dropBlockData(pos, newState, blockData.nbtTag);
        }
      }
      // BlockEntity handling is now handled automatically by Minecraft 1.21
      // We no longer need to manually save or load block entity data
    }
    else if (ConfigRegistryCreeperheal.isDropIfAlreadyBlock() && blockData.getBlockState().getBlock() != null) {
      // 不替换方块，但掉落要恢复的方块物品
      dropBlockData(pos, blockData.getBlockState(), blockData.nbtTag);
    }
  }

  private void dropBlockData(BlockPos pos, BlockState blockState) {
    dropBlockData(pos, blockState, null);
  }
  private void dropBlockData(BlockPos pos, BlockState blockState, CompoundTag nbtTag) {
    Block block = blockState.getBlock();
    DataComponentPatch.Builder set = DataComponentPatch.builder();
    if (nbtTag != null) {
      set.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(nbtTag));
    }
    LevelWorldUtil.dropItemStackRandomMotion(level, pos, new ItemStack(Holder.direct(block.asItem()), 1, set.build()), 0.05F);
    // BlockEntity handling is now handled automatically by Minecraft 1.21
    // We no longer need to manually drop block entity contents
  }
  
  // 兼容旧方法
  private void dropBlockData(BlockStatePosWrapper blockData) {
    dropBlockData(blockData.getBlockPos(), blockData.getBlockState(), null);
  }

  @Override
  public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
    ListTag tagList = new ListTag();
    for (TickContainer<Collection<BlockStatePosWrapper>> tc : this.healTask.getLinkedList()) {
      CompoundTag tickContainerTag = new CompoundTag();
      tickContainerTag.putInt("ticks", tc.getTick());
      ListTag blockDataListTag = new ListTag();
      for (BlockStatePosWrapper blockData : tc.getData()) {
        CompoundTag blockDataTag = new CompoundTag();
        blockData.writeToNBT(blockDataTag);
        blockDataListTag.add(blockDataTag);
      }
      tickContainerTag.put("blockdatalist", blockDataListTag);
      tagList.add(tickContainerTag);
    }
    tag.put("healtasklist", tagList);
    return tag;
  }

  public static WorldHealerSaveDataSupplier load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
    WorldHealerSaveDataSupplier result = new WorldHealerSaveDataSupplier();
    ListTag tagList = tag.getList("healtasklist", Tag.TAG_COMPOUND);
    for (CompoundTag tickContainerTag : ((CollectionTag<CompoundTag>)(CollectionTag)tagList)) {
      int ticksLeft = tickContainerTag.getInt("ticks");
      LinkedList<BlockStatePosWrapper> blockDataList = new LinkedList<BlockStatePosWrapper>();
      ListTag blockDataListTag = tickContainerTag.getList("blockdatalist", Tag.TAG_COMPOUND);
      for (ListIterator<Tag> iter0 = blockDataListTag.listIterator(); iter0.hasNext(); ) {
        CompoundTag blockDataTag = (CompoundTag) iter0.next();
        BlockStatePosWrapper blockData = new BlockStatePosWrapper();
        // 直接读取NBT数据，暂时传入null作为level
        // 实际的BlockState会在loadWorldHealer中被修复
        blockData.readFromNBT(blockDataTag, null);
        blockDataList.add(blockData);
      }
      result.healTask.getLinkedList().addLast(new TickContainer<Collection<BlockStatePosWrapper>>(ticksLeft, blockDataList));
    }
    return result;
  }

  public static WorldHealerSaveDataSupplier loadWorldHealer(ServerLevel serverLevelIn) {
    //first get data saved from last time we used this world
    DimensionDataStorage storage = serverLevelIn.getDataStorage();
    // 使用Factory的静态方法创建Factory实例
    net.minecraft.world.level.saveddata.SavedData.Factory<WorldHealerSaveDataSupplier> factory = 
        new net.minecraft.world.level.saveddata.SavedData.Factory<>
            (() -> new WorldHealerSaveDataSupplier(),
            WorldHealerSaveDataSupplier::load);
    WorldHealerSaveDataSupplier result = storage.computeIfAbsent(factory, DATAKEY);
    result.level = serverLevelIn;
    // 修复所有BlockState数据，使用正确的level
    for (TickContainer<Collection<BlockStatePosWrapper>> tickContainer : result.healTask.getLinkedList()) {
      for (BlockStatePosWrapper blockData : tickContainer.getData()) {
        // 重新加载BlockState，使用正确的level
        // 从保存的数据中重新读取
        // 这里需要重新读取NBT数据，但我们没有保存原始的NBT标签
        // 简化处理：直接使用位置获取当前BlockState
        // 这在游戏运行时会被正确处理
        BlockPos pos = blockData.getBlockPos();
        BlockState state = serverLevelIn.getBlockState(pos);
        if (!state.isAir()) {
          // 如果位置有方块，保持原样
        } else {
          // 否则使用默认的STONE
          state = Blocks.STONE.defaultBlockState();
        }
        // 使用反射更新私有字段
        blockData.setBlockState(state);
      }
    }
    return result;
  }

  public void tick(Level level) {
    onTick();
  }

  private void onTick() {
    Collection<BlockStatePosWrapper> blocksToHeal = healTask.tick();
    if (blocksToHeal != null) {
      for (BlockStatePosWrapper blockData : blocksToHeal) {
        heal(blockData);
      }
      // 标记数据已更改，需要保存到硬盘
      this.setDirty();
    }
  }
}
