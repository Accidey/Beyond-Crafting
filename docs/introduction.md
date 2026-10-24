# Beyond Crafting

An addon for Beyond Dimensions. It adds a **Deep Crafting** tab to the network screen and finishes the job the network starts: you name the item you want, and it works out how to get there from what you are actually holding.

## The short version

Your network is full of diamond blocks and not a single loose diamond. You want a chestplate. You click once. The block gets unpacked, the diamonds get moved, the chestplate gets crafted, and the result lands back in the network — along with the one diamond you didn't need.

If the chain can't be completed, nothing is taken. The page tells you what is missing, down to the last piece.

## How it behaves

- Only crafting-table recipes are followed, shaped and shapeless. Smelting, blasting, smithing and machine recipes are not part of the chain.
- Materials come out of the network only. Your inventory and the crafting grid are left alone.
- It always works on your primary network, even when you are looking at a different one.
- Everything goes through the network's own storage rules and capacity limits, exactly as if you had moved the items by hand. If the network is full, the craft does not happen.
- A recipe that returns a container — a bucket, say — is evaluated outside the grid, so the returned item is approximated rather than placed back into a specific slot.
- Compression blocks run backwards: nine identical items into one block, or one block back into nine items. You can turn that off, or exclude individual items.

## The screen

Search by name, by pinyin initials, or by item id, pick a quantity between 1 and 4096, and the page shows what will be consumed. If you press the "+" transfer button in JEI or EMI, it opens this page with that recipe's result already selected instead of filling the crafting grid.

## Does it break the game?

No. Nothing appears that you couldn't have made by hand. It performs the same steps in the right order, with the same inputs and the same outputs. If a recipe is expensive, the result is expensive.

## Configuration

Three options, all applied without restarting: the main switch, whether compression blocks may be unpacked (plus a list of items that never should be), and how deep a chain may be followed.

## Requirements

Minecraft 1.21.1, NeoForge 21.1.235 or newer, Beyond Dimensions 0.8.0 or newer. JEI and EMI are optional. Beyond Dimensions already requires CompixelUI and a Kotlin runtime, so you have those either way.

---

# 超越合成

超越维度的附属模组。它在网络界面里加了一个**深度合成**页签，把网络没做完的那部分接上：你说出想要的东西，它负责算清楚怎么从你现有的存货走到那一步。

## 一句话版本

你网络里堆着一堆钻石块，一颗散钻石都没有；你想要一件钻石胸甲。点一下。钻石块被拆开，钻石被搬走，胸甲被合成，成品落回网络——连同你多出来的那一颗钻石。

链条走不通的时候，一个材料都不会扣，页面会告诉你具体缺什么、缺几个。

## 它的行为

- 只处理工作台配方，有序的和无序的都算。熔炼、高炉、锻造、各类机器配方不在链条内。
- 材料只从网络出，背包和合成格一概不碰。
- 无论你正看着哪个网络，它用的都是你的主网络。
- 所有进出都走网络自己的存储规则与容量上限，和你手动搬没有区别。网络装不下，就不合成。
- 配方返还容器（比如桶）时，是在合成格外模拟的，所以返还物只按数量回收，不会摆回某个特定格子。
- 压缩方块可以反着用：九个同种物品合成一个方块，或者一个方块拆回九个物品。可以整体关掉，也可以把个别物品排除。

## 界面

按名称、拼音首字母、物品 id 搜索，数量在 1 到 4096 之间自己填，页面会列出将要消耗的材料。在 JEI 或 EMI 里点"+"转移按钮，会直接打开这一页并选中该配方的产物，而不是往合成格里塞东西。

## 会破坏游戏吗

不会。没有任何东西是你本来做不出来的。它只是把同样的步骤按正确顺序做一遍，同样的输入，同样的产出。配方贵，结果就贵。

## 配置

三个选项，改动都不需要重启游戏：总开关、是否允许拆解压缩方块（可指定永不被拆解的物品）、配方链条最多展开多深。

## 环境要求

Minecraft 1.21.1、NeoForge 21.1.235 或更高版本、超越维度 0.8.0 或更高版本。JEI 与 EMI 可选。超越维度本身就要求 CompixelUI 与 Kotlin 运行库，所以你装不装都有。
