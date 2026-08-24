// TRACKED HASH: d5622944d672b2af11ae93f5d7d4e097c075ddc4
package xyz.bluspring.kilt.injects.world.level;

import java.util.List;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionInject {
    @Shadow @Final private Level level;

    @Unique private List<BlockPos> kilt$toBlow = List.of();

    @Definition(id = "level", field = "Lnet/minecraft/world/level/ServerExplosion;level:Lnet/minecraft/server/level/ServerLevel;")
    @Definition(id = "getEntities", method = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;")
    @Expression("this.level.getEntities(?, ?)")
    @ModifyExpressionValue(method = "hurtEntities", at = @At("MIXINEXTRAS:EXPRESSION"))
    private List<Entity> kilt$callNeoExplosionDetonate(List<Entity> original) {
        EventHooks.onExplosionDetonate(this.level, (ServerExplosion) (Object) this, original, this.kilt$toBlow);
        return original;
    }

    @Definition(id = "entity", local = @Local(type = Entity.class, name = "entity"))
    @Definition(id = "push", method = "Lnet/minecraft/world/entity/Entity;push(Lnet/minecraft/world/phys/Vec3;)V")
    @Expression("entity.push(?)")
    @ModifyVariable(method = "hurtEntities", at = @At("MIXINEXTRAS:EXPRESSION"), name = "knockback")
    private Vec3 kilt$modifyExplosionKnockback(Vec3 knockback, @Local Entity entity) {
        return EventHooks.getExplosionKnockback(this.level, (ServerExplosion) (Object) this, entity, knockback, this.kilt$toBlow);
    }

    @WrapOperation(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerExplosion;hurtEntities()V"))
    private void kilt$storeExplodingBlocks(ServerExplosion instance, Operation<Void> original, @Local(name = "toBlow") List<BlockPos> toBlow) {
        this.kilt$toBlow = toBlow;
        original.call(instance);
        this.kilt$toBlow = List.of();
    }
}
