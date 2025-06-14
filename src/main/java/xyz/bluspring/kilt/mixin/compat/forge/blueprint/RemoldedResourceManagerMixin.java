package xyz.bluspring.kilt.mixin.compat.forge.blueprint;

import net.fabricmc.fabric.impl.resource.loader.FabricLifecycledResourceManager;
import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

// Kilt: If this is not implemented, all Fabric resource reloaders fully fail to function.
@Pseudo
@Mixin(targets = "com.teamabnormals.blueprint.common.remolder.RemoldedResourceManager")
public abstract class RemoldedResourceManagerMixin implements FabricLifecycledResourceManager {
    @Shadow @Final private PackType packType;

    @Override
    public PackType fabric_getResourceType() {
        return this.packType;
    }
}
