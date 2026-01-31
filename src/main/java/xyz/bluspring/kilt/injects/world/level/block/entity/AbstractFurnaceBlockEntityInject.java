package xyz.bluspring.kilt.injects.world.level.block.entity;

import com.mojang.datafixers.util.Either;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.block.entity.AbstractFurnaceBlockEntityInjection;

import java.util.Map;
import java.util.function.ObjIntConsumer;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityInject implements AbstractFurnaceBlockEntityInjection {
    @Inject(method = "add(Ljava/util/Map;Lnet/minecraft/world/level/ItemLike;I)V", at = @At("TAIL"))
    private static void kilt$appendItemToKiltMap(Map<Item, Integer> map, ItemLike item, int burnTime, CallbackInfo ci) {
        AbstractFurnaceBlockEntityInjection.kilt$itemCookTimes.put(item, burnTime);
    }

    @Inject(method = "add(Ljava/util/Map;Lnet/minecraft/tags/TagKey;I)V", at = @At("TAIL"))
    private static void kilt$appendTagToKiltMap(Map<Item, Integer> map, TagKey<Item> itemTag, int burnTime, CallbackInfo ci) {
        AbstractFurnaceBlockEntityInjection.kilt$tagCookTimes.put(itemTag, burnTime);
    }

    @CreateStatic
    private static void buildFuels(ObjIntConsumer<Either<Item, TagKey<Item>>> fuelConsumer) {
        AbstractFurnaceBlockEntityInjection.buildFuels(fuelConsumer);
    }
}
