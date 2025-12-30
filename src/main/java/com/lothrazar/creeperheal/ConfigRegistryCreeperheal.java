package com.lothrazar.creeperheal;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

/**
 * Configuration class for CreeperHeal mod.
 * Uses NeoForge 1.21.1 official config API.
 */
public class ConfigRegistryCreeperheal {

  // Public static instance of the config
  public static final ConfigRegistryCreeperheal CONFIG;
  // The config specification
  public static final ModConfigSpec CONFIG_SPEC;
  
  // Config values
  public final IntValue minTicksBeforeHeal;
  public final IntValue randomTickVar;
  public final BooleanValue overrideBlocks;
  public final BooleanValue dropIfAlreadyBlock;
  public final BooleanValue dropOriginalBlock;
  public final BooleanValue replaceBlock;
  public final BooleanValue onlyCreepers;

  // Static block to build the config
  static {
    // Create a builder and configure it with our config class
    final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    CONFIG = new ConfigRegistryCreeperheal(BUILDER);
    CONFIG_SPEC = BUILDER.build();
  }

  /**
   * Constructor to build the config values
   * @param builder ModConfigSpec.Builder to use for building the config
   */
  private ConfigRegistryCreeperheal(ModConfigSpec.Builder builder) {
    // Start a new section for general settings
    builder.push("general");
    
    // Define config values
    minTicksBeforeHeal = builder
        .comment("A lower number means it will start healing faster")
        .defineInRange("tickStartDelay", 600, 1, 600000);
    
    randomTickVar = builder
        .comment("Determines the random nature of the heal. Time between in ticks is the minimum + rand(1,this)")
        .defineInRange("tickRandomInterval", 1200, 1, 600000);
    
    overrideBlocks = builder
        .comment("If the healing will replace blocks that were put in after (such as fallen gravel or placed blocks)")
        .define("overrideBlocks", true);
    
    dropIfAlreadyBlock = builder
        .comment("If this is true (and we are not overriding blocks), and a block tries to get healed but something is in the way, then that block will drop as an itemstack on the ground")
        .define("dropBlockConflict", true);
    
    dropOriginalBlock = builder
        .comment("If true, when healing over an existing block, drop the ORIGINAL block (that's in the way) as items. If false, drop the HEALING block (that's trying to replace it) as items.")
        .define("dropOriginalBlock", false);

    replaceBlock = builder
        .comment("If true, when healing over an existing block, replace the ORIGINAL block (that's in the way) with the HEALING block. If false, do not replace the block.")
        .define("replaceBlock", true);
    
    onlyCreepers = builder
        .comment("If this is true, only creeper explosions are healed. Otherwise, all explosions will be healed (TNT, stuff from other mods, etc)")
        .define("onlyCreepers", true);
    
    // End the general section
    builder.pop();
  }

  /**
   * Registers the config with NeoForge
   * @param container ModContainer to use for registration
   */
  public static void registerConfig(ModContainer container) {
    // Register the config using the mod container
    container.registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC, "creeperheal.toml");
  }

  /**
   * Gets the minimum ticks before healing starts
   * @return minimum ticks before healing
   */
  public static int getMinimumTicksBeforeHeal() {
    return CONFIG.minTicksBeforeHeal.get();
  }

  /**
   * Gets the random tick variance for healing
   * @return random tick variance
   */
  public static int getRandomTickVar() {
    return CONFIG.randomTickVar.get();
  }

  /**
   * Gets whether healing should override existing blocks
   * @return true if healing should override blocks
   */
  public static boolean isOverride() {
    return CONFIG.overrideBlocks.get();
  }

  /**
   * Gets whether conflicting blocks should drop as items
   * @return true if conflicting blocks should drop as items
   */
  public static boolean isDropIfAlreadyBlock() {
    return CONFIG.dropIfAlreadyBlock.get();
  }

  /**
   * Gets whether only creeper explosions should be healed
   * @return true if only creeper explosions should be healed
   */
  public static boolean isOnlyCreepers() {
    return CONFIG.onlyCreepers.get();
  }
  
  /**
   * Gets whether to drop the original block when healing over an existing block
   * @return true if original block should be dropped, false if healing block should be dropped
   */
  public static boolean isDropOriginalBlock() {
    return CONFIG.dropOriginalBlock.get();
  }

  /**
   * Gets whether to replace the original block when healing over an existing block
   * @return true if original block should be replaced, false if not
   */
  public static boolean isReplaceBlock() {
    return CONFIG.replaceBlock.get();
  }
}
