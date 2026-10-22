package com.xulai.beyondcrafting.craft;

import com.wintercogs.beyonddimensions.api.dimensionnet.DimensionsNet;
import com.wintercogs.beyonddimensions.api.dimensionnet.UnifiedStorage;
import com.wintercogs.beyonddimensions.api.storage.key.IStackKey;
import com.wintercogs.beyonddimensions.api.storage.key.KeyAmount;
import com.wintercogs.beyonddimensions.api.storage.key.impl.ItemStackKey;
import com.wintercogs.beyonddimensions.util.InventoryHelper;
import com.xulai.beyondcrafting.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DeepCraftService
{
    public enum Status
    {
        DONE,
        ALREADY,
        DISABLED,
        NO_NETWORK,
        INVALID,
        NO_SPACE,
        STALE
    }

    public record Result(Status status, List<CraftPlan.Missing> missing)
    {
        public static Result of(Status status)
        {
            return new Result(status, List.of());
        }

        public boolean success()
        {
            return status == Status.DONE || status == Status.ALREADY;
        }
    }

    private DeepCraftService()
    {
    }

    public static CraftPlan preview(Player player, ItemStackKey target, long amount)
    {
        DimensionsNet net = DimensionsNet.getPrimaryNetFromPlayer(player);
        if (net == null)
            return new CraftPlan(List.of(), List.of(), List.of(), List.of(new CraftPlan.Missing(target, amount)));

        UnifiedStorage storage = net.getUnifiedStorage();
        RecipeGraph graph = RecipeGraphCache.get(player.level().getRecipeManager(), player.level().registryAccess(), false);
        DeepCraftSolver solver = new DeepCraftSolver(graph,
                key -> storage.getStackByKey(key).amount(),
                Config.MAX_RECIPE_DEPTH.get(),
                Config.MAX_PLAN_NODES.get());
        return solver.solve(target, amount);
    }

    public static Result craft(ServerPlayer player, ItemStackKey target, long amount)
    {
        Result result = execute(player, target, amount);
        if (!result.success())
            player.displayClientMessage(message(result, target), false);
        return result;
    }

    public static Item targetFor(Player player, List<IStackKey<?>> keys, List<Long> amounts)
    {
        if (!Config.ENABLE_DEEP_CRAFTING.get())
            return null;
        RecipeGraph graph = RecipeGraphCache.get(player.level().getRecipeManager(), player.level().registryAccess(), false);
        return graph.targetFor(keys, amounts);
    }

    public static void supplyFor(Player player, List<IStackKey<?>> keys, List<Long> amounts)
    {
        for (int i = 0; i < keys.size() && i < amounts.size(); i++)
        {
            if (!(keys.get(i) instanceof ItemStackKey target))
                continue;
            Long amount = amounts.get(i);
            if (amount == null || amount <= 0L)
                continue;

            long held = countInInventory(player, target);
            long deficit = amount - held;
            if (deficit > 0L)
                execute(player, target, deficit);
        }
    }

    public static long countInInventory(Player player, ItemStackKey target)
    {
        long total = 0L;
        for (ItemStack stack : player.getInventory().items)
        {
            if (stack.isEmpty())
                continue;
            if (target.equals(new ItemStackKey(stack)))
                total += stack.getCount();
        }
        return total;
    }

    private static Result execute(Player player, ItemStackKey target, long amount)
    {
        if (!Config.ENABLE_DEEP_CRAFTING.get())
            return Result.of(Status.DISABLED);
        if (amount <= 0L)
            return Result.of(Status.INVALID);
        if (!player.isAlive() || player.isSpectator())
            return Result.of(Status.INVALID);

        DimensionsNet net = DimensionsNet.getPrimaryNetFromPlayer(player);
        if (net == null || !net.getPlayers().contains(player.getUUID()))
            return Result.of(Status.NO_NETWORK);

        UnifiedStorage storage = net.getUnifiedStorage();
        CraftPlan plan = preview(player, target, amount);
        if (!plan.complete())
            return new Result(Status.INVALID, plan.missing());
        if (plan.withdrawals().isEmpty() && plan.deposits().isEmpty())
            return Result.of(Status.ALREADY);

        for (KeyAmount deposit : plan.deposits())
        {
            if (storage.insert(deposit.key(), deposit.amount(), true).amount() > 0L)
                return Result.of(Status.NO_SPACE);
        }
        for (KeyAmount withdrawal : plan.withdrawals())
        {
            if (storage.extract(withdrawal.key(), withdrawal.amount(), true, false).amount() < withdrawal.amount())
                return Result.of(Status.STALE);
        }

        for (KeyAmount withdrawal : plan.withdrawals())
            storage.extract(withdrawal.key(), withdrawal.amount(), false, false);

        List<KeyAmount> overflow = new ArrayList<>();
        for (KeyAmount deposit : plan.deposits())
        {
            long leftover = storage.insert(deposit.key(), deposit.amount(), false).amount();
            if (leftover > 0L)
                overflow.add(new KeyAmount(deposit.key(), leftover));
        }
        for (KeyAmount leftover : overflow)
            handOver(player, leftover);

        return Result.of(Status.DONE);
    }

    private static void handOver(Player player, KeyAmount leftover)
    {
        if (!(leftover.key() instanceof ItemStackKey itemKey))
            return;
        ItemStack stack = itemKey.copyStackWithCount(leftover.amount());
        InventoryHelper.transferToPlayerInventory(player, stack);
        if (!stack.isEmpty())
            player.drop(stack, false);
    }

    private static Component message(Result result, IStackKey<?> target)
    {
        if (result.status() == Status.INVALID && result.missing().size() == 1 && result.missing().get(0).key().equals(target))
            return Component.translatable("message.beyondcrafting.no_recipe_target", describeKey(target));
        return describe(result);
    }

    public static Component describe(Result result)
    {
        return switch (result.status())
        {
            case DONE -> Component.translatable("message.beyondcrafting.done");
            case ALREADY -> Component.translatable("message.beyondcrafting.already");
            case DISABLED -> Component.translatable("message.beyondcrafting.disabled");
            case NO_NETWORK -> Component.translatable("message.beyondcrafting.no_network");
            case NO_SPACE -> Component.translatable("message.beyondcrafting.no_space");
            case STALE -> Component.translatable("message.beyondcrafting.stale");
            case INVALID -> missingComponent(result.missing());
        };
    }

    private static Component missingComponent(List<CraftPlan.Missing> missing)
    {
        if (missing.isEmpty())
            return Component.translatable("message.beyondcrafting.no_recipe");
        Component list = Component.empty();
        for (int i = 0; i < missing.size(); i++)
        {
            CraftPlan.Missing entry = missing.get(i);
            if (i > 0)
                list = list.copy().append(Component.literal("、"));
            list = list.copy().append(describeKey(entry.key())).append(Component.literal(" ×" + entry.amount()));
        }
        return Component.translatable("message.beyondcrafting.missing", list);
    }

    public static Component describeKey(IStackKey<?> key)
    {
        if (key instanceof ItemStackKey itemKey)
            return itemKey.copyStack().getHoverName();
        return Component.literal(String.valueOf(key.getSourceId()));
    }
}
