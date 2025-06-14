package xyz.bluspring.kilt.forgeinjects.network;

import net.minecraft.network.CompressionEncoder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CompressionEncoder.class)
public abstract class CompressionEncoderInject {
    // Kilt: doesn't seem like anything we should implement here
}
