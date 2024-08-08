package xyz.bluspring.kilt.mixin.compat.sodium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelDataManager;
import net.minecraftforge.common.extensions.IForgeBlockGetter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@IfModLoaded("sodium")
@Mixin(WorldSlice.class)
public class WorldSliceMixin implements IForgeBlockGetter {
    @Shadow @Final private ClientLevel world;

    @Override
    public @Nullable BlockEntity getExistingBlockEntity(BlockPos pos) {
        return this.world.getExistingBlockEntity(pos);
    }

    @Override
    public @Nullable ModelDataManager getModelDataManager() {
        return this.world.getModelDataManager();
    }
}
