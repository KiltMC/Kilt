package xyz.bluspring.kilt.mixin;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@IfModLoaded(value = "sodium", maxVersion = "0.6.0")
@Mixin(WorldSlice.class)
public interface WorldSliceAccessor {
    @Accessor
    ClientLevel getWorld();
}
