package com.xulai.beyondcrafting;

import com.mojang.logging.LogUtils;
import com.xulai.beyondcrafting.craft.RecipeGraphCache;
import com.xulai.beyondcrafting.net.DeepCraftActions;
import com.xulai.beyondcrafting.net.OpenDeepCraftPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(BeyondCrafting.MODID)
public class BeyondCrafting
{
    public static final String MODID = "beyondcrafting";
    public static final ResourceLocation DEEP_CRAFTING_PAGE = ResourceLocation.fromNamespaceAndPath(MODID, "deep_crafting");
    public static final Logger LOGGER = LogUtils.getLogger();

    public BeyondCrafting(IEventBus modBus, ModContainer container)
    {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        container.getEventBus().addListener((ModConfigEvent.Loading event) -> Config.reloadState(event.getConfig()));
        container.getEventBus().addListener((ModConfigEvent.Reloading event) -> Config.reloadState(event.getConfig()));

        DeepCraftActions.register();
        OpenDeepCraftPayload.register(modBus);
        NeoForge.EVENT_BUS.addListener(RecipeGraphCache::onDatapackSync);
    }
}
