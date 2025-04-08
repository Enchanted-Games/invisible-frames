package games.enchanted.invisibleframes.mixin.access;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.HangingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HangingEntity.class)
public interface HangingEntityAccess {
    @Accessor("direction")
    Direction invisible_frames$getDirection();
}
