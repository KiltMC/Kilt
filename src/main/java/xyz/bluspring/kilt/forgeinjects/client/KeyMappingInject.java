package xyz.bluspring.kilt.forgeinjects.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KeyMapping.class)
public abstract class KeyMappingInject implements IForgeKeyMapping {

}
