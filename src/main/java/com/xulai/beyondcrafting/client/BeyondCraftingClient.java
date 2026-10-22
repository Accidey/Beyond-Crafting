package com.xulai.beyondcrafting.client;

import com.wintercogs.beyonddimensions.api.ui.page.BdPages;
import com.wintercogs.beyonddimensions.common.menu.DimensionsNetMenu;
import com.xulai.beyondcrafting.BeyondCrafting;
import com.xulai.beyondcrafting.client.page.DeepCraftPage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = BeyondCrafting.MODID, value = Dist.CLIENT)
public final class BeyondCraftingClient
{
    private BeyondCraftingClient()
    {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> BdPages.register(
                BeyondCrafting.DEEP_CRAFTING_PAGE,
                DimensionsNetMenu.class,
                DeepCraftPage.INSTANCE));
    }
}
