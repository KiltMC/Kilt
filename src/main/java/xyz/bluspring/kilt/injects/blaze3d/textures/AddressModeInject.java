package xyz.bluspring.kilt.injects.blaze3d.textures;

import com.mojang.blaze3d.textures.AddressMode;
import net.neoforged.neoforge.internal.NonExhaustiveEnum;
import org.spongepowered.asm.mixin.Mixin;

@NonExhaustiveEnum(reason = "Further address modes may be added")
@Mixin(AddressMode.class)
public enum AddressModeInject {
    ;
}
