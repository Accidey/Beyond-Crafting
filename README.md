# 超越合成 Beyond Crafting

[超越维度](https://github.com/Frostbite-time/BeyondDimensions)（Beyond Dimensions）的附属模组：给维度网络加一个"深度合成"页签，把配方一路展开到网络里真实存在的材料为止，一次扣料、一次产出。

举例：网络里只有钻石块，想合成钻石胸甲。原版需要先把钻石块拆成钻石、再摆成胸甲形状；本模组直接把整条链路算完，点一次就得到胸甲。

## 功能

- **递归子配方**：目标物品缺料时沿配方树向下展开，直到网络里有现成的材料为止。
- **压缩块拆解**：把"9 个同种物品 → 1 个方块"的配方反向使用，例如钻石块拆成 9 个钻石、煤炭块拆成 9 个煤炭。可用配置关闭，也可按物品 id 拉黑。
- **页签界面**：注入维度存储界面的左侧竖条，搜索配方产物（支持中文、拼音首字母、注册 id），显示材料清单或具体缺少的材料及数量。
- **配方查看器跳转**：在 JEI 或 EMI 里点"+"，直接跳到本页并预选该产物，不再往合成格塞东西。
- **原子提交**：材料对不上就一个都不扣，网络放不下产物就不动手；成功后产物与余数一并进入网络。

## 环境

| 项目 | 版本 |
| --- | --- |
| Minecraft | 1.21.1 |
| 加载器 | NeoForge 21.1.235 及以上 |
| 前置 | Beyond Dimensions 0.8.0 及以上（其自身要求 CompixelUI 与 Kotlin） |
| 可选 | JEI、EMI（用于"+"跳转，没有也能用页签） |

## 构建

`libs/` 下三个 jar 是构建依赖，已在仓库中：Beyond Dimensions 本体、JEI、EMI。JEI 与 EMI 只用于编译对应的可选集成。

```
./gradlew build
```

产物：`build/libs/beyondcrafting-1.21.1-neoforge-<版本>.jar`

## 配置

`config/beyondcrafting-common.toml`，全部支持热重载。

| 配置项 | 默认 | 说明 |
| --- | --- | --- |
| `enableDeepCrafting` | true | 总开关 |
| `allowReverseCompression` | true | 是否允许把 9 合 1 的方块拆回 9 个物品 |
| `maxRecipeDepth` | 8 | 递归层数上限 |
| `maxPlanNodes` | 4096 | 单次求解考察的节点上限 |
| `reverseBlockBlacklist` | 空 | 不允许拆解的物品 id |

## 结构

| 路径 | 作用 |
| --- | --- |
| `craft/RecipeGraph` | 产物 → 配方索引，含压缩块逆向的虚拟配方 |
| `craft/RecipeGraphCache` | 索引缓存，客户端与服务端各一份，数据包重载时失效 |
| `craft/DeepCraftSolver` | 贪心递归求解，输出带缺料清单的 `CraftPlan` |
| `craft/DeepCraftService` | 两阶段提交：先模拟校验，再扣料入网 |
| `net/DeepCraftActions` | 注册到维度网络菜单的服务端动作 |
| `client/page/DeepCraftPage.kt` | 注入 BD 界面的页签 |
| `mixin/` | 三处注入：切换页签、服务端配方转移兜底、JEI 与 EMI 的"+" |

## 已知限制

- 只处理工作台配方（`RecipeType.CRAFTING`），熔炼、锻造、机器配方不参与递归。
- 服务端始终操作玩家的**主网络**；在非主网络的界面里点合成，扣的是主网络的料。
- 配方里带容器的返还物（如桶）按 3×3 输入重建后计算，个别依赖格子位置的配方可能与实际有出入。
- 同一组材料对应多个产物时，服务端兜底路径的产物是推断出来的；JEI 与 EMI 的"+"路径用的是配方对象本身，不会有这个问题。

## 许可证

MIT
