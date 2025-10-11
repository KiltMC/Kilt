package xyz.bluspring.kilt.mixin;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@IfModLoaded("sodium")
@Mixin(LevelSlice.class)
public interface WorldSliceAccessor {
    @Accessor
    ClientLevel getLevel();
}
