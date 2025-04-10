package games.enchanted.invisibleframes.mixin;

import games.enchanted.invisibleframes.duck.InvisibleFramesAccess;
import games.enchanted.invisibleframes.mixin.access.HangingEntityAccess;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
        // summon a particle if ItemFrame is invisible, has an item it was made invisible by, is empty, and is less than 15 blocks away from player
        if(!((InvisibleFramesAccess) itemFrameEntity).invisibleFrames$getInvisibleItem().isEmpty() && this.isInvisible() && itemFrameEntity.getItem().isEmpty() && random.nextInt(30) == 0) {
            AABB boundingBox = this.getBoundingBox();

            double x = boundingBox.minX + (boundingBox.getXsize() * random.nextDouble());
            double y = boundingBox.minY + (boundingBox.getYsize() * random.nextDouble());
            double z = boundingBox.minZ + (boundingBox.getZsize() * random.nextDouble());
            if(((HangingEntityAccess) itemFrameEntity).invisible_frames$getDirection() == Direction.UP) {
                y += 0.125;
            }

            for (ServerPlayer player : ((InvisibleFramesAccess) itemFrameEntity).invisibleFrames$getTrackedPlayers()) {
                if(player.distanceTo(this) > 15 && player.hasLineOfSight(this)) return;
                player.connection.send( new ClientboundLevelParticlesPacket( ParticleTypes.END_ROD, false, true, x, y, z, 0.0f, 0.0f, 0.0f, 0.0f, 1 ) );
            }
        }
    }
}
