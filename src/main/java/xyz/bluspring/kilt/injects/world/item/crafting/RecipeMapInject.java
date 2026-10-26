package xyz.bluspring.kilt.injects.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.item.crafting.RecipeMapInjection;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

@Mixin(RecipeMap.class)
public abstract class RecipeMapInject implements RecipeMapInjection {
    @Shadow @Final private Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey;
    @Shadow @Final @Mutable private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Override
    public void order(Object2IntMap<ResourceKey<Recipe<?>>> recipePriorities) {
        Int2ObjectRBTreeMap<List<RecipeHolder<?>>> priorityBuilder = new Int2ObjectRBTreeMap<>();
        LinkedListMultimap<RecipeType<?>, RecipeHolder<?>> finalBuilder = LinkedListMultimap.create();

        for (RecipeHolder<?> recipeholder : this.byKey.values()) {
            int priority = recipePriorities.getOrDefault(recipeholder.id(), 0);
            priorityBuilder.computeIfAbsent(priority, p -> new ArrayList<>())
                .add(recipeholder);
        }

        for (List<RecipeHolder<?>> list : priorityBuilder.reversed().values()) {
            for (RecipeHolder<?> recipeHolder : list) {
                finalBuilder.put(recipeHolder.value().getType(), recipeHolder);
            }
        }

        this.byType = ImmutableMultimap.copyOf(finalBuilder);
    }
}
