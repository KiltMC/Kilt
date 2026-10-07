// TRACKED HASH: 381478d70082864904d99c0e2af6d7b72e1615b7
package xyz.bluspring.kilt.injects.network.syncher;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.network.syncher.EntityDataSerializers;

@Mixin(EntityDataSerializers.class)
public abstract class EntityDataSerializersInject {
    // Kilt: handled via Fabric API
}
