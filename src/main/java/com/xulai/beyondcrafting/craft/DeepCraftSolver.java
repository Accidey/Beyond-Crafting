package com.xulai.beyondcrafting.craft;

import com.wintercogs.beyonddimensions.api.storage.key.IStackKey;
import com.wintercogs.beyonddimensions.api.storage.key.KeyAmount;
import com.wintercogs.beyonddimensions.api.storage.key.impl.ItemStackKey;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToLongFunction;

public final class DeepCraftSolver
{
    private static final int MAX_MISSING = 8;

    private final RecipeGraph graph;
    private final ToLongFunction<IStackKey<?>> stock;
    private final int maxDepth;
    private final long maxNodes;

    private final Map<IStackKey<?>, Long> taken = new LinkedHashMap<>();
    private final Map<IStackKey<?>, Long> deposits = new LinkedHashMap<>();
    private final List<CraftPlan.Step> steps = new ArrayList<>();
    private final List<CraftPlan.Missing> missing = new ArrayList<>();
    private long nodes;
    private int missingDepth = -1;

    public DeepCraftSolver(RecipeGraph graph, ToLongFunction<IStackKey<?>> stock, int maxDepth, long maxNodes)
    {
        this.graph = graph;
        this.stock = stock;
        this.maxDepth = maxDepth;
        this.maxNodes = maxNodes;
    }

    public CraftPlan solve(ItemStackKey target, long amount)
    {
        if (amount <= 0L)
            return CraftPlan.EMPTY;

        long have = Math.max(0L, stock.applyAsLong(target));
        long deficit = amount - have;
        if (deficit <= 0L)
            return CraftPlan.EMPTY;

        if (!craft(target, deficit, 0, Set.of()))
        {
            List<CraftPlan.Missing> failed = missing.isEmpty()
                    ? List.of(new CraftPlan.Missing(target, deficit))
                    : List.copyOf(missing);
            return new CraftPlan(List.of(), List.of(), List.of(), failed);
        }

        add(deposits, target, deficit);
        return new CraftPlan(toAmounts(taken), toAmounts(deposits), List.copyOf(steps), List.of());
    }

    private boolean supply(IStackKey<?> key, long amount, int depth, Set<Item> path)
    {
        if (amount <= 0L)
            return true;
        if (++nodes > maxNodes)
            return false;

        long produced = deposits.getOrDefault(key, 0L);
        long fromProduced = Math.min(produced, amount);
        if (fromProduced > 0L)
        {
            long left = produced - fromProduced;
            if (left <= 0L)
                deposits.remove(key);
            else
                deposits.put(key, left);
            amount -= fromProduced;
        }
        if (amount <= 0L)
            return true;

        long have = Math.max(0L, stock.applyAsLong(key) - taken.getOrDefault(key, 0L));
        long fromStock = Math.min(have, amount);
        if (fromStock > 0L)
            add(taken, key, fromStock);

        long deficit = amount - fromStock;
        if (deficit <= 0L)
            return true;

        Mark mark = mark();
        if (craft(key, deficit, depth, path))
            return true;
        rollback(mark);
        noteMissing(key, deficit, depth);
        return false;
    }

    private boolean craft(IStackKey<?> key, long need, int depth, Set<Item> path)
    {
        if (need <= 0L)
            return true;
        if (depth >= maxDepth)
            return false;

        Item item = itemOf(key);
        if (item == null)
            return false;
        if (path.contains(item))
            return false;

        List<RecipeGraph.Entry> candidates = graph.candidates(item);
        if (candidates.isEmpty())
            return false;

        Set<Item> next = new HashSet<>(path);
        next.add(item);

        int missingMark = missing.size();
        int missingDepthMark = missingDepth;
        boolean recorded = false;

        for (RecipeGraph.Entry entry : candidates)
        {
            long out = entry.result().getCount();
            if (out <= 0L)
                continue;
            long batches = ceilDiv(need, out);
            if (batches <= 0L)
                continue;

            Mark mark = mark();
            int candidateMissingMark = missing.size();
            int candidateDepthMark = missingDepth;
            List<ItemStack> chosen = new ArrayList<>(entry.slots().size());
            boolean ok = true;
            for (RecipeGraph.Slot slot : entry.slots())
            {
                ItemStack pick = choose(slot);
                if (pick == null)
                {
                    ok = false;
                    continue;
                }
                chosen.add(pick);
                if (!supply(new ItemStackKey(pick), batches, depth + 1, next))
                    ok = false;
            }
            if (!ok)
            {
                rollback(mark);
                if (recorded)
                {
                    truncate(missing, candidateMissingMark);
                    missingDepth = candidateDepthMark;
                }
                else if (missing.size() > missingMark)
                {
                    recorded = true;
                }
                continue;
            }

            long produced;
            try
            {
                produced = Math.multiplyExact(batches, out);
            }
            catch (ArithmeticException overflow)
            {
                rollback(mark);
                continue;
            }

            truncate(missing, missingMark);
            missingDepth = missingDepthMark;

            if (produced > need)
                add(deposits, key, produced - need);
            steps.add(new CraftPlan.Step(depth, key, batches, entry.reverse()));
            depositRemainders(entry, chosen, batches);
            return true;
        }

        return false;
    }

    private @Nullable ItemStack choose(RecipeGraph.Slot slot)
    {
        ItemStack best = null;
        long bestStock = -1L;
        for (ItemStack candidate : slot.choices())
        {
            if (candidate.isEmpty())
                continue;
            IStackKey<?> key = new ItemStackKey(candidate);
            long available = Math.max(0L, stock.applyAsLong(key) - taken.getOrDefault(key, 0L));
            if (available > bestStock)
            {
                bestStock = available;
                best = candidate;
            }
        }
        return best;
    }

    private void depositRemainders(RecipeGraph.Entry entry, List<ItemStack> chosen, long batches)
    {
        if (entry.recipe() == null)
            return;

        List<ItemStack> grid = new ArrayList<>(9);
        for (ItemStack stack : chosen)
            grid.add(stack.copyWithCount(1));
        while (grid.size() < 9)
            grid.add(ItemStack.EMPTY);

        NonNullList<ItemStack> remainders;
        try
        {
            remainders = entry.recipe().getRemainingItems(CraftingInput.of(3, 3, grid));
        }
        catch (RuntimeException failure)
        {
            return;
        }

        for (ItemStack remainder : remainders)
        {
            if (remainder.isEmpty())
                continue;
            long count;
            try
            {
                count = Math.multiplyExact((long) remainder.getCount(), batches);
            }
            catch (ArithmeticException overflow)
            {
                continue;
            }
            if (count > 0L)
                add(deposits, new ItemStackKey(remainder), count);
        }
    }

    private void noteMissing(IStackKey<?> key, long amount, int depth)
    {
        if (missingDepth < 0 || depth < missingDepth)
        {
            missing.clear();
            missingDepth = depth;
        }
        else if (depth > missingDepth)
        {
            return;
        }

        for (int i = 0; i < missing.size(); i++)
        {
            if (missing.get(i).key().equals(key))
            {
                missing.set(i, new CraftPlan.Missing(key, saturatedAdd(missing.get(i).amount(), amount)));
                return;
            }
        }
        if (missing.size() >= MAX_MISSING)
            return;
        missing.add(new CraftPlan.Missing(key, amount));
    }

    private static @Nullable Item itemOf(IStackKey<?> key)
    {
        if (key instanceof ItemStackKey itemKey && itemKey.getSource() instanceof Item item)
            return item;
        return null;
    }

    private static long ceilDiv(long value, long unit)
    {
        if (unit <= 0L)
            return 0L;
        return value / unit + (value % unit == 0L ? 0L : 1L);
    }

    private static void add(Map<IStackKey<?>, Long> target, IStackKey<?> key, long amount)
    {
        if (amount <= 0L)
            return;
        target.merge(key, amount, DeepCraftSolver::saturatedAdd);
    }

    private static long saturatedAdd(long first, long second)
    {
        return second > Long.MAX_VALUE - first ? Long.MAX_VALUE : first + second;
    }

    private static List<KeyAmount> toAmounts(Map<IStackKey<?>, Long> source)
    {
        List<KeyAmount> out = new ArrayList<>(source.size());
        source.forEach((key, amount) -> out.add(new KeyAmount(key, amount)));
        return List.copyOf(out);
    }

    private Mark mark()
    {
        return new Mark(new LinkedHashMap<>(taken), new LinkedHashMap<>(deposits), steps.size());
    }

    private void rollback(Mark mark)
    {
        taken.clear();
        taken.putAll(mark.taken);
        deposits.clear();
        deposits.putAll(mark.deposits);
        truncate(steps, mark.steps);
    }

    private static <T> void truncate(List<T> list, int size)
    {
        while (list.size() > size)
            list.remove(list.size() - 1);
    }

    private record Mark(Map<IStackKey<?>, Long> taken, Map<IStackKey<?>, Long> deposits, int steps)
    {
    }
}
