package xyz.bluspring.kilt.forgeinjects.world.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClipContext.class)
public abstract class ClipContextInject {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/CollisionContext;of(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/shapes/CollisionContext;"))
    private CollisionContext kilt$tryUseEmptyCollisionContext(Entity entity, Operation<CollisionContext> original) {
        return entity == null ? CollisionContext.empty() : original.call(entity);
    }
}
