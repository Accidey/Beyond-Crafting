package com.xulai.beyondcrafting.client;

import com.wintercogs.beyonddimensions.client.ui.base.BdPageSelection;
import com.xulai.beyondcrafting.BeyondCrafting;
import com.xulai.beyondcrafting.client.page.DeepCraftPage;
import net.minecraft.world.item.ItemStack;

public final class DeepCraftClientHooks
{
    private static volatile BdPageSelection selection;

    private DeepCraftClientHooks()
    {
    }

    public static void track(BdPageSelection value)
    {
        selection = value;
    }

    public static void open(ItemStack stack)
    {
        if (stack.isEmpty())
            return;
        DeepCraftPage.INSTANCE.focus(stack.getItem());
        BdPageSelection current = selection;
        if (current != null)
            current.open(BeyondCrafting.DEEP_CRAFTING_PAGE.toString());
    }
}
