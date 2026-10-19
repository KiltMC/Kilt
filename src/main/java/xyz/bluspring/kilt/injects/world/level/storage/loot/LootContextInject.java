package xyz.bluspring.kilt.injects.world.level.storage.loot;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.storage.loot.LootContextInjection;
import xyz.bluspring.kilt.mixin.LootContextBuilderAccessor;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;

@Mixin(LootContext.class)
public abstract class LootContextInject implements LootContextInjection {
    @Nullable @Unique private Identifier queriedLootTableId;

    @Override
    public void setQueriedLootTableId(@Nullable Identifier queriedLootTableId) {
        if (this.queriedLootTableId == null && queriedLootTableId != null)
            this.queriedLootTableId = queriedLootTableId;
    }

    @Override
    public @Nullable Identifier getQueriedLootTableId() {
        return this.queriedLootTableId == null ? LootTableIdCondition.UNKNOWN_LOOT_TABLE : this.queriedLootTableId;
    }

    @Override
    public Identifier kilt$getQueriedLootTableId() {
        return this.queriedLootTableId;
    }

    @Mixin(LootContext.Builder.class)
    public abstract static class BuilderInject implements LootContextInjection.BuilderInjection {
        @Shadow
        private @Nullable RandomSource random;
        @Unique @Nullable private Identifier queriedLootTableId;

        public BuilderInject(LootParams params) {}

        @CreateInitializer
        public BuilderInject(LootContext context) {
            this(((LootContextBuilderAccessor) context).getParams());
            this.random = ((LootContextBuilderAccessor) context).getRandom();
            this.queriedLootTableId = context.kilt$getQueriedLootTableId();
        }

        @Override
        public LootContext.Builder withQueriedLootTableId(Identifier queriedLootTableId) {
            this.queriedLootTableId = queriedLootTableId;
            return (LootContext.Builder) (Object) this;
        }

        @ModifyReturnValue(method = "create", at = @At("RETURN"))
        private LootContext kilt$attachLootTableIdToContext(LootContext original) {
            original.setQueriedLootTableId(this.queriedLootTableId);
            return original;
        }
    }
}
