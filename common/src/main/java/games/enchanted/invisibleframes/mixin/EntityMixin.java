package games.enchanted.invisibleframes.mixin;

import games.enchanted.invisibleframes.InvisibleFramesConstants;
import games.enchanted.invisibleframes.duck.InvisibleFramesAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow private CustomData customData;

    @Inject(
        at = @At("HEAD"),
        method = "save",
        cancellable = true
    )
    public void invisibleFrames$skipSavingEntityIfCustomDataPresent(ValueOutput output, CallbackInfoReturnable<Boolean> cir) {
        if(this.customData.contains(InvisibleFramesConstants.ENTITY_NO_SAVE_TAG)) {
            cir.setReturnValue(false);
        }
    }

    // inject into the following here, rather than overriding in ItemFrameMixin for compatibility
    @Inject(
        at = @At("HEAD"),
        method = {"discard", "kill"}
    )
    public void invisibleFrames$onItemFrameRemoved(CallbackInfo ci) {
        if(!((Object) this instanceof InvisibleFramesAccess frame)) return;
        frame.invisibleFrames$resetGhostAnimation();
    }

    @Inject(
        at = @At("HEAD"),
        method = "startSeenByPlayer"
    )
    public void invisibleFrames$startSeenByPlayer(ServerPlayer player, CallbackInfo ci) {
        if(!((Object) this instanceof InvisibleFramesAccess frame)) return;
        frame.invisibleFrames$startSeenByPlayer(player);
    }

    @Inject(
        at = @At("HEAD"),
        method = "stopSeenByPlayer"
    )
    public void invisibleFrames$stopSeenByPlayer(ServerPlayer player, CallbackInfo ci) {
        if(!((Object) this instanceof InvisibleFramesAccess frame)) return;
        frame.invisibleFrames$stopSeenByPlayer(player);
    }
}
