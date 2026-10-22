package com.xulai.beyondcrafting.mixin.jei;

import com.wintercogs.beyonddimensions.common.menu.DimensionsCraftMenu;
import com.wintercogs.beyonddimensions.integration.module.jei.transfer.CraftMenuRecipeTransferHandler;
import com.xulai.beyondcrafting.Config;
import com.xulai.beyondcrafting.client.DeepCraftClientHooks;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftMenuRecipeTransferHandler.class, remap = false)
public abstract class CraftMenuRecipeTransferHandlerMixin
{
    @Inject(method = "transferRecipe", at = @At("HEAD"), cancellable = true)
    private void beyondcrafting$openDeepCrafting(DimensionsCraftMenu container, RecipeHolder<CraftingRecipe> recipe, IRecipeSlotsView slots,
                                                 Player player, boolean maxTransfer, boolean doTransfer,
                                                 CallbackInfoReturnable<IRecipeTransferError> info)
    {
        if (!doTransfer)
            return;
        if (!Config.ENABLE_DEEP_CRAFTING.get())
            return;

        ItemStack result = recipe.value().getResultItem(player.level().registryAccess());
        if (result.isEmpty())
            return;

        DeepCraftClientHooks.open(result.copyWithCount(1));
        info.setReturnValue(null);
    }
}
