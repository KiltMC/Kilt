package xyz.bluspring.kilt.mixin.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.mixinconstraints.annotations.IfDevEnvironment;
import org.spongepowered.asm.mixin.Mixin;

@IfDevEnvironment
@Mixin(value = PoseStack.class, priority = 1500)
public abstract class PoseStackMixin {
    /*@Unique private final LayeredStackTracker kilt$stackTracker = new LayeredStackTracker();

    @Inject(method = "pushPose()V", at = @At("HEAD"))
    private void kilt$pushStackTracker(CallbackInfo ci) {
        var stackTracker = kilt$stackTracker;
        var stackWalker = StackWalker.getInstance();
        stackWalker.walk(stream -> {
            var hasFound = new AtomicBoolean(false);

            stream.forEach(frame -> {
                if (!frame.getClassName().equals(PoseStack.class.getName()) && !hasFound.get()) {
                    stackTracker.push(frame.getClassName() + " " + frame.getMethodName() + ":" + frame.getLineNumber());
                    hasFound.set(true);
                }
            });

            return null;
        });
    }

    @Inject(method = "popPose()V", at = @At("HEAD"))
    private void kilt$popStackTracker(CallbackInfo ci) {
        var stackTracker = kilt$stackTracker;
        var stackWalker = StackWalker.getInstance();
        stackWalker.walk(stream -> {
            var hasFound = new AtomicBoolean(false);

            stream.forEach(frame -> {
                if (!frame.getClassName().equals(PoseStack.class.getName()) && !hasFound.get()) {
                    stackTracker.pop(frame.getClassName() + " " + frame.getMethodName() + ":" + frame.getLineNumber());
                    hasFound.set(true);
                }
            });

            return null;
        });
    }

    @WrapOperation(method = "pushPose()V", at = @At(value = "INVOKE", target = "Ljava/util/Deque;getLast()Ljava/lang/Object;"))
    private <E> E kilt$printCurrentPoseStack(Deque<E> instance, Operation<E> original) {
        try {
            return original.call(instance);
        } catch (NoSuchElementException e) {
            kilt$stackTracker.dump();
            throw new RuntimeException(e);
        }
    }*/
}
