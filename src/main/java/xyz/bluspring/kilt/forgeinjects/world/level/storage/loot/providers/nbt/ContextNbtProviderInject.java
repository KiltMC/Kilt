package xyz.bluspring.kilt.forgeinjects.world.level.storage.loot.providers.nbt;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.level.storage.loot.EntityTargetInjection;

@Mixin(ContextNbtProvider.class)
public abstract class ContextNbtProviderInject {
    @Mixin(targets = "net/minecraft/world/level/storage/loot/providers/nbt/ContextNbtProvider$2")
    public static abstract class Getter2Inject {
        @Shadow @Final private LootContext.EntityTarget val$target;

        @ModifyReturnValue(method = "getId", at = @At("RETURN"))
        private String kilt$useInternalName(String original) {
            return ((EntityTargetInjection) (Object) val$target).getName();
        }
    }
}
