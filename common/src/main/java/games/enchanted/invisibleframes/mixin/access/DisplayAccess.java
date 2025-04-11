package games.enchanted.invisibleframes.mixin.access;

import com.mojang.math.Transformation;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface DisplayAccess {
    @Invoker("setTransformation")
    void invisibleFrames$setTransformation(Transformation transformation);

    @Invoker("setTransformationInterpolationDuration")
    void invisibleFrames$setInterpolationDuration(int transformationInterpolationDuration);

    @Invoker("setTransformationInterpolationDelay")
    void invisibleFrames$setInterpolationDelay(int transformationInterpolationDelay);

    @Invoker("setBrightnessOverride")
    void invisibleFrames$setBrightness(@Nullable Brightness brightnessOverride);
}
