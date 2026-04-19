package xyz.bluspring.kilt.injects.world.entity.vehicle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

@Mixin(ChestBoat.class)
public abstract class ChestBoatInject extends Boat {
    public ChestBoatInject(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "getDropItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Items;OAK_CHEST_BOAT:Lnet/minecraft/world/item/Item;", opcode = Opcodes.GETSTATIC))
    private Item kilt$tryReturnCustomChestBoatItem(Operation<Item> original) {
        try {
            if (this.getVariant() != Boat.Type.OAK) {
                return this.getVariant().kilt$getChestBoatItem().get();
            }
        } catch (Throwable ignored) {}

        return original.call();
    }
}
