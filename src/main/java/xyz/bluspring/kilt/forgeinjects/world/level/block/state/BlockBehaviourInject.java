// TRACKED HASH: 7c7fd92e3368b3a700df7efcb860051c936220b7
package xyz.bluspring.kilt.forgeinjects.world.level.block.state;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.level.block.state.BlockBehaviourPropertiesInjection;
import xyz.bluspring.kilt.mixin.PropertiesAccessor;

import java.util.function.Supplier;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourInject {
    @Shadow @Nullable protected ResourceLocation drops;
    private Supplier<ResourceLocation> lootTableSupplier;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$addLootTableSupplier(BlockBehaviour.Properties properties, CallbackInfo ci) {
        var lootTableCache = ((PropertiesAccessor) properties).getDrops();

        if (lootTableCache != null) {
            lootTableSupplier = () -> lootTableCache;
        } else if (((BlockBehaviourPropertiesInjection) properties).getLootTableSupplier() != null) {
            lootTableSupplier = ((BlockBehaviourPropertiesInjection) properties).getLootTableSupplier();
        } else {
            lootTableSupplier = () -> {
                var registryName = ForgeRegistries.BLOCKS.getKey((Block) (Object) this);
                return new ResourceLocation(registryName.getNamespace(), "blocks/" + registryName.getPath());
            };
        }
    }

    @Inject(at = @At("HEAD"), method = "getLootTable", cancellable = true)
    public void kilt$useLootTableSupplier(CallbackInfoReturnable<ResourceLocation> cir) {
        if (this.drops == null) {
            cir.setReturnValue(this.lootTableSupplier.get());
        }
    }

    @Mixin(BlockBehaviour.Properties.class)
    public static class PropertiesInject implements BlockBehaviourPropertiesInjection {
        private Supplier<ResourceLocation> lootTableSupplier;

        @Override
        public Supplier<ResourceLocation> getLootTableSupplier() {
            return lootTableSupplier;
        }

        public BlockBehaviour.Properties lootFrom(Supplier<? extends Block> blockIn) {
            this.lootTableSupplier = () -> blockIn.get().getLootTable();
            return (BlockBehaviour.Properties) (Object) this;
        }
    }
}