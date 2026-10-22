package com.xulai.beyondcrafting.client.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wintercogs.beyonddimensions.api.storage.key.impl.ItemStackKey
import com.wintercogs.beyonddimensions.api.ui.page.BdPage
import com.wintercogs.beyonddimensions.api.ui.page.BdPageContext
import com.wintercogs.beyonddimensions.api.ui.page.BdPageScope
import com.wintercogs.beyonddimensions.client.ui.base.tr
import com.wintercogs.beyonddimensions.client.ui.kit.BdChip
import com.wintercogs.beyonddimensions.client.ui.kit.BdIcons
import com.wintercogs.beyonddimensions.client.ui.kit.BdNumberEditor
import com.wintercogs.beyonddimensions.client.ui.kit.BdPixelIcon
import com.wintercogs.beyonddimensions.client.ui.kit.BdScrollColumn
import com.wintercogs.beyonddimensions.client.ui.kit.BdSearchField
import com.wintercogs.beyonddimensions.client.ui.kit.BdSectionLabel
import com.wintercogs.beyonddimensions.common.menu.DimensionsNetMenu
import com.wintercogs.beyonddimensions.util.TinyPinyinUtils
import com.xulai.beyondcrafting.Config
import com.xulai.beyondcrafting.craft.CraftPlan
import com.xulai.beyondcrafting.craft.DeepCraftService
import com.xulai.beyondcrafting.craft.DeepCraftSolver
import com.xulai.beyondcrafting.craft.RecipeGraph
import com.xulai.beyondcrafting.craft.RecipeGraphCache
import com.xulai.beyondcrafting.net.DeepCraftActions
import dev.compixel.ui.ore.display.OreText
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.WeakHashMap
import java.util.function.ToLongFunction

data class DeepCraftState(
    val query: String,
    val targets: List<String>,
    val selected: Int,
    val amount: Long,
    val lines: List<String>,
    val canCraft: Boolean,
    val version: Int = 0,
)

sealed interface DeepCraftAction {
    data class Query(val text: String) : DeepCraftAction
    data class Select(val index: Int) : DeepCraftAction
    data class Amount(val amount: Long) : DeepCraftAction
    data object Craft : DeepCraftAction
}

object DeepCraftPage : BdPage<DimensionsNetMenu, DeepCraftState, DeepCraftAction> {
    private val limit = 60

    private val states = WeakHashMap<DimensionsNetMenu, ClientState>()

    private var pendingTarget: Item? = null

    fun focus(item: Item) {
        pendingTarget = item
    }

    private fun takePendingTarget(): Item? {
        val value = pendingTarget
        pendingTarget = null
        return value
    }

    override val icon: BdPixelIcon = BdIcons.Crafting

    override fun title(context: BdPageContext<DimensionsNetMenu>): Component =
        Component.translatable("ui.beyondcrafting.page")

    override fun snapshot(context: BdPageContext<DimensionsNetMenu>): DeepCraftState =
        try {
            stateOf(context.menu).snapshot(context.menu)
        } catch (failure: Throwable) {
            DeepCraftState("", emptyList(), -1, 1L, listOf(failure.toString()), false, 0)
        }

    override fun handle(context: BdPageContext<DimensionsNetMenu>, action: DeepCraftAction) {
        val state = stateOf(context.menu)
        when (action) {
            is DeepCraftAction.Query -> {
                state.query = action.text
                state.selected = -1
                state.dirty = true
            }

            is DeepCraftAction.Select -> {
                state.selected = action.index
                state.dirty = true
            }

            is DeepCraftAction.Amount -> {
                state.amount = action.amount
                state.dirty = true
            }

            DeepCraftAction.Craft -> {
                val target = state.target() ?: return
                context.request(DeepCraftActions.CRAFT, DeepCraftActions.Request(ItemStackKey(ItemStack(target)), state.amount))
            }
        }
    }

    @Composable
    override fun ColumnScope.Content(state: DeepCraftState, scope: BdPageScope<DeepCraftAction>) {
        val text = remember(state.version) { mutableStateOf(state.query) }
        BdSearchField(text.value, {
            text.value = it
            scope.send(DeepCraftAction.Query(it))
        }, tr("ui.beyondcrafting.search"))
        BdScrollColumn(Modifier.fillMaxWidth().height(120.dp)) {
            state.targets.forEachIndexed { index, name ->
                BdChip(
                    onClick = { scope.send(DeepCraftAction.Select(index)) },
                    active = index == state.selected,
                ) { OreText(name, maxLines = 1) }
                Spacer(Modifier.height(2.dp))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            BdNumberEditor(state.amount, { scope.send(DeepCraftAction.Amount(it)) }, 1L..DeepCraftActions.MAX_AMOUNT)
            BdChip(
                onClick = { scope.send(DeepCraftAction.Craft) },
                enabled = state.canCraft,
                active = state.canCraft,
            ) { OreText(tr("ui.beyondcrafting.craft"), maxLines = 1) }
        }
        BdSectionLabel(tr("ui.beyondcrafting.materials"))
        BdScrollColumn(Modifier.fillMaxWidth().height(46.dp)) {
            state.lines.forEach { line ->
                OreText(line, maxLines = 1)
                Spacer(Modifier.height(2.dp))
            }
        }
    }

    private fun stateOf(menu: DimensionsNetMenu): ClientState = states.getOrPut(menu) { ClientState() }

    private fun matches(item: Item, query: String): Boolean {
        if (query.isEmpty()) return true
        val name = ItemStack(item).hoverName.string.lowercase()
        val id = BuiltInRegistries.ITEM.getKey(item).toString()
        if (name.contains(query) || id.contains(query)) return true
        return TinyPinyinUtils.getFirstPinYin(name).lowercase().contains(query)
    }

    private class ClientState {
        var query: String = ""
        var selected: Int = -1
        var amount: Long = 1L
        var dirty: Boolean = true
        private var version: Int = 0

        private var lastQuery: String = "\u0000"
        private var items: List<Item> = emptyList()
        private var plan: CraftPlan = CraftPlan.EMPTY
        private var planItem: Item? = null
        private var planAmount: Long = -1L
        private var ticks: Int = 0

        fun target(): Item? = items.getOrNull(selected)

        fun snapshot(menu: DimensionsNetMenu): DeepCraftState {
            val level = Minecraft.getInstance().level
            if (level == null) return DeepCraftState(query, emptyList(), selected, amount, emptyList(), false, version)

            val focus = DeepCraftPage.takePendingTarget()
            if (focus != null) {
                query = ItemStack(focus).hoverName.string
                lastQuery = "\u0000"
                selected = -1
                version++
            }

            val normalized = query.trim().lowercase()
            val graph = RecipeGraphCache.get(level.recipeManager, level.registryAccess(), true)
            if (normalized != lastQuery) {
                items = graph.products().filter { matches(it, normalized) }.take(limit)
                lastQuery = normalized
                if (focus != null) selected = items.indexOf(focus)
                else if (selected >= items.size) selected = -1
            }

            val current = items.getOrNull(selected)
            ticks++
            if (dirty || ticks % 10 == 0 || current !== planItem || amount != planAmount) {
                plan = solve(graph, menu, current)
                planItem = current
                planAmount = amount
                dirty = false
            }

            val lines = ArrayList<String>()
            if (current != null) {
                for (entry in plan.withdrawals)
                    lines.add(DeepCraftService.describeKey(entry.key).string + " x" + entry.amount)
                val self = ItemStackKey(ItemStack(current))
                if (plan.missing.size == 1 && plan.missing[0].key == self)
                    lines.add(tr("ui.beyondcrafting.line_no_recipe", DeepCraftService.describeKey(plan.missing[0].key).string))
                else
                    for (entry in plan.missing)
                        lines.add(tr("ui.beyondcrafting.line_missing", DeepCraftService.describeKey(entry.key).string, entry.amount))
                if (lines.isEmpty()) lines.add(tr("ui.beyondcrafting.line_ready"))
            }

            return DeepCraftState(
                query = query,
                targets = items.map { ItemStack(it).hoverName.string },
                selected = selected,
                amount = amount,
                lines = lines,
                canCraft = current != null && plan.complete() && plan.withdrawals.isNotEmpty(),
                version = version,
            )
        }

        private fun solve(graph: RecipeGraph, menu: DimensionsNetMenu, item: Item?): CraftPlan {
            if (item == null || !Config.ENABLE_DEEP_CRAFTING.get()) return CraftPlan.EMPTY
            val stock = ToLongFunction<com.wintercogs.beyonddimensions.api.storage.key.IStackKey<*>> {
                menu.storage.getStackByKey(it).amount
            }
            val solver = DeepCraftSolver(graph, stock, Config.MAX_RECIPE_DEPTH.get(), Config.MAX_PLAN_NODES.get().toLong())
            return solver.solve(ItemStackKey(ItemStack(item)), amount)
        }
    }
}
