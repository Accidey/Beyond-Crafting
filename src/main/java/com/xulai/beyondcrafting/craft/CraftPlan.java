package com.xulai.beyondcrafting.craft;

import com.wintercogs.beyonddimensions.api.storage.key.IStackKey;
import com.wintercogs.beyonddimensions.api.storage.key.KeyAmount;

import java.util.List;

public record CraftPlan(List<KeyAmount> withdrawals, List<KeyAmount> deposits, List<Step> steps, List<Missing> missing)
{
    public static final CraftPlan EMPTY = new CraftPlan(List.of(), List.of(), List.of(), List.of());

    public record Step(int depth, IStackKey<?> key, long batches, boolean reverse)
    {
    }

    public record Missing(IStackKey<?> key, long amount)
    {
    }

    public boolean complete()
    {
        return missing.isEmpty();
    }
}
