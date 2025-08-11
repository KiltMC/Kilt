package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.item.MapItemInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(MapItem.class)
public abstract class MapItemInject implements MapItemInjection {
    @Shadow public static @Nullable Integer getMapId(ItemStack stack) {
        throw new IllegalStateException();
    }

    @Shadow public static @Nullable MapItemSavedData getSavedData(@Nullable Integer mapId, Level level) {
        throw new IllegalStateException();
    }

    @Inject(method = "getSavedData(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;", at = @At("HEAD"), cancellable = true)
    private static void kilt$tryUseCustomMapData(ItemStack stack, Level level, CallbackInfoReturnable<MapItemSavedData> cir) {
        var item = stack.getItem();

        if (!(item instanceof MapItem mapItem))
            return;

        if (KiltHelper.INSTANCE.hasMethodOverride(mapItem.getClass(), MapItem.class, "getCustomMapData", ItemStack.class, Level.class)) {
            cir.setReturnValue(((MapItemInjection) mapItem).getCustomMapData(stack, level));
        }
    }

    @Override
    public MapItemSavedData getCustomMapData(ItemStack stack, Level level) {
        var id = getMapId(stack);
        return getSavedData(id, level);
    }
}
