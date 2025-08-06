package xyz.bluspring.kilt.mixin.compat.forge.tacz;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Mixin(targets = "com.tacz.guns.entity.EntityKineticBullet", remap = false)
public abstract class MixinEntityKineticBullet {
    @WrapMethod(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;ZLcom/tacz/guns/resource/pojo/data/gun/GunData;Lcom/tacz/guns/resource/pojo/data/gun/BulletData;)V"
    )
    private void postInit(Level worldIn, LivingEntity throwerIn, ItemStack gunItem, ResourceLocation ammoId, ResourceLocation gunId, ResourceLocation gunDisplayId, boolean isTracerAmmo, GunData gunData, BulletData bulletData, Operation<Void> original) {
        final Ship ship = VSGameUtilsKt.getShipMountedTo(throwerIn);
        if (ship != null) {
            final Vector3d pos = VectorConversionsMCKt.toJOML(throwerIn.position());
            final Vector3d oPos = new Vector3d(throwerIn.xOld, throwerIn.yOld, throwerIn.zOld);
            ship.getTransform().getWorldToShip().transformPosition(pos);
            ship.getTransform().getWorldToShip().transformPosition(oPos);
            final Vector3d newPos = oPos.add(pos.sub(oPos).mul(0.5));
            ((com.tacz.guns.entity.EntityKineticBullet)(Object)this).setPos(newPos.x, newPos.y + throwerIn.getEyeHeight(), newPos.z);
        }
    }
}
