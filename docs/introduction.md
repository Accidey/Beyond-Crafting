# Beyond Crafting

**Deep crafting for your Beyond Dimensions network — turn what you have into what you need, in one click.**

---

## The problem

Beyond Dimensions gives you a network that stores everything. Crafting, though, still works the old way: to make a diamond chestplate you first have to pull out a diamond block, break it back into nine diamonds, put five of them into the right shape, and only then take the result.

The network already knows what you own. It should be able to do the boring part for you.

## What this mod does

Beyond Crafting adds a **Deep Crafting** page to the network screen (default key `O`).

Pick what you want to end up with. The mod walks the recipe chain downwards — recipe after recipe — until it reaches materials the network is actually holding, takes everything it needs in a single step, and puts the finished item back into the network.

If the chain is longer than what you have, you get an exact shopping list instead: what is missing, and how many of it. Nothing is taken when the chain cannot be completed.

## Features

- **🏗️ One click, whole chain.** Ask for a finished item; intermediate steps are resolved automatically. A diamond block in storage is enough to make a diamond chestplate.
- **📦 Compression blocks count as materials.** Any block that is crafted from nine identical items can be used the other way around — a diamond block supplies nine diamonds, a block of coal supplies nine coal. This can be turned off, and individual items can be excluded.
- **🧾 Exact material lists.** Before you commit, the page shows what will be consumed. When something is missing, it says which material and how many, not just "insufficient materials".
- **🔍 Search that understands players.** Find a target by its name, by pinyin initials, or by its item id. Pick a quantity from 1 up to 4096.
- **🎯 Works with JEI and EMI.** Pressing the "+" transfer button in either recipe viewer opens the Deep Crafting page with that recipe's result already selected, instead of filling the crafting grid.
- **🔒 All or nothing.** Materials are only deducted when the entire chain can be satisfied. A failed attempt changes nothing.
- **♻️ Nothing is lost.** Leftovers from recipes that produce more than one item per craft, and containers returned by a recipe (buckets and the like), go back into the network. Anything the network cannot hold is handed to you.
- **⚖️ Respects your network.** Everything goes through the network's own storage rules and capacity limits, exactly as if you had moved the items by hand. If the network is full, the craft does not happen.

## What it does not do

Being precise here saves surprises:

- Only **crafting-table recipes** are followed — shaped and shapeless. Smelting, blasting, smoking, smithing and machine recipes are not part of the chain.
- It uses **only what is in the network**, never your inventory or the crafting grid.
- It always works on your **primary network**. If you are looking at a different network, the materials still come from your primary one.
- Recipes that need a container returned in a particular grid position are approximated, because the recipe is evaluated outside the grid.

## Requirements

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.235 or newer |
| Required | Beyond Dimensions 0.8.0 or newer |
| Optional | JEI or EMI, for the "+" shortcut |

Beyond Dimensions itself requires CompixelUI and a Kotlin runtime, so you already have them.

## Configuration

A few options, all applied without restarting the game:

- Turn the whole feature on or off.
- Allow or disallow unpacking compression blocks, with a list of items that should never be unpacked.
- Limit how deep a recipe chain may be followed, and how much work a single request may cost.

## A note on balance

Deep crafting does not create items out of nothing. It only performs the steps you could have performed by hand, in the right order, with the same inputs and the same outputs. If a recipe is expensive, the result is expensive.

---

# 超越合成

**为你的超越维度网络做深度合成：手上有什么，就一步做出想要的东西。**

---

## 问题在哪

超越维度给了你一个能装下一切的维度网络，但合成还是老样子：想做一件钻石胸甲，得先把钻石块取出来、拆回九个钻石、摆成胸甲的形状，再取走成品。

网络本来就清楚你有哪些东西，这些机械的步骤应该由它代劳。

## 这个模组做什么

超越合成在维度网络界面（默认按 `O` 打开）里加了一页**深度合成**。

你只要选出最终想要的东西。模组会沿着配方一层层往下展开，直到碰上网络里真实存在的材料，然后一次性扣齐、把成品放回网络。

如果链条走不到底，你得到的是一份准确的清单：缺什么、缺几个。链条走不通时，一个材料都不会扣。

## 功能

- **🏗️ 一次点击，整条链走完。** 你只需要目标成品，中间产物自动处理。网络里只有钻石块，也能直接得到钻石胸甲。
- **📦 压缩方块也算材料。** 凡是"九个同种物品合成一个方块"的东西都能反着用——一个钻石块顶九个钻石，一个煤炭块顶九个煤炭。可以整体关闭，也可以把个别物品排除在外。
- **🧾 精确的材料清单。** 动手之前就能看到会消耗什么；材料不够时，会说明是哪种材料、差多少个，而不是笼统的一句"材料不足"。
- **🔍 会读玩家心思的搜索。** 按名称、按拼音首字母、按物品 id 都能找到目标，数量可以在 1 到 4096 之间自己填。
- **🎯 与 JEI、EMI 联动。** 在任一配方查看器里点"+"转移按钮，会直接打开深度合成页并选中该配方的产物，不再往合成格里塞东西。
- **🔒 全有或全无。** 只有整条链都能走通才会扣材料；失败一次，什么都不会变。
- **♻️ 不漏东西。** 一次合成产出多个物品时多出来的部分、以及配方返还的容器（比如桶），都会回到网络；网络装不下的则交给你。
- **⚖️ 遵守网络的规矩。** 所有进出都走网络自己的存储规则与容量上限，和你手动搬运完全一样。网络放不下，就不会合成。

## 它不做的事

说清楚这些，可以少很多意外：

- 只处理**工作台配方**（有序与无序）。熔炼、高炉、烟熏、锻造以及各类机器配方都不在链条内。
- 只使用**网络里**的材料，不动背包，也不动合成格。
- 永远作用于你的**主网络**。即使你正在看另一个网络，材料仍来自主网络。
- 需要在合成格里特定位置返还容器的配方，结果按近似处理——因为配方并不在合成格里实际摆放。

## 环境要求

| | |
|---|---|
| Minecraft | 1.21.1 |
| 加载器 | NeoForge 21.1.235 及以上 |
| 必需前置 | 超越维度 0.8.0 及以上 |
| 可选 | JEI 或 EMI，用于"+"快捷入口 |

超越维度本身要求 CompixelUI 与 Kotlin 运行库，所以这些你本来就有。

## 配置

选项不多，改动都不需要重启游戏：

- 总开关，可整体停用本功能。
- 是否允许拆解压缩方块，并可指定永不被拆解的物品。
- 配方链条最多展开多深，以及单次请求最多消耗多少计算。

## 关于平衡性

深度合成不会凭空造物。它只是替你把本来就能做的事按正确顺序做了一遍：同样的输入，同样的产出。配方贵，结果就贵。
