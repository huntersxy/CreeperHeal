package com.lothrazar.creeperheal.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LevelWorldUtil {
    public static void dropItemStackRandomMotion(Level level, BlockPos pos, ItemStack stack, float motion) {
        if (stack.isEmpty()) {
            return;
        }

        double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
        double y = pos.getY() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
        double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;

        ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
        itemEntity.setDeltaMovement(
            (level.random.nextDouble() - 0.5) * motion,
            (level.random.nextDouble() - 0.5) * motion,
            (level.random.nextDouble() - 0.5) * motion
        );
        level.addFreshEntity(itemEntity);
    }
}