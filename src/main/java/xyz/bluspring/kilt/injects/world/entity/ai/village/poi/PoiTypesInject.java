package xyz.bluspring.kilt.injects.world.entity.ai.village.poi;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(PoiTypes.class)
public abstract class PoiTypesInject {
    @Unique
    private static final Map<BlockState, Holder<PoiType>> kilt$forgeTypeByState = GameData.getBlockStatePointOfInterestTypeMap();

    @ModifyReturnValue(method = "forState", at = @At("RETURN"))
    private static Optional<Holder<PoiType>> kilt$tryGetByForge(Optional<Holder<PoiType>> original, BlockState state) {
        return original.or(() -> Optional.ofNullable(kilt$forgeTypeByState.get(state)));
    }
}
