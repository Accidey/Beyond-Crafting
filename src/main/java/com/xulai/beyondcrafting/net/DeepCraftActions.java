package com.xulai.beyondcrafting.net;

import com.wintercogs.beyonddimensions.api.menu.BdMenuActions;
import com.wintercogs.beyonddimensions.api.storage.key.IStackKey;
import com.wintercogs.beyonddimensions.api.storage.key.impl.ItemStackKey;
import com.wintercogs.beyonddimensions.common.menu.DimensionsNetMenu;
import com.xulai.beyondcrafting.craft.DeepCraftService;
import dev.compixel.forge.sync.MenuAction;
import dev.compixel.forge.sync.MinecraftSyncCodecs;
import dev.compixel.sync.state.SyncCodec;

public final class DeepCraftActions
{
    public static final long MAX_AMOUNT = 4096L;

    private static final int MAX_BYTES = 8 * 1024 * 1024;

    public record Request(IStackKey<?> key, long amount)
    {
    }

    public static final MenuAction<DimensionsNetMenu, Request> CRAFT = MenuAction.of("beyondcrafting.craft", requestCodec(), MAX_BYTES,
            (menu, player, request) -> {
                if (!(request.key() instanceof ItemStackKey target))
                    return false;
                if (request.amount() <= 0L || request.amount() > MAX_AMOUNT)
                    return false;
                if (player.containerMenu != menu || !menu.stillValid(player))
                    return false;
                DeepCraftService.craft(player, target, request.amount());
                return true;
            });

    private DeepCraftActions()
    {
    }

    public static void register()
    {
        BdMenuActions.register(DimensionsNetMenu.class, CRAFT);
    }

    private static SyncCodec<Request> requestCodec()
    {
        SyncCodec<IStackKey<?>> nativeKey = MinecraftSyncCodecs.registry("beyondcrafting:stack_key/1", IStackKey.STREAM_CODEC);
        return SyncCodec.of("beyondcrafting:request/1:" + nativeKey.id(),
                (out, value) -> {
                    nativeKey.write(out, value.key());
                    out.writeLong(value.amount());
                },
                in -> new Request(nativeKey.read(in), in.readLong()));
    }
}
