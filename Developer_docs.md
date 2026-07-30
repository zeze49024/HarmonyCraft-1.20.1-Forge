# Developer开发者文档

## 方块、物品、创造模式物品栏类的创建

```java
// Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
// Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
// Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

// Creates a new Block with the id "examplemod:example_block", combining the namespace and path
public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
// Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

// Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
        .alwaysEat().nutrition(1).saturationMod(2f).build())));

// Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
        .withTabsBefore(CreativeModeTabs.COMBAT)
        .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
        .displayItems((parameters, output) -> {
            output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
        }).build());
```

```java
// Register the Deferred Register to the mod event bus so blocks get registered
BLOCKS.register(modEventBus);
// Register the Deferred Register to the mod event bus so items get registered
ITEMS.register(modEventBus);
// Register the Deferred Register to the mod event bus so tabs get registered
CREATIVE_MODE_TABS.register(modEventBus);
```

## datagen（**“数据生成”系统**）文件下的各类含义

| **类名**                       | 含义                 |
| ------------------------------ | -------------------- |
| **ModBlockLootTablesProvider** | 方块战利品列表       |
| **ModBlockStateProvider**      | 方块状态文件         |
| **ModBlockTagsProvider**       | 方块标签文件         |
| **ModItemModelsProvider**      | 物品模型文件         |
| **ModItemTagsProvider**        | 物品标签文件         |
| **ModRecipesProvider**         | 合成配方文件         |
| **ModZhCnLangProvider**        | 生成简体中文语言文件 |
| **ModEnUsLangProvider**        | 生成英文语言文件     |
| **ModJaJpLangProvider**        | 生成日文语言文件     |

## lang文件下的翻译对照表

| 注册ID                  | 类型         | zh-cn                | en-us                | ja-jp |
| ----------------------- | ------------ | -------------------- | -------------------- | ----- |
| itemGroup.road_tab      | 物品栏       | HarmonyCraft \| 道路 | HarmonyCraft \| Road |       |
| HARMONY_LOGO            | 物品         | 花梦工艺Logo         | Harmony Logo         |       |
| ASPHALT_BLOCK           | 方块         | 沥青道路             | Asphalt Block        |       |
| ASPHALT_SLAB            | 方块（半砖） | 沥青道路半砖         | Asphalt Slab         |       |
| ASPHALT_WHITE_STR_BLOCK | 方块         | 沥青道路半砖         | White Line           |       |
| ASPHALT_WHITE_STR_SLAB  | 方块（半砖） | 沥青道路白线半砖     | White Line Slab      |       |









## 方块不同面引用不同贴图 —— 完整总结

### 一、核心概念：两个 JSON 文件的职责分离

| 文件位置                               | 作用                                    | 关键字段                                                 |
| -------------------------------------- | --------------------------------------- | -------------------------------------------------------- |
| `assets/<modid>/blockstates/xxx.json`  | 把 BlockState（属性组合）映射到模型文件 | `variants` → `{ "model": "...", "y": 180 }`、`multipart` |
| `assets/<modid>/models/block/xxx.json` | 定义方块的几何形状和贴图                | `parent`、`textures`、`elements`                         |

**关键铁律**：
- `parent` 和 `textures` 字段**只能出现在 `models/` 下的文件里**。
- blockstates 文件的 variant 值里只能有 `model`、`x`、`y`、`uvlock` 等字段，**不能写 `parent`/`textures`**。
- 写错位置会导致 Minecraft 找不到 `model` 字段 → 回退到缺失模型 → **紫黑块**。

---

### 二、四種实现方法（按场景选择）

#### 方法一：`minecraft:block/cube`（6 面各不同）

适用：6 个面全部使用不同贴图的完整方块。

```json
{
  "parent": "minecraft:block/cube",
  "textures": {
    "particle": "harmonycraft_mod:block/asphalt_block",
    "down":  "harmonycraft_mod:block/asphalt_block_top",
    "up":    "harmonycraft_mod:block/asphalt_block_top",
    "north": "harmonycraft_mod:block/asphalt_block_side",
    "south": "harmonycraft_mod:block/asphalt_block_side",
    "east":  "harmonycraft_mod:block/asphalt_block_side",
    "west":  "harmonycraft_mod:block/asphalt_block_side"
  }
}
```

#### 方法二：`minecraft:block/cube_column`（柱状：上下相同、四侧相同）

适用：上下底面一张、四个侧面另一张（最简洁）。

```json
{
  "parent": "minecraft:block/cube_column",
  "textures": {
    "end":   "harmonycraft_mod:block/asphalt_block_top",
    "side":  "harmonycraft_mod:block/asphalt_block_side"
  }
}
```

#### 方法三：`minecraft:block/cube_directional` + 朝向属性（带朝向的方块）

适用：方块需要随玩家放置方向旋转（如带箭头的路面）。

需要 **3 步配合**：

**1. Java 方块类注册 FACING 属性**（继承 Block，重写 3 个方法）：
- `createBlockStateDefinition` —— 把 `FACING` 加到 BlockState 定义
- `getStateForPlacement` —— 放置时根据玩家朝向写入 FACING
- 构造函数中 `registerDefaultState(...)` 设置默认朝向

**2. blockstates 文件写 4 个 facing variant**：
```json
{
  "variants": {
    "facing=north": { "model": "harmonycraft_mod:block/asphalt_white_str_block", "y": 0   },
    "facing=south": { "model": "harmonycraft_mod:block/asphalt_white_str_block", "y": 180 },
    "facing=east":  { "model": "harmonycraft_mod:block/asphalt_white_str_block", "y": 90  },
    "facing=west":  { "model": "harmonycraft_mod:block/asphalt_white_str_block", "y": 270 }
  }
}
```

**3. 模型文件**用 `cube` 即可（朝向靠 blockstates 的 `y` 旋转实现，不需要靠模型本身）。

#### 方法四：自定义 `elements`（最灵活）

适用：以上都不满足（如半砖要 4 个侧面各不同、需要自定义 UV 等）。

```json
{
  "parent": "minecraft:block/block",
  "textures": {
    "top":   "harmonycraft_mod:block/asphalt_block_top",
    "side":  "harmonycraft_mod:block/asphalt_block_side",
    "front": "harmonycraft_mod:block/asphalt_block_front"
  },
  "elements": [
    { "from": [0,0,0], "to": [16,16,16],
      "faces": {
        "down":  { "texture": "#top",   "cullface": "down"  },
        "up":    { "texture": "#top",   "cullface": "up"    },
        "north": { "texture": "#front", "cullface": "north" },
        "south": { "texture": "#side",  "cullface": "south" },
        "east":  { "texture": "#side",  "cullface": "east"  },
        "west":  { "texture": "#side",  "cullface": "west"  }
      }
    }
  ]
}
```

要点：
- `#xxx` 引用 `textures` 中定义的变量
- `cullface` 表示相邻方块遮挡时该面会被剔除（性能优化 + 防止 Z-fighting）
- `from`/`to` 是方块内坐标 0~16，下半砖是 `[0,0,0]`→`[16,8,16]`，上半砖是 `[0,8,0]`→`[16,16,16]`

---

### 三、各父模型贴图变量对照表（**踩坑高发区**）

| 父模型                             | 贴图变量                                                   | 用途                 |
| ---------------------------------- | ---------------------------------------------------------- | -------------------- |
| `minecraft:block/cube_all`         | `all`                                                      | 全 6 面同一贴图      |
| `minecraft:block/cube`             | `particle`, `down`, `up`, `north`, `south`, `east`, `west` | 6 面独立             |
| `minecraft:block/cube_column`      | `end`, `side`                                              | 柱状                 |
| `minecraft:block/cube_directional` | `particle`, `down`, `up`, `north`, `south`, `east`, `west` | 带朝向（不自动旋转） |
| `minecraft:block/slab`             | `particle`, `bottom`, `top`, `side`                        | 半砖                 |
| `minecraft:block/slab_top`         | `particle`, `bottom`, `top`, `side`                        | 上半砖               |
| `minecraft:block/stairs`           | `particle`, `bottom`, `top`, `side`                        | 楼梯                 |
| `minecraft:block/fence_side`       | `texture`                                                  | 栅栏                 |
| `minecraft:block/cross`            | `cross`                                                    | 花、草、菌类         |

**最常见错误**：把 `cube` 的 `down/up/north/south/east/west` 变量名用到了 `slab`/`stairs` 上 → 父模型引用的是 `#bottom/#top/#side`，解析不到 → 紫黑块。

---

### 四、注意事项清单

#### 1. 贴图文件必须实际存在
- 路径：`src/main/resources/assets/<modid>/textures/block/xxx.png`
- 引用时只写 `命名空间:block/xxx`（不带 `.png` 后缀）
- 贴图缺失 → 该面紫黑

#### 2. `particle` 字段建议显式指定
- 控制方块破碎时的粒子贴图
- 不写会从 `down` 或 `bottom` 继承，可能不是你想要的
- 缺失粒子贴图不报紫黑，但会显示默认石头粒子

#### 3. 半砖的 `type=double` 必须指向**完整方块模型**
错误示例（指向半砖模型 → 合成完整方块时只显示下半截）：
```json
"type=double": { "model": "harmonycraft_mod:block/asphalt_white_str_slab" }
```
正确：
```json
"type=double": { "model": "harmonycraft_mod:block/asphalt_white_str_block" }
```

#### 4. 改了 `generated/` 目录下的 JSON 后必须同步改 datagen
- `src/generated/resources/` 下的 JSON 是 datagen 自动生成的
- 直接手改的话，下次跑 `.\gradlew runData` 会被覆盖
- **正确做法**：改 [ModBlockStateProvider.java](file:///d:\myCodeProject\minecraftModCode\HarmonyCraft-1.20.1-Forge\src\main\java\com\zeze\harmony\datagen\ModBlockStateProvider.java) 的逻辑，让 datagen 生成出正确的 JSON
- 现有 `simpleBlockWithItem` + `cubeAll(...)` 只能生成单一贴图，要做多面贴图需要换成手写 `ConfiguredModel` 或自定义 helper

#### 5. 注册名不要随便改
- 改 Java 类（如 `Block` → `AsphaltWhiteStrBlock`）时，注册名 `asphalt_white_str_block` 保持不变
- 否则存档里已放置的方块会消失

#### 6. `cube_directional` **不会自动旋转**
- 它只是 6 面各放一张贴图的方块，跟 `cube` 几乎一样
- 真正的旋转靠 blockstates 文件里 variant 的 `y` 字段
- 想要朝向必须配合 Java 方块类的 FACING 属性

#### 7. 朝向方块需要的 Java 改动顺序
1. 新建继承 Block 的子类，注册 `HORIZONTAL_FACING` 属性
2. 在 [ModBlocks.java](file:///d:\myCodeProject\minecraftModCode\HarmonyCraft-1.20.1-Forge\src\main\java\com\zeze\harmony\block\ModBlocks.java) 用新子类替换 `new Block(...)`
3. 在 [ModBlockStateProvider.java](file:///d:\myCodeProject\minecraftModCode\HarmonyCraft-1.20.1-Forge\src\main\java\com\zeze\harmony\datagen\ModBlockStateProvider.java) 用 `getVariantBuilder(...).forAllStates(...)` 生成 4 个 variant
4. 跑 `.\gradlew runData` 自动生成 JSON

#### 8. 模型文件正面（north）= 默认朝向
- 写朝向方块时，模型文件中 `north` 面的贴图就是「箭头默认指向北」时的正面
- blockstates 的 `y` 旋转是相对 north 计算的， south=180, west=270, east=90

---

### 五、常见紫黑块排查流程

1. **检查 blockstates JSON 结构**：variant 值里有没有 `model` 字段？有没有误写 `parent`/`textures`？
2. **检查模型文件 parent**：贴图变量名是否匹配父模型？（参照对照表）
3. **检查贴图文件**：`src/main/resources/assets/<modid>/textures/block/` 下贴图是否存在？文件名是否拼写正确？
4. **检查 `type=double`**：半砖合成完整方块时是否指向了正确模型？
5. **看游戏日志**：启动时控制台会输出具体哪个文件解析失败、哪一行有问题
6. **检查 datagen 是否覆盖**：如果你手改了 `generated/` 下的 JSON，再跑 `runData` 会被覆盖回旧版本

---

### 六、本项目（HarmonyCraft）文件改动地图

| 文件                                           | 改动内容                                                     |
| ---------------------------------------------- | ------------------------------------------------------------ |
| `blockstates/asphalt_white_str_block.json`     | 改成正确格式：variant 只放 `model` 字段                      |
| `models/block/asphalt_white_str_block.json`    | 改 parent 为 `cube`，textures 用 `down/up/north/south/east/west` |
| `blockstates/asphalt_white_str_slab.json`      | `type=double` 改指向 `asphalt_white_str_block`               |
| `models/block/asphalt_white_str_slab.json`     | textures 变量名改成 `bottom/top/side`                        |
| `models/block/asphalt_white_str_slab_top.json` | 同上，parent 保持 `slab_top`                                 |
| `block/AsphaltWhiteStrBlock.java`（新建）      | 注册 FACING 属性（仅方案三需要）                             |
| `block/ModBlocks.java`                         | `new Block(...)` 换成 `new AsphaltWhiteStrBlock(...)`（仅方案三需要） |
| `datagen/ModBlockStateProvider.java`           | 改用 `getVariantBuilder().forAllStates()` 生成 4 个 variant，避免 datagen 覆盖手改的 JSON |
