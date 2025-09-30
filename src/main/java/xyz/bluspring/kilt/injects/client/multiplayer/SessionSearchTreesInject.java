package xyz.bluspring.kilt.injects.client.multiplayer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.CreativeModeTabSearchRegistry;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.multiplayer.SessionSearchTreesInjection;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(SessionSearchTrees.class)
public abstract class SessionSearchTreesInject implements SessionSearchTreesInjection {
    @Shadow public static SessionSearchTrees.Key CREATIVE_TAGS;
    @Shadow public abstract void updateCreativeTags(List<ItemStack> items);
    @Shadow public abstract void updateCreativeTooltips(HolderLookup.Provider registries, List<ItemStack> items);

    @Shadow
    private CompletableFuture<SearchTree<ItemStack>> creativeByTagSearch;
    @Shadow
    private CompletableFuture<SearchTree<ItemStack>> creativeByNameSearch;
    @Unique private SessionSearchTrees.Key kilt$key;

    @Override
    public void updateCreativeTags(List<ItemStack> items, SessionSearchTrees.Key key) {
        this.kilt$key = key;
        this.updateCreativeTags(items);
        this.kilt$key = null;
    }

    @ModifyExpressionValue(method = "updateCreativeTags", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/SessionSearchTrees;CREATIVE_TAGS:Lnet/minecraft/client/multiplayer/SessionSearchTrees$Key;"))
    private SessionSearchTrees.Key kilt$useCustomKeyIfAvailable(SessionSearchTrees.Key original) {
        if (this.kilt$key != null)
            return this.kilt$key;

        return original;
    }

    @ModifyExpressionValue(method = "method_60368", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/SessionSearchTrees;creativeByTagSearch:Ljava/util/concurrent/CompletableFuture;", opcode = Opcodes.GETFIELD))
    private CompletableFuture<SearchTree<ItemStack>> kilt$useNeoTagSearchTree(CompletableFuture<SearchTree<ItemStack>> original) {
        if (this.kilt$key != null)
            return CreativeModeTabSearchRegistry.getTagSearchTree(this.kilt$key);

        return original;
    }

    @WrapOperation(method = "method_60368", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private <U> CompletableFuture<U> kilt$storeTagSearchTree(Supplier<U> supplier, Executor executor, Operation<CompletableFuture<U>> original) {
        var future = original.call(supplier, executor);

        if (this.kilt$key != null) {
            CreativeModeTabSearchRegistry.putTagSearchTree(this.kilt$key, (CompletableFuture<SearchTree<ItemStack>>) future);
            return (CompletableFuture<U>) this.creativeByTagSearch;
        }

        return future;
    }

    @Override
    public SearchTree<ItemStack> creativeTagSearch(SessionSearchTrees.Key key) {
        return CreativeModeTabSearchRegistry.getTagSearchTree(key).join();
    }

    @Override
    public void updateCreativeTooltips(HolderLookup.Provider provider, List<ItemStack> items, SessionSearchTrees.Key key) {
        this.kilt$key = key;
        this.updateCreativeTooltips(provider, items);
        this.kilt$key = null;
    }

    @ModifyExpressionValue(method = "updateCreativeTooltips", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/SessionSearchTrees;CREATIVE_NAMES:Lnet/minecraft/client/multiplayer/SessionSearchTrees$Key;"))
    private SessionSearchTrees.Key kilt$useCustomKeyOnNameIfAvailable(SessionSearchTrees.Key original) {
        if (this.kilt$key != null)
            return this.kilt$key;

        return original;
    }

    @ModifyExpressionValue(method = "method_60369", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/SessionSearchTrees;creativeByNameSearch:Ljava/util/concurrent/CompletableFuture;", opcode = Opcodes.GETFIELD))
    private CompletableFuture<SearchTree<ItemStack>> kilt$useNeoNameSearchTree(CompletableFuture<SearchTree<ItemStack>> original) {
        if (this.kilt$key != null)
            return CreativeModeTabSearchRegistry.getNameSearchTree(this.kilt$key);

        return original;
    }

    @WrapOperation(method = "method_60369", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private <U> CompletableFuture<U> kilt$storeNameSearchTree(Supplier<U> supplier, Executor executor, Operation<CompletableFuture<U>> original) {
        var future = original.call(supplier, executor);

        if (this.kilt$key != null) {
            CreativeModeTabSearchRegistry.putNameSearchTree(this.kilt$key, (CompletableFuture<SearchTree<ItemStack>>) future);
            return (CompletableFuture<U>) this.creativeByNameSearch;
        }

        return future;
    }

    @Override
    public SearchTree<ItemStack> creativeNameSearch(SessionSearchTrees.Key key) {
        return CreativeModeTabSearchRegistry.getNameSearchTree(key).join();
    }
}
