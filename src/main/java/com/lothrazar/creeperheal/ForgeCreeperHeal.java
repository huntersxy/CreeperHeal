package com.lothrazar.creeperheal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lothrazar.creeperheal.handler.ExplosionEventHandler;
import com.lothrazar.creeperheal.handler.WorldEventHandler;
import com.lothrazar.creeperheal.handler.WorldTickEventHandler;
import com.lothrazar.creeperheal.worldhealer.WorldHealerSaveDataSupplier;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@net.neoforged.fml.common.Mod(ForgeCreeperHeal.MODID)
public class ForgeCreeperHeal {

  public static final String MODID = "creeperheal";
  public static final Logger LOGGER = LoggerFactory.getLogger(ForgeCreeperHeal.class);
  private static WorldEventHandler WEV;

  public ForgeCreeperHeal(ModContainer container) {
    // Register the config using the mod container
    ConfigRegistryCreeperheal.registerConfig(container);
    
    ForgeCreeperHeal.WEV = new WorldEventHandler();
    NeoForge.EVENT_BUS.register(WEV);
    new WorldTickEventHandler();
    new ExplosionEventHandler();
  }

  public static WorldHealerSaveDataSupplier getWorldHealer(ServerLevel level) {
    return WEV.getWorldHealers().get(level);
  }
}
