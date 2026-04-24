package xyz.bluspring.kilt.injects.world.level.storage.loot;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.fabricators_of_create.porting_lib.loot.extensions.LootContextExtensions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.mixin.world.level.storage.loot.LootContextAccessor;

import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;

@Mixin(LootContext.class)
public abstract class LootContextInject implements LootContextExtensions {
    private LootContextInject(LootParams params, RandomSource random, HolderGetter.Provider provider) {}

    @CreateInitializer
    private LootContextInject(LootParams params, RandomSource random, HolderGetter.Provider provider, ResourceLocation queriedLootTableId) {
        this(params, random, provider);
        this.setQueriedLootTableId(queriedLootTableId);
    }

    @Mixin(LootContext.Builder.class)
    public abstract static class BuilderInject {
        @Shadow @Nullable private RandomSource random;

        @Unique @Nullable private ResourceLocation queriedLootTableId;

        public BuilderInject(LootParams params) {}

        @CreateInitializer
        public BuilderInject(LootContext context) {
            this(((LootContextAccessor) context).getParams());
            this.random = ((LootContextAccessor) context).getRandom();
            this.queriedLootTableId = context.getQueriedLootTableId();
        }

        @Unique
        public LootContext.Builder withQueriedLootTableId(ResourceLocation queriedLootTableId) {
            this.queriedLootTableId = queriedLootTableId;
            return (LootContext.Builder) (Object) this;
        }

        @ModifyReturnValue(method = "create", at = @At("RETURN"))
        private LootContext kilt$appendLootTableIdToContext(LootContext original) {
            original.setQueriedLootTableId(this.queriedLootTableId);
            return original;
        }
    }
}
