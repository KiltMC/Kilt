package xyz.bluspring.kilt.forgeinjects.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.fabricators_of_create.porting_lib.loot.LootHooks;
import io.github.fabricators_of_create.porting_lib.loot.extensions.LootTableExtensions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.storage.loot.LootPoolInjection;
import xyz.bluspring.kilt.injections.world.level.storage.loot.LootTableInjection;

import java.util.List;
import java.util.Objects;

@Mixin(LootTable.class)
public abstract class LootTableInject implements LootTableInjection, LootTableExtensions {
    @Shadow @Final @Mutable public LootPool[] pools;
    @Unique private List<LootPool> kilt$pools;
    @Unique private boolean isFrozen = false;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$createPoolsList(LootContextParamSet paramSet, LootPool[] pools, LootItemFunction[] functions, CallbackInfo ci) {
        this.kilt$pools = Lists.newArrayList(this.pools);
    }

    @ModifyReturnValue(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    private ObjectArrayList<ItemStack> kilt$modifyLootTable(ObjectArrayList<ItemStack> original, @Local(argsOnly = true) LootContext context) {
        return ForgeHooks.modifyLoot(this.getLootTableId(), original, context);
    }

    @WrapOperation(method = "validate", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/storage/loot/LootTable;pools:[Lnet/minecraft/world/level/storage/loot/LootPool;"))
    private LootPool[] kilt$useListSize(LootTable instance, Operation<LootPool[]> original) {
        this.kilt$setPoolsArray();
        return original.call(instance);
    }

    @WrapOperation(method = "validate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootPool;validate(Lnet/minecraft/world/level/storage/loot/ValidationContext;)V"))
    private void kilt$useListValidation(LootPool instance, ValidationContext context, Operation<Void> original, @Local int i) {
        original.call(this.kilt$pools.get(i), context);
    }

    @Override
    public void freeze() {
        this.isFrozen = true;
        this.kilt$pools.forEach(pool -> ((LootPoolInjection) pool).freeze());
    }

    @Override
    public boolean isFrozen() {
        return isFrozen;
    }

    private void checkFrozen() {
        if (this.isFrozen())
            throw new RuntimeException("Attempted to modify LootTable after being finalized!");
    }

    private void kilt$setPoolsArray() {
        this.pools = this.kilt$pools.toArray(new LootPool[0]);
    }

    @Override
    public LootPool getPool(String name) {
        return this.kilt$pools.stream().filter(e -> name.equals(e.getName())).findFirst().orElse(null);
    }

    @Override
    public LootPool removePool(String name) {
        checkFrozen();
        for (LootPool pool : this.kilt$pools) {
            if (name.equals(pool.getName())) {
                this.kilt$pools.remove(pool);
                this.kilt$setPoolsArray();
                return pool;
            }
        }
        return null;
    }

    @Override
    public void addPool(LootPool pool) {
        checkFrozen();
        if (kilt$pools.stream().anyMatch(e -> e == pool || e.getName() != null && e.getName().equals(pool.getName())))
            throw new RuntimeException("Attempted to add a duplicate pool to loot table: " + pool.getName());

        this.kilt$pools.add(pool);
        this.kilt$setPoolsArray();
    }

    // These are supposed to be implemented by Porting Lib, but they're not kicking in for some reason??
    @Unique private ResourceLocation lootTableId;

    public void setLootTableId(ResourceLocation id) {
        if (this.lootTableId != null) {
            throw new IllegalStateException("Attempted to rename loot table from '" + this.lootTableId + "' to '" + id + "': this is not supported");
        } else {
            this.lootTableId = Objects.requireNonNull(id);
        }
    }

    public ResourceLocation getLootTableId() {
        return this.lootTableId;
    }

    @ModifyReturnValue(
        method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
        at = @At("RETURN")
    )
    public ObjectArrayList<ItemStack> port_lib$modifyGlobalLootTable(ObjectArrayList<ItemStack> list, LootContext context) {
        return LootHooks.modifyLoot(this.getLootTableId(), list, context);
    }
}
