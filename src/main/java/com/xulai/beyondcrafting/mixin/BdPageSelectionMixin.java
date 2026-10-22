package com.xulai.beyondcrafting.mixin;

import com.wintercogs.beyonddimensions.client.ui.base.BdPageSelection;
import com.xulai.beyondcrafting.client.DeepCraftClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BdPageSelection.class, remap = false)
public abstract class BdPageSelectionMixin
{
    @Inject(method = "<init>", at = @At("RETURN"))
    private void beyondcrafting$track(CallbackInfo info)
    {
        DeepCraftClientHooks.track((BdPageSelection) (Object) this);
    }
}
