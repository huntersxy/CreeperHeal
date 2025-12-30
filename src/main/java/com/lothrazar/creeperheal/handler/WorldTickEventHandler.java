package com.lothrazar.creeperheal.handler;

import com.lothrazar.creeperheal.worldhealer.WorldHealerSaveDataSupplier;

import com.lothrazar.creeperheal.ForgeCreeperHeal;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

public class WorldTickEventHandler {

  public WorldTickEventHandler() {
    NeoForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onWorldTick(LevelTickEvent.Post event) {
    if (!event.getLevel().isClientSide()) {
      WorldHealerSaveDataSupplier heal = ForgeCreeperHeal.getWorldHealer((ServerLevel) event.getLevel());
      if (heal != null) {
        heal.tick(event.getLevel());
      }
    }
  }
}
