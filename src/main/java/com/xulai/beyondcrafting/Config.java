package com.xulai.beyondcrafting;

import com.xulai.beyondcrafting.craft.RecipeGraphCache;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static
    {
        ENABLE_DEEP_CRAFTING = BUILDER
                .comment(
                        "启用深度合成",
                        "Enable deep crafting",
                        "关闭后页签仍然显示，停止执行任何合成。",
                        "When disabled the page stays visible but performs no crafting.",
                        "Default: true / 默认：true"
                )
                .define("enableDeepCrafting", true);

        BUILDER.comment(" ");

        ALLOW_REVERSE_COMPRESSION = BUILDER
                .comment(
                        "允许拆解压缩方块",
                        "Allow unpacking compression blocks",
                        "允许把九个同种物品合成的方块拆回九个该物品，例如钻石块拆成九个钻石。",
                        "Lets a block made of nine identical items be taken apart into those nine items, such as a diamond block into nine diamonds.",
                        "Default: true / 默认：true"
                )
                .define("allowReverseCompression", true);

        BUILDER.comment(" ");

        MAX_RECIPE_DEPTH = BUILDER
                .comment(
                        "最大递归层数",
                        "Maximum recursion depth",
                        "一次深度合成最多向内展开多少层子配方，超过后视为缺少材料。",
                        "How many sub-recipe layers a single deep craft may expand; deeper requirements count as missing.",
                        "Default: 8 / 默认：8",
                        "Range: 1 ~ 32"
                )
                .defineInRange("maxRecipeDepth", 8, 1, 32);

        BUILDER.comment(" ");

        MAX_PLAN_NODES = BUILDER
                .comment(
                        "单次求解上限",
                        "Maximum nodes per plan",
                        "一次深度合成最多考察多少个材料节点，超过后放弃并提示材料不足。",
                        "How many material nodes a single deep craft may visit before giving up and reporting missing materials.",
                        "Default: 4096 / 默认：4096",
                        "Range: 128 ~ 65536"
                )
                .defineInRange("maxPlanNodes", 4096, 128, 65536);

        BUILDER.comment(" ");

        REVERSE_BLOCK_BLACKLIST = BUILDER
                .comment(
                        "禁止拆解的物品",
                        "Items that cannot be unpacked",
                        "列出的物品不会由压缩方块拆回，填写物品 id。",
                        "Listed items are never obtained by unpacking a compression block. Use item ids.",
                        "Default: [] / 默认：[]"
                )
                .defineListAllowEmpty("reverseBlockBlacklist", List.of(), () -> "", obj -> obj instanceof String);
    }

    public static final ModConfigSpec.BooleanValue ENABLE_DEEP_CRAFTING;
    public static final ModConfigSpec.BooleanValue ALLOW_REVERSE_COMPRESSION;
    public static final ModConfigSpec.IntValue MAX_RECIPE_DEPTH;
    public static final ModConfigSpec.IntValue MAX_PLAN_NODES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> REVERSE_BLOCK_BLACKLIST;

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static volatile Set<ResourceLocation> reverseBlacklist = Set.of();

    private Config()
    {
    }

    public static void reloadState(ModConfig config)
    {
        if (config.getSpec() != SPEC)
            return;
        resetReverseBlacklistCache();
    }

    public static Set<ResourceLocation> reverseBlacklist()
    {
        return reverseBlacklist;
    }

    public static void resetReverseBlacklistCache()
    {
        Set<ResourceLocation> next = new LinkedHashSet<>();
        for (String id : REVERSE_BLOCK_BLACKLIST.get())
        {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location != null)
                next.add(location);
        }
        reverseBlacklist = Set.copyOf(next);
        RecipeGraphCache.invalidate();
    }
}
