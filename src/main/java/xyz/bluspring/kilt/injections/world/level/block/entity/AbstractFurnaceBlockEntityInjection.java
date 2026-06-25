package xyz.bluspring.kilt.injections.world.level.block.entity;

import java.util.function.ObjIntConsumer;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface AbstractFurnaceBlockEntityInjection {
    Object2IntMap<ItemLike> kilt$itemCookTimes = new Object2IntLinkedOpenHashMap<>();
    Object2IntMap<TagKey<Item>> kilt$tagCookTimes = new Object2IntLinkedOpenHashMap<>();

    static void buildFuels(ObjIntConsumer<Either<Item, TagKey<Item>>> fuelConsumer) {
        kilt$itemCookTimes.forEach((item, time) -> fuelConsumer.accept(Either.left(item.asItem()), time));
        kilt$tagCookTimes.forEach((tag, time) -> fuelConsumer.accept(Either.right(tag), time));
    }
}
