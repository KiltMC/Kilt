package xyz.bluspring.kilt.injects.world.level.storage.loot;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.fabricators_of_create.porting_lib.loot.extensions.LootContextExtensions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.storage.loot.EntityTargetInjection;
import xyz.bluspring.kilt.mixin.world.level.storage.loot.LootContextAccessor;

@Mixin(value = LootContext.class, priority = 1050)
public abstract class LootContextInject implements LootContextExtensions {
    @Shadow @Nullable public abstract <T> T getParamOrNull(LootContextParam<T> parameter);

    // Overwrite Porting Lib's
    @Override
    public int getLootingModifier() {
        return CommonHooks.getLootingLevel(this.getParamOrNull(LootContextParams.THIS_ENTITY), this.getParamOrNull(LootContextParams.KILLER_ENTITY), this.getParamOrNull(LootContextParams.DAMAGE_SOURCE));
    }

    @Mixin(LootContext.Builder.class)
    public static abstract class BuilderInject {
        @Shadow private RandomSource random;
        @Shadow @Final @Mutable
        private LootParams params;
        @Unique private ResourceLocation queriedLootTableId;

        public BuilderInject(ServerLevel level) {}

        @CreateInitializer
        public BuilderInject(LootContext context) {
            this(context.getLevel());
            this.params = ((LootContextAccessor) context).getParams();
            this.random = context.getRandom();
            this.queriedLootTableId = context.getQueriedLootTableId();
        }

        public LootContext.Builder withQueriedLootTableId(ResourceLocation queriedLootTableId) {
            this.queriedLootTableId = queriedLootTableId;
            return (LootContext.Builder) (Object) this;
        }

        @ModifyReturnValue(method = "create", at = @At("RETURN"))
        private LootContext kilt$addQueriedLootTableId(LootContext original) {
            original.setQueriedLootTableId(this.queriedLootTableId); // ??
            return original;
        }
    }

    @Mixin(LootContext.EntityTarget.class)
    public static abstract class EntityTargetInject implements EntityTargetInjection {
        @Shadow @Final String name;

        public String getName() {
            return this.name;
        }
    }
}
