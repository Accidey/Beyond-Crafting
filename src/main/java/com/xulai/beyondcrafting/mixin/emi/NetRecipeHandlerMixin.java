package com.xulai.beyondcrafting.mixin.emi;

import com.wintercogs.beyonddimensions.integration.module.emi.recipe.NetRecipeHandler;
import com.xulai.beyondcrafting.Config;
import com.xulai.beyondcrafting.client.DeepCraftClientHooks;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = NetRecipeHandler.class, remap = false)
public abstract class NetRecipeHandlerMixin
{
    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private void beyondcrafting$openDeepCrafting(EmiRecipe recipe, EmiCraftContext<?> context, CallbackInfoReturnable<Boolean> info)
    {
        if (!Config.ENABLE_DEEP_CRAFTING.get())
            return;

        List<EmiStack> outputs = recipe.getOutputs();
        if (outputs.isEmpty())
            return;

        ItemStack stack = outputs.get(0).getItemStack();
        if (stack.isEmpty())
            return;

        DeepCraftClientHooks.open(stack.copyWithCount(1));
        info.setReturnValue(true);
    }
}
