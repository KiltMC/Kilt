package xyz.bluspring.kilt.forgeinjects.world.level.storage.loot;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.fabricators_of_create.porting_lib.loot.LootTableIdCondition;
import io.github.fabricators_of_create.porting_lib.loot.extensions.LootContextExtensions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.mixin.LootContextAccessor;

import java.util.Map;
import java.util.Set;

@Mixin(value = LootContext.class, priority = 1050)
public abstract class LootContextInject implements LootContextExtensions {
    @Shadow @Nullable public abstract <T> T getParamOrNull(LootContextParam<T> parameter);

    // Not sure why, but these aren't getting implemented, so.
    private ResourceLocation queriedLootTableId;

    @Override
    public void setQueriedLootTableId(ResourceLocation queriedLootTableId) {
        if (this.queriedLootTableId == null && queriedLootTableId != null) {
            this.queriedLootTableId = queriedLootTableId;
        }
    }

    @Override
    public ResourceLocation getQueriedLootTableId() {
        return this.queriedLootTableId == null ? LootTableIdCondition.UNKNOWN_LOOT_TABLE : this.queriedLootTableId;
    }

    // Overwrite Porting Lib's
    @Override
    public int getLootingModifier() {
        return ForgeHooks.getLootingLevel(this.getParamOrNull(LootContextParams.THIS_ENTITY), this.getParamOrNull(LootContextParams.KILLER_ENTITY), this.getParamOrNull(LootContextParams.DAMAGE_SOURCE));
    }

    @Mixin(LootContext.Builder.class)
    public static abstract class BuilderInject {
        @Shadow @Final private Map<LootContextParam<?>, Object> params;
        @Shadow @Final private Map<ResourceLocation, LootContext.DynamicDrop> dynamicDrops;
        @Shadow private RandomSource random;
        @Unique private ResourceLocation queriedLootTableId;

        public BuilderInject(ServerLevel level) {}

        @CreateInitializer
        public BuilderInject(LootContext context) {
            this(context.getLevel());
            this.params.putAll(((LootContextAccessor) context).getParams());
            this.dynamicDrops.putAll(((LootContextAccessor) context).getDynamicDrops());
            this.random = context.getRandom();
            this.queriedLootTableId = context.getQueriedLootTableId();
        }

        @Redirect(method = "create", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z"))
        private boolean kilt$cancelDisallowedParams(Set instance) {
            return true;
        }

        @ModifyReturnValue(method = "create", at = @At("RETURN"))
        private LootContext kilt$addQueriedLootTableId(LootContext original) {
            original.setQueriedLootTableId(this.queriedLootTableId); // ??
            return original;
        }
    }

    @Mixin(LootContext.EntityTarget.class)
    public static abstract class EntityTargetInject {
        @Shadow @Final String name;

        public String getName() {
            return this.name;
        }
    }
}
