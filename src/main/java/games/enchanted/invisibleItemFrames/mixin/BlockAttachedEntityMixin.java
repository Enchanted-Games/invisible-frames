package games.enchanted.invisibleItemFrames.mixin;

import games.enchanted.invisibleItemFrames.duck.InvisibleFramesAccess;
import games.enchanted.invisibleItemFrames.mixin.access.HangingEntityAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin extends Entity {
    public BlockAttachedEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
        at = @At("HEAD"),
        method = "tick()V"
    )
    public void tick(CallbackInfo ci) {
        if(!((Object) this instanceof ItemFrameEntity itemFrameEntity)) return;
        // summon a particle if ItemFrame is invisible, has a glass_pane_item in its NBT, is empty, and is less than 15 blocks away from player
        if( !((InvisibleFramesAccess) itemFrameEntity).invisible_frames$getGlassPaneStack().isEmpty() && this.isInvisible() && itemFrameEntity.getHeldItemStack().isEmpty() && random.nextInt(30) == 0) {
            Box boundingBox = this.getBoundingBox();

            double x = boundingBox.minX + (boundingBox.getLengthX() * random.nextDouble());
            double y = boundingBox.minY + (boundingBox.getLengthY() * random.nextDouble());
            double z = boundingBox.minZ + (boundingBox.getLengthZ() * random.nextDouble());
            if(((HangingEntityAccess) itemFrameEntity).invisible_frames$getFacing() == Direction.UP) {
                y += 0.125;
            }

            for ( ServerPlayerEntity player : ((InvisibleFramesAccess) itemFrameEntity).invisible_frames$getTrackedPlayers()) {
                if(player.distanceTo(this) > 15 && player.canSee(this)) {
                    return;
                }
                player.networkHandler.sendPacket( new ParticleS2CPacket( ParticleTypes.END_ROD, false, true, x, y, z, 0.0f, 0.0f, 0.0f, 0.0f, 1 ) );
            }
        }
    }
}
