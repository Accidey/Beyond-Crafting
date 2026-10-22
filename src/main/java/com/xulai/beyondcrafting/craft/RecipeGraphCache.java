package com.xulai.beyondcrafting.craft;

import com.xulai.beyondcrafting.BeyondCrafting;
import com.xulai.beyondcrafting.Config;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.util.Set;

public final class RecipeGraphCache
{
    private static RecipeManager clientManager;
    private static Set<ResourceLocation> clientBlacklist = Set.of();
    private static boolean clientReverse;
    private static RecipeGraph clientGraph;

    private static RecipeManager serverManager;
    private static Set<ResourceLocation> serverBlacklist = Set.of();
    private static boolean serverReverse;
    private static RecipeGraph serverGraph;

    private RecipeGraphCache()
    {
    }

    public static synchronized RecipeGraph get(RecipeManager manager, HolderLookup.Provider registries, boolean clientSide)
    {
        Set<ResourceLocation> blacklist = Config.reverseBlacklist();
        boolean reverse = Config.ALLOW_REVERSE_COMPRESSION.get();

        if (clientSide)
        {
            if (clientGraph != null && clientManager == manager && clientReverse == reverse && clientBlacklist.equals(blacklist))
                return clientGraph;

            clientGraph = build(manager, registries, blacklist, reverse);
            clientManager = manager;
            clientBlacklist = blacklist;
            clientReverse = reverse;
            return clientGraph;
        }

        if (serverGraph != null && serverManager == manager && serverReverse == reverse && serverBlacklist.equals(blacklist))
            return serverGraph;

        serverGraph = build(manager, registries, blacklist, reverse);
        serverManager = manager;
        serverBlacklist = blacklist;
        serverReverse = reverse;
        return serverGraph;
    }

    private static RecipeGraph build(RecipeManager manager, HolderLookup.Provider registries, Set<ResourceLocation> blacklist, boolean reverse)
    {
        try
        {
            RecipeGraph graph = RecipeGraph.build(manager, registries, blacklist, reverse);
            BeyondCrafting.LOGGER.info("Indexed crafting recipes into {} products", graph.products().size());
            return graph;
        }
        catch (RuntimeException failure)
        {
            BeyondCrafting.LOGGER.error("Failed to index crafting recipes, deep crafting is unavailable until the next reload", failure);
            return RecipeGraph.empty();
        }
    }

    public static synchronized void invalidate()
    {
        clientGraph = null;
        clientManager = null;
        serverGraph = null;
        serverManager = null;
    }

    public static void onDatapackSync(OnDatapackSyncEvent event)
    {
        invalidate();
    }
}
