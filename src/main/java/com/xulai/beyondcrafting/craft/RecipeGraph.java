package com.xulai.beyondcrafting.craft;

import com.wintercogs.beyonddimensions.api.storage.key.IStackKey;
import com.wintercogs.beyonddimensions.api.storage.key.impl.ItemStackKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RecipeGraph
{
    public record Slot(List<ItemStack> choices)
    {
    }

    public record Entry(ItemStack result, List<Slot> slots, @Nullable CraftingRecipe recipe, boolean reverse)
    {
    }

    private final Map<Item, List<Entry>> byOutput;
    private final List<Item> products;

    private RecipeGraph(Map<Item, List<Entry>> byOutput)
    {
        this.byOutput = byOutput;
        this.products = List.copyOf(byOutput.keySet());
    }

    public List<Entry> candidates(Item item)
    {
        return byOutput.getOrDefault(item, List.of());
    }

    public List<Item> products()
    {
        return products;
    }

    public static RecipeGraph empty()
    {
        return new RecipeGraph(Map.of());
    }

    public @Nullable Item targetFor(List<IStackKey<?>> keys, List<Long> amounts)
    {
        List<IStackKey<?>> copies = new ArrayList<>();
        for (int i = 0; i < keys.size() && i < amounts.size(); i++)
        {
            Long amount = amounts.get(i);
            if (amount == null || amount <= 0L)
                continue;
            if (amount > 9L)
                return null;
            for (long copy = 0L; copy < amount; copy++)
                copies.add(keys.get(i));
        }
        if (copies.isEmpty() || copies.size() > 9)
            return null;

        for (Map.Entry<Item, List<Entry>> bucket : byOutput.entrySet())
            for (Entry entry : bucket.getValue())
                if (matches(entry, copies))
                    return bucket.getKey();
        return null;
    }

    private static boolean matches(Entry entry, List<IStackKey<?>> copies)
    {
        List<Slot> slots = entry.slots();
        if (slots.size() != copies.size())
            return false;
        return assign(slots, 0, copies, new boolean[copies.size()]);
    }

    private static boolean assign(List<Slot> slots, int index, List<IStackKey<?>> copies, boolean[] used)
    {
        if (index >= slots.size())
            return true;
        Slot slot = slots.get(index);
        for (int i = 0; i < copies.size(); i++)
        {
            if (used[i] || !accepts(slot, copies.get(i)))
                continue;
            used[i] = true;
            if (assign(slots, index + 1, copies, used))
                return true;
            used[i] = false;
        }
        return false;
    }

    private static boolean accepts(Slot slot, IStackKey<?> key)
    {
        for (ItemStack choice : slot.choices())
            if (new ItemStackKey(choice).equals(key))
                return true;
        return false;
    }

    public static RecipeGraph build(RecipeManager manager, HolderLookup.Provider registries, Set<ResourceLocation> reverseBlacklist, boolean allowReverse)
    {
        Map<Item, List<Entry>> byOutput = new LinkedHashMap<>();
        List<Entry> reverseCandidates = new ArrayList<>();

        for (RecipeHolder<CraftingRecipe> holder : manager.getAllRecipesFor(RecipeType.CRAFTING))
        {
            CraftingRecipe recipe = holder.value();
            ItemStack result = recipe.getResultItem(registries);
            if (result.isEmpty())
                continue;

            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            List<Slot> slots = new ArrayList<>(ingredients.size());
            boolean usable = true;
            for (Ingredient ingredient : ingredients)
            {
                if (ingredient == Ingredient.EMPTY)
                    continue;
                ItemStack[] items = ingredient.getItems();
                if (items.length == 0)
                {
                    usable = false;
                    break;
                }
                slots.add(new Slot(List.of(items)));
            }
            if (!usable || slots.isEmpty())
                continue;

            byOutput.computeIfAbsent(result.getItem(), key -> new ArrayList<>())
                    .add(new Entry(result, List.copyOf(slots), recipe, false));

            if (allowReverse)
            {
                Entry reverse = reverseOf(result, slots, reverseBlacklist);
                if (reverse != null)
                    reverseCandidates.add(reverse);
            }
        }

        for (Entry reverse : reverseCandidates)
        {
            Item produced = reverse.result().getItem();
            List<Entry> existing = byOutput.get(produced);
            if (existing != null && existing.stream().anyMatch(entry -> entry.result().getItem() == produced && !entry.reverse()))
                continue;
            byOutput.computeIfAbsent(produced, key -> new ArrayList<>()).add(reverse);
        }

        return new RecipeGraph(byOutput);
    }

    private static @Nullable Entry reverseOf(ItemStack result, List<Slot> slots, Set<ResourceLocation> blacklist)
    {
        if (result.getCount() != 1 || !(result.getItem() instanceof BlockItem) || slots.size() != 9)
            return null;

        Item shared = null;
        for (Slot slot : slots)
        {
            Item only = singleItem(slot.choices());
            if (only == null || shared != null && shared != only)
                return null;
            shared = only;
        }
        if (shared == null)
            return null;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(shared);
        if (blacklist.contains(id))
            return null;

        return new Entry(new ItemStack(shared, 9), List.of(new Slot(List.of(result.copy()))), null, true);
    }

    private static @Nullable Item singleItem(List<ItemStack> choices)
    {
        Item found = null;
        for (ItemStack stack : choices)
        {
            if (stack.isEmpty())
                continue;
            if (found != null && found != stack.getItem())
                return null;
            found = stack.getItem();
        }
        return found;
    }
}
