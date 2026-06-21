package games.enchanted.eg_invisible_frames.common.mixin.access;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayAccess {
    @Invoker("setBackgroundColor")
    void invisibleFrames$setBackgroundColor(int backgroundColor);

    @Invoker("setText")
    void invisibleFrames$setText(Component text);
}
