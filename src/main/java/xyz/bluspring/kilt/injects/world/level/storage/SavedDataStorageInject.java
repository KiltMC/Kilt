package xyz.bluspring.kilt.injects.world.level.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.common.IOUtilities;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.RecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.storage.SavedDataStorageInjection;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

@Mixin(SavedDataStorage.class)
public abstract class SavedDataStorageInject implements SavedDataStorageInjection {
    @Shadow private CompletableFuture<?> pendingWriteFuture;
    @Shadow @Final private Path dataFolder;
    
    @Unique private @Nullable ServerLevel level;

    @Override
    public void kilt$setLevel(ServerLevel level) {
        this.level = level;
    }

    public SavedDataStorageInject(Path dataFolder, DataFixer fixerUpper, HolderLookup.Provider registries) {}

    @CreateInitializer
    public SavedDataStorageInject(@Nullable ServerLevel level, Path dataFolder, DataFixer fixerUpper, HolderLookup.Provider registries) {
        this(dataFolder, fixerUpper, registries);
        this.level = level;
    }

    @Definition(id = "type", local = @Local(type = SavedDataType.class, name = "type", argsOnly = true))
    @Definition(id = "constructor", method = "Lnet/minecraft/world/level/saveddata/SavedDataType;constructor()Ljava/util/function/Supplier;")
    @Definition(id = "get", method = "Ljava/util/function/Supplier;get()Ljava/lang/Object;")
    @Expression("@(type.constructor()).get()")
    @WrapOperation(method = "computeIfAbsent", at = @At("MIXINEXTRAS:EXPRESSION"))
    private <T extends SavedData> Supplier<T> kilt$tryHandleWithLevel(SavedDataType<T> instance, Operation<Supplier<T>> original) {
        if (RecordMixinRefMaps.EXTENDED_SAVED_DATA_TYPE.containsKey(instance)) {
            return () -> instance.factory().create(this.level);
        }

        return original.call(instance);
    }

    @WrapOperation(method = {"readSavedData", "encodeUnchecked"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/SavedDataType;codec()Lcom/mojang/serialization/Codec;"))
    private <T extends SavedData> Codec<T> kilt$tryHandleCodecWithLevel(SavedDataType<T> instance, Operation<Codec<T>> original) {
        if (RecordMixinRefMaps.EXTENDED_SAVED_DATA_TYPE.containsKey(instance)) {
            return instance.codecFactory().create(this.level);
        }

        return original.call(instance);
    }

    // Kilt: readTagFromDisk nullable datafixer handled by Fabric API

    @Inject(method = "readTagFromDisk", at = @At("TAIL"))
    private void kilt$deleteTempFiles(Path dataFile, DataFixTypes type, int newVersion, CallbackInfoReturnable<CompoundTag> cir) {
        this.pendingWriteFuture = this.pendingWriteFuture.thenRunAsync(
            () -> IOUtilities.tryCleanupTempFiles(this.dataFolder, dataFile.getFileName().toString()),
            Util.ioPool()
        );
    }

    @WrapOperation(method = "tryWrite", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/nio/file/Path;)V"))
    private void kilt$ensureParentsExist(CompoundTag tag, Path file, Operation<Void> original) throws IOException {
        if (!Files.exists(file))
            Files.createDirectories(file.getParent());

        // Kilt TODO: can we make this actually good?
        IOUtilities.writeNbtCompressed(tag, file);
    }
}
