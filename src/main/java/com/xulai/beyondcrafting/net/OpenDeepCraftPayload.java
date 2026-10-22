package com.xulai.beyondcrafting.net;

import com.xulai.beyondcrafting.BeyondCrafting;
import com.xulai.beyondcrafting.client.DeepCraftClientHooks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record OpenDeepCraftPayload(ItemStack stack) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<OpenDeepCraftPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BeyondCrafting.MODID, "open_deep_crafting"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDeepCraftPayload> CODEC =
            StreamCodec.composite(ItemStack.STREAM_CODEC, OpenDeepCraftPayload::stack, OpenDeepCraftPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void register(IEventBus modBus)
    {
        modBus.addListener(OpenDeepCraftPayload::onRegister);
    }

    private static void onRegister(RegisterPayloadHandlersEvent event)
    {
        event.registrar("1").playToClient(TYPE, CODEC, (payload, context) -> DeepCraftClientHooks.open(payload.stack()));
    }
}
