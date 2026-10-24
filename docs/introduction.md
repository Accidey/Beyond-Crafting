# Beyond Crafting

An addon for Beyond Dimensions. It provides a deep crafting page inside the dimension network interface. Given a target item, the page expands the full recipe chain down to materials already held by the network, deducts those materials in a single operation, and writes the finished item back to the network.

## 🧭 Operation

The page is part of the dimension network interface. Choose a target item from the search results, which can be matched by item name, by pinyin initials, or by item id, set a quantity between 1 and 4096, and confirm. The page lists the materials that will be consumed. If the chain cannot be completed, no material is deducted, and the missing materials are listed with their exact amounts.

## ⚙️ Recipe resolution

- Only crafting-table recipes are considered. Smelting, blasting, smithing and machine recipes are not part of the chain.
- Compression blocks are usable in both directions. A recipe that combines nine identical items into one block may be run in reverse, so a single diamond block supplies nine diamonds. This behaviour can be disabled, and individual items can be excluded from it.
- Expansion stops at materials held by the network. Materials in the player inventory and in the crafting grid are not used.
- Intermediate products are not written to the network. Only the target item and any surplus produced by a recipe are inserted.
- Containers returned by a recipe are accounted for by quantity. Because a recipe is evaluated outside the crafting grid, the position of a returned container within a grid layout is not reproduced.
- Chains that exceed the configured depth or node limit are treated as incomplete.

## 🔗 Network interaction

Every transfer passes through the network's own storage rules and capacity limits. If the target item or a surplus item cannot be inserted, the operation is aborted before any material is deducted.

The page always operates on the player's primary network.

## 🎯 Recipe viewer integration

Using the transfer button in JEI or EMI opens this page with the result of that recipe, instead of filling the crafting grid.

## 🛠 Configuration

The main switch, the handling of compression blocks together with an exclusion list, and the resolution limits are configurable. Changes are applied without restarting the game.

---

# 超越合成

超越维度的附属模组，在维度网络界面中提供深度合成功能。给定一个目标物品，页面会展开完整的配方链，直至网络中已有的材料，一次性扣除这些材料，并将成品写回网络。

## 🧭 使用方式

该页面位于维度网络界面内。在搜索结果中选择目标物品，搜索支持按名称、拼音首字母或物品 id 匹配，设定 1 至 4096 之间的数量并确认。页面会列出将被消耗的材料。若配方链无法完成，则不会扣除任何材料，并列出缺失材料的种类与数量。

## ⚙️ 配方展开规则

- 仅处理工作台配方，熔炼、高炉、锻造及各类机器配方不参与展开。
- 压缩方块可按两个方向使用。将九个同种物品合成一个方块的配方可反向执行，即一个钻石块可提供九个钻石。该行为可关闭，也可指定排除个别物品。
- 展开在网络上已有的材料处停止，玩家背包与合成格中的材料不被使用。
- 中间产物不会写入网络，只有目标物品与配方产生的多余产物会被插入。
- 配方返还的容器按数量处理。由于配方在合成格之外结算，返还容器在格位中的摆放位置不会被复现。
- 超出设定深度或节点上限的配方链视为无法完成。

## 🔗 与网络的交互

所有转移均经过网络自身的存储规则与容量限制。若目标物品或多余产物无法插入网络，操作会在扣除任何材料之前中止。

该页面始终作用于玩家的主网络。

## 🎯 配方查看器联动

在 JEI 或 EMI 中点击转移按钮时，将打开此页面并选中该配方的产物，而不是填充合成格。

## 🛠 配置

总开关、压缩方块的处理方式（含排除列表）以及展开上限均可配置，修改后无需重启游戏。
