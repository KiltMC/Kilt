package xyz.bluspring.kilt.mixin.compat.porting_lib.extensions;

import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomDataPacketHandlingBlockEntity;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomRenderBoundingBoxBlockEntity;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomUpdateTagHandlingBlockEntity;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
    CustomDataPacketHandlingBlockEntity.class,
    CustomRenderBoundingBoxBlockEntity.class,
    CustomUpdateTagHandlingBlockEntity.class,
})
public interface PortingLibBlockEntityExtensionsMixin extends IBlockEntityExtension {
}
