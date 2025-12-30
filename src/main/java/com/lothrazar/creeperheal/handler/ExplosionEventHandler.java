package com.lothrazar.creeperheal.handler;

import com.lothrazar.creeperheal.ConfigRegistryCreeperheal;
import com.lothrazar.creeperheal.ForgeCreeperHeal;
import com.lothrazar.creeperheal.worldhealer.WorldHealerSaveDataSupplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ExplosionEventHandler {

  public ExplosionEventHandler() {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onDetonate(ExplosionEvent.Detonate event) {
    if (event.getLevel().isClientSide()) {
      return;
    }
    Entity exploder = event.getExplosion().getDirectSourceEntity(); // .getSourceMob();
    boolean isCreeper = exploder instanceof Creeper;
    if (ConfigRegistryCreeperheal.isOnlyCreepers() == false ||
        (ConfigRegistryCreeperheal.isOnlyCreepers() && isCreeper)) {
      //if only creeper is false, dont need to check
      //only creepers allowed in, so it better be one
      WorldHealerSaveDataSupplier worldHealer = ForgeCreeperHeal.getWorldHealer((ServerLevel) event.getLevel());
      if (worldHealer != null) {
        worldHealer.onDetonate(event);
      }
    }
  }
}
