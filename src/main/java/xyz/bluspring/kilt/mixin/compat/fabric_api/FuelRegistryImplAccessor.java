package xyz.bluspring.kilt.mixin.compat.fabric_api;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FuelRegistryImpl.class)
public interface FuelRegistryImplAccessor {
    @Accessor
    Object2IntMap<ItemLike> getItemCookTimes();

    @Accessor
    Object2IntMap<TagKey<Item>> getTagCookTimes();
}
