package xyz.bluspring.kilt.forgeinjects.data.recipes;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

@Mixin(RecipeProvider.class)
public class RecipeProviderInject {
    private static final Map<Item, TagKey<Item>> replacements = new HashMap<>();
    private static final Set<ResourceLocation> excludes = new HashSet<>();

    private static void exclude(ItemLike item)
    {
        excludes.add(ForgeRegistries.ITEMS.getKey(item.asItem()));
    }

    private static void replace(ItemLike item, TagKey<Item> tag)
    {
        replacements.put(item.asItem(), tag);
    }

    @Nullable
    private static FinishedRecipe enhance(FinishedRecipe vanilla)
    {
        if (vanilla instanceof ShapelessRecipeBuilder.Result shapeless)
            return enhance(shapeless);
        if (vanilla instanceof ShapedRecipeBuilder.Result shaped)
            return enhance(shaped);
        return null;
    }

    @Nullable
    private static FinishedRecipe enhance(ShapelessRecipeBuilder.Result vanilla)
    {
        List<Ingredient> ingredients = getField(ShapelessRecipeBuilder.Result.class, vanilla, 4);
        boolean modified = false;
        for (int x = 0; x < ingredients.size(); x++)
        {
            Ingredient ing = enhance(vanilla.getId(), ingredients.get(x));
            if (ing != null)
            {
                ingredients.set(x, ing);
                modified = true;
            }
        }
        return modified ? vanilla : null;
    }

    @Nullable
    private static FinishedRecipe enhance(ShapedRecipeBuilder.Result vanilla)
    {
        Map<Character, Ingredient> ingredients = getField(ShapedRecipeBuilder.Result.class, vanilla, 5);
        boolean modified = false;
        for (Character x : ingredients.keySet())
        {
            Ingredient ing = enhance(vanilla.getId(), ingredients.get(x));
            if (ing != null)
            {
                ingredients.put(x, ing);
                modified = true;
            }
        }
        return modified ? vanilla : null;
    }

    @Nullable
    private static Ingredient enhance(ResourceLocation name, Ingredient vanilla)
    {
        if (excludes.contains(name))
            return null;

        boolean modified = false;
        List<Ingredient.Value> items = new ArrayList<>();
        Ingredient.Value[] vanillaItems = getField(Ingredient.class, vanilla, 2); //This will probably crash between versions, if null fix index
        for (Ingredient.Value entry : vanillaItems)
        {
            if (entry instanceof Ingredient.ItemValue)
            {
                ItemStack stack = entry.getItems().stream().findFirst().orElse(ItemStack.EMPTY);
                TagKey<Item> replacement = replacements.get(stack.getItem());
                if (replacement != null)
                {
                    items.add(new Ingredient.TagValue(replacement));
                    modified = true;
                } else
                    items.add(entry);
            } else
                items.add(entry);
        }
        return modified ? Ingredient.fromValues(items.stream()) : null;
    }

    @SuppressWarnings("unchecked")
    private static <T, R> R getField(Class<T> clz, T inst, int index)
    {
        Field fld = clz.getDeclaredFields()[index];
        fld.setAccessible(true);
        try
        {
            return (R) fld.get(inst);
        } catch (IllegalArgumentException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @ModifyVariable(at = @At("HEAD"), method = "buildCraftingRecipes", argsOnly = true)
    private static Consumer<FinishedRecipe> kilt$replaceRecipes(Consumer<FinishedRecipe> consumer) {
        replace(Items.STICK, Tags.Items.RODS_WOODEN);
        replace(Items.GOLD_INGOT, Tags.Items.INGOTS_GOLD);
        replace(Items.IRON_INGOT, Tags.Items.INGOTS_IRON);
        replace(Items.NETHERITE_INGOT, Tags.Items.INGOTS_NETHERITE);
        replace(Items.COPPER_INGOT, Tags.Items.INGOTS_COPPER);
        replace(Items.AMETHYST_SHARD, Tags.Items.GEMS_AMETHYST);
        replace(Items.DIAMOND, Tags.Items.GEMS_DIAMOND);
        replace(Items.EMERALD, Tags.Items.GEMS_EMERALD);
        replace(Items.CHEST, Tags.Items.CHESTS_WOODEN);
        replace(Blocks.COBBLESTONE, Tags.Items.COBBLESTONE_NORMAL);
        replace(Blocks.COBBLED_DEEPSLATE, Tags.Items.COBBLESTONE_DEEPSLATE);

        exclude(Blocks.GOLD_BLOCK);
        exclude(Items.GOLD_NUGGET);
        exclude(Blocks.IRON_BLOCK);
        exclude(Items.IRON_NUGGET);
        exclude(Blocks.DIAMOND_BLOCK);
        exclude(Blocks.EMERALD_BLOCK);
        exclude(Blocks.NETHERITE_BLOCK);
        exclude(Blocks.COPPER_BLOCK);
        exclude(Blocks.AMETHYST_BLOCK);

        exclude(Blocks.COBBLESTONE_STAIRS);
        exclude(Blocks.COBBLESTONE_SLAB);
        exclude(Blocks.COBBLESTONE_WALL);
        exclude(Blocks.COBBLED_DEEPSLATE_STAIRS);
        exclude(Blocks.COBBLED_DEEPSLATE_SLAB);
        exclude(Blocks.COBBLED_DEEPSLATE_WALL);

        return (vanilla) -> {
            FinishedRecipe modified = enhance(vanilla);
            if (modified != null)
                consumer.accept(modified);
        };
    }
}
