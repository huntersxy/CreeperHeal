## 迁移计划：将 Forge 导入改为 NeoForge

### 问题分析
项目已配置 NeoForge Gradle 插件，但代码中仍使用 `net.minecraftforge` 包，这在 NeoForge 1.21 中已不存在，导致编译错误。

### 修改步骤

1. **修改配置相关导入**
   - 文件：`ConfigRegistryCreeperheal.java`
   - 将 `net.minecraftforge.common.ForgeConfigSpec` 改为 `net.neoforged.neoforge.common.Configuration` 或对应 NeoForge 配置类

2. **修改主类注解和事件总线**
   - 文件：`ForgeCreeperHeal.java`
   - 将 `@Mod` 注解包从 `net.minecraftforge.fml.common.Mod` 改为 `net.neoforged.fml.common.Mod`
   - 更新事件总线相关导入

3. **修改事件处理器导入**
   - 文件：`ExplosionEventHandler.java`、`WorldEventHandler.java`、`WorldTickEventHandler.java`
   - 将所有 `net.minecraftforge.event` 改为 `net.neoforged.neoforge.event`
   - 将 `net.minecraftforge.eventbus.api.SubscribeEvent` 改为 `net.neoforged.bus.api.SubscribeEvent`

4. **修改世界治疗器相关导入**
   - 文件：`WorldHealerSaveDataSupplier.java`
   - 检查并更新任何 Forge 相关导入

5. **更新类名（可选）**
   - 考虑将 `ForgeCreeperHeal` 改为 `NeoForgeCreeperHeal` 以保持一致性

### 预期结果
- 所有编译错误消失
- 项目成功使用 NeoForge API 构建
- 保持原有功能不变

### 风险评估
- 配置系统可能需要较大调整，因为 NeoForge 1.21 可能有不同的配置API
- 事件系统可能有细微变化，需要测试确保事件仍能正确触发
- 主类初始化流程可能需要调整

### 测试建议
- 编译项目确保无错误
- 启动游戏测试基本功能
- 测试爆炸后自动修复功能
- 测试配置选项是否正常工作