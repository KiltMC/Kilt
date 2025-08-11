package xyz.bluspring.kilt.injects.world.phys;

import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.phys.AABBInjection;

@Mixin(AABB.class)
public abstract class AABBInject implements AABBInjection {
    @Shadow @Final public double minX;
    @Shadow @Final public double minY;
    @Shadow @Final public double minZ;
    @Shadow @Final public double maxX;
    @Shadow @Final public double maxY;
    @Shadow @Final public double maxZ;
    @CreateStatic
    private static final AABB INFINITE = AABBInjection.INFINITE;

    @Override
    public boolean isInfinite() {
        return (Object) this == INFINITE || (Double.isInfinite(this.minX) && Double.isInfinite(this.minY) && Double.isInfinite(this.minZ)
            && Double.isInfinite(this.maxX) && Double.isInfinite(this.maxY) && Double.isInfinite(this.maxZ));
    }
}
