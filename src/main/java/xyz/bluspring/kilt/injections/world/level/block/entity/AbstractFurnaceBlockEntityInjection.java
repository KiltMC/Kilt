package xyz.bluspring.kilt.injections.world.level.block.entity;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.mixin.compat.fabric_api.FuelRegistryImplAccessor;

import java.util.function.ObjIntConsumer;

public interface AbstractFurnaceBlockEntityInjection {
    Object2IntMap<ItemLike> kilt$itemCookTimes = new Object2IntLinkedOpenHashMap<>();
    Object2IntMap<TagKey<Item>> kilt$tagCookTimes = new Object2IntLinkedOpenHashMap<>();

    static void buildFuels(ObjIntConsumer<Either<Item, TagKey<Item>>> fuelConsumer) {
        kilt$itemCookTimes.forEach((item, time) -> fuelConsumer.accept(Either.left(item.asItem()), time));
        kilt$tagCookTimes.forEach((tag, time) -> fuelConsumer.accept(Either.right(tag), time));
    }
}
