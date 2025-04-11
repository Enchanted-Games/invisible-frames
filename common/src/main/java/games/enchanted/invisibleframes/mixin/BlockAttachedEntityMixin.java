package games.enchanted.invisibleframes.mixin;

import games.enchanted.invisibleframes.duck.InvisibleFramesAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin extends Entity {
    public BlockAttachedEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(
        at = @At("HEAD"),
        method = "tick()V"
    )
    public void tick(CallbackInfo ci) {
        if(!((Object) this instanceof ItemFrame itemFrameEntity)) return;
        ((InvisibleFramesAccess) itemFrameEntity).invisibleFrames$tickGhostManager();
    }
}
