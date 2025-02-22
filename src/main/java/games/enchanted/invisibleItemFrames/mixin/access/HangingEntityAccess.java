package games.enchanted.invisibleItemFrames.mixin.access;

import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractDecorationEntity.class)
public interface HangingEntityAccess {
    @Accessor("facing")
    Direction invisible_frames$getFacing();
}
