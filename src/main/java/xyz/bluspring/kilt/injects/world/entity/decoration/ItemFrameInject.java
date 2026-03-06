package xyz.bluspring.kilt.injects.world.entity.decoration;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemFrame.class)
public abstract class ItemFrameInject {
    @WrapOperation(method = "removeFramedMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/MapItem;getSavedData(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;"))
    private MapItemSavedData kilt$tryGetStackBasedSavedData(MapId mapId, Level level, Operation<MapItemSavedData> original, @Local(argsOnly = true) ItemStack stack) {
        var result = original.call(mapId, level);

        if (result == null) {
            return MapItem.getSavedData(stack, level);
        }

        return result;
    }
}
