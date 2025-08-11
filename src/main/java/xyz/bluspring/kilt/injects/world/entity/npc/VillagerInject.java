package xyz.bluspring.kilt.injects.world.entity.npc;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerInject extends AbstractVillager {
    public VillagerInject(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;isSleeping()Z"))
    private boolean kilt$checkSecondaryUseActive(boolean original, @Local(argsOnly = true) Player player) {
        return original || player.isSecondaryUseActive();
    }

    @WrapOperation(method = "getTypeName", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;getPath()Ljava/lang/String;"))
    private String kilt$useProperlyFormattedPath(ResourceLocation instance, Operation<String> original) {
        if (!"minecraft".equals(instance.getNamespace())) {
            return instance.getNamespace() + "." + instance.getPath();
        }

        return original.call(instance);
    }

    @Definition(id = "level", local = @Local(type = ServerLevel.class, argsOnly = true))
    @Definition(id = "getDifficulty", method = "Lnet/minecraft/server/level/ServerLevel;getDifficulty()Lnet/minecraft/world/Difficulty;")
    @Definition(id = "PEACEFUL", field = "Lnet/minecraft/world/Difficulty;PEACEFUL:Lnet/minecraft/world/Difficulty;")
    @Expression("level.getDifficulty() != PEACEFUL")
    @ModifyExpressionValue(method = "thunderHit", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanConvertToWitch(boolean original) {
        return original && EventHooks.canLivingConvert(this, EntityType.WITCH, $ -> {});
    }

    @Inject(method = "thunderHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private void kilt$callLivingConvertEvent(ServerLevel level, LightningBolt lightning, CallbackInfo ci, @Local Witch witch) {
        EventHooks.onLivingConvert(this, witch);
    }
}
