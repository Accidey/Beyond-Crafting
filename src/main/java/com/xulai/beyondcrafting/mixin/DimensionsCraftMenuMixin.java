package com.xulai.beyondcrafting.mixin;

import com.wintercogs.beyonddimensions.api.storage.key.IStackKey;
import com.wintercogs.beyonddimensions.common.menu.DimensionsCraftMenu;
import com.xulai.beyondcrafting.craft.DeepCraftService;
import com.xulai.beyondcrafting.net.OpenDeepCraftPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = DimensionsCraftMenu.class, remap = false)
public abstract class DimensionsCraftMenuMixin
{
    @Inject(method = "transferRecipe(Ljava/util/List;Ljava/util/List;Z)Z", at = @At("HEAD"), cancellable = true)
    private void beyondcrafting$redirect(List<IStackKey<?>> keys, List<Long> amounts, boolean compress, CallbackInfoReturnable<Boolean> info)
    {
        DimensionsCraftMenu menu = (DimensionsCraftMenu) (Object) this;
        if (menu.player.level().isClientSide())
            return;
        if (keys == null || amounts == null || keys.isEmpty() || keys.size() != amounts.size())
            return;
        if (!(menu.player instanceof ServerPlayer player))
            return;
        if (player.containerMenu != menu || !player.isAlive() || player.isSpectator())
            return;

        Item target = DeepCraftService.targetFor(player, keys, amounts);
        if (target != null)
        {
            PacketDistributor.sendToPlayer(player, new OpenDeepCraftPayload(new ItemStack(target)));
            info.setReturnValue(true);
            return;
        }

        DeepCraftService.supplyFor(menu.player, keys, amounts);
    }
}
