package com.xulai.beyondcrafting.client;

import com.xulai.beyondcrafting.BeyondCrafting;
import com.xulai.beyondcrafting.craft.RecipeGraphCache;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;

@EventBusSubscriber(modid = BeyondCrafting.MODID, value = Dist.CLIENT)
public final class ClientRecipeListener
{
    private ClientRecipeListener()
    {
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event)
    {
        RecipeGraphCache.invalidate();
    }
}
