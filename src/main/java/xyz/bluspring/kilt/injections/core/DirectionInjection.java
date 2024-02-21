package xyz.bluspring.kilt.injections.core;

import com.mojang.math.Constants;
import net.minecraft.core.Direction;

public interface DirectionInjection {
    static Direction getNearestStable(float x, float y, float z) {
        Direction direction = Direction.NORTH;
        float f = Float.MIN_VALUE;

        for (Direction value : Direction.values()) {
            float f1 = x * value.getNormal().getX() + y * value.getNormal().getY() + z * value.getNormal().getZ();
            if (f1 > f + Constants.EPSILON) {
                f = f1;
                direction = value;
            }
        }

        return direction;
    }
}
