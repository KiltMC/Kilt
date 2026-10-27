package xyz.bluspring.kilt.injects.client.renderer.block.dispatch;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.client.model.block.BlockStateModelHooks;
import net.neoforged.neoforge.client.model.block.CustomBlockModelDefinition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.StupidWorkarounds;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.block.dispatch.BlockStateModelDispatcherInjection;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

@Mixin(BlockStateModelDispatcher.class)
public abstract class BlockStateModelDispatcherInject implements BlockStateModelDispatcherInjection {
    @Shadow public BlockStateModelDispatcherInject(Optional<BlockStateModelDispatcher.SimpleModelSelectors> simpleModels, Optional<BlockStateModelDispatcher.MultiPartDefinition> multiPart) {}
    @Shadow public abstract Map<BlockState, BlockStateModel.UnbakedRoot> instantiate(StateDefinition<Block, BlockState> stateDefinition, Supplier<String> source);

    @Shadow @Final @Mutable public static Codec<BlockStateModelDispatcher> CODEC;

    @CreateInitializer
    public BlockStateModelDispatcherInject(Optional<BlockStateModelDispatcher.SimpleModelSelectors> simpleModels, Optional<BlockStateModelDispatcher.MultiPartDefinition> multiPart, Optional<CustomBlockModelDefinition> customDefinition) {
        this(simpleModels, multiPart);
        ClientRecordMixinRefMaps.EXTENDED_BLOCK_STATE_MODEL_DISPATCHER.put((BlockStateModelDispatcher) (Object) this, new ClientRecordMixinRefMaps.ExtendedBlockStateModelDispatcher(customDefinition));
    }

    @CreateInitializer
    public BlockStateModelDispatcherInject(CustomBlockModelDefinition customDefinition) {
        this(Optional.empty(), Optional.empty(), Optional.of(customDefinition));
    }

    @Override
    public Optional<CustomBlockModelDefinition> customDefinition() {
        var extension = ClientRecordMixinRefMaps.EXTENDED_BLOCK_STATE_MODEL_DISPATCHER.get(this);
        if (extension != null)
            return extension.customDefinition();

        return Optional.empty();
    }

    @Inject(method = "instantiate", at = @At("HEAD"), cancellable = true)
    private void kilt$tryUseCustomDefinition(StateDefinition<Block, BlockState> stateDefinition, Supplier<String> source, CallbackInfoReturnable<Map<BlockState, BlockStateModel.UnbakedRoot>> cir) {
        if (this.customDefinition().isPresent() && !StupidWorkarounds.kilt$shouldUseVanillaBypass.contains((BlockStateModelDispatcher) (Object) this)) {
            cir.setReturnValue(this.customDefinition().orElseThrow().instantiate(stateDefinition, source));
        }
    }

    @Override
    public Map<BlockState, BlockStateModel.UnbakedRoot> instantiateVanilla(StateDefinition<Block, BlockState> stateDefinition, Supplier<String> source) {
        try {
            StupidWorkarounds.kilt$shouldUseVanillaBypass.add((BlockStateModelDispatcher) (Object) this);
            return this.instantiate(stateDefinition, source);
        } finally {
            StupidWorkarounds.kilt$shouldUseVanillaBypass.remove((BlockStateModelDispatcher) (Object) this);
        }
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static <O> Codec<O> kilt$tryStoreVanillaCodec(Codec<O> original) {
        // we wrap here because otherwise it might get replaced.
        // we replace in the inject below instead.
        BlockStateModelDispatcherInjection.kilt$vanillaCodecProvider.set((Codec<BlockStateModelDispatcher>) original);
        return original;
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$wrapWithNeoCodec(CallbackInfo ci) {
        CODEC = BlockStateModelHooks.kilt$makeDefinitionCodec(MapCodec.assumeMapUnsafe(CODEC));
    }
}
