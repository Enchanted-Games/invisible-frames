package games.enchanted.invisibleframes.mixin;

import games.enchanted.invisibleframes.InvisibleFramesConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow private CustomData customData;

    @Inject(
        at = @At("HEAD"),
        method = "save",
        cancellable = true
    )
    public void invisibleFrames$skipSavingEntityIfCustomDataPresent(CompoundTag compound, CallbackInfoReturnable<Boolean> cir) {
        if(this.customData.contains(InvisibleFramesConstants.ENTITY_NO_SAVE_TAG)) {
            cir.setReturnValue(false);
        }
    }
}
