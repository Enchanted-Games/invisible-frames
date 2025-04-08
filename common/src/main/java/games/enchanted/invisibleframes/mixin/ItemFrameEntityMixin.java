package games.enchanted.invisibleframes.mixin;


import java.util.ArrayList;
import java.util.Optional;

import games.enchanted.invisibleframes.InvisibleFramesConstants;
import games.enchanted.invisibleframes.duck.InvisibleFramesAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public abstract class ItemFrameEntityMixin extends HangingEntity implements InvisibleFramesAccess {
	public ItemFrameEntityMixin(EntityType<? extends HangingEntity> entityType, Level world) {
		super(entityType, world);
	}

	@Shadow
	private boolean fixed;
	@Shadow
	public void playPlacementSound() {
		throw new AssertionError( "onPlace not shadowed" );
	};
	@Shadow
	public abstract ItemStack getItem();

	@Unique
	private ItemStack invisibleFrames$GLASS_PANE_ITEM = ItemStack.EMPTY;

	@Unique
	private ArrayList<ServerPlayer> invisibleFrames$playersTrackingThisFrame = new ArrayList<>();

	// check if a player attacks ItemFrame while holding any item from #eg-invisible-frames:makes_item_frames_invisible
	//   if so, set ItemFrame to invisible and save the glass pane
	@Inject(
		at = @At( value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ItemFrame;isInvulnerableToBase(Lnet/minecraft/world/damagesource/DamageSource;)Z", shift = At.Shift.AFTER ),
		method = "hurtServer",
		cancellable = true
	)
	public void damage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir ) {
		Entity attacker = source.getEntity();

		// if player damages an item frame that isn't invisible
		if (attacker instanceof Player player && !this.isInvisible() && !this.level().isClientSide) {
			final ItemStack playerMainHandStack = player.getMainHandItem();

			if(playerMainHandStack.is(InvisibleFramesConstants.MAKES_ITEM_FRAMES_INVISIBLE_TAG)) {
				ItemStack copiedStack = playerMainHandStack.copy();
				copiedStack.setCount(1);
				invisibleFrames$setGlassPaneItemStack(copiedStack);
				if (!player.getAbilities().instabuild) {
					playerMainHandStack.shrink(1);
				}

				this.setInvisible(true);
				this.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
				
				cir.setReturnValue(true);
			}
		}
		// if player damages an item frame that is invisible, has a glass pane item and has no held item
		else if (attacker instanceof Player && this.isInvisible() && !this.invisibleFrames$getInvisibleItemStack().isEmpty() && this.getItem().isEmpty()) {
			this.invisibleFrames$dropInvisibleItemStack(attacker);
			this.setInvisible(false);
			cir.setReturnValue(true);
		}
	}

	// drop glass pane when item frame breaks
	@Inject(at = @At("HEAD"), method = "dropItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)V")
	private void onBreak(ServerLevel world, Entity breaker, CallbackInfo ci ) {
		this.invisibleFrames$dropInvisibleItemStack(breaker);
	}
	
	// save glass_pane_item to ItemFrame NBT
	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	private void writeCustomDataToNbt( CompoundTag nbt, CallbackInfo ci ) {
		if(!invisibleFrames$getInvisibleItemStack().isEmpty()) {
			nbt.put("eg_invisible_frames:made_invisible_item", invisibleFrames$getInvisibleItemStack().save(this.registryAccess()));
		}
	}
	
	// read glass_pane_item from ItemFrame NBT
	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
	private void readCustomDataFromNbt( CompoundTag nbt, CallbackInfo ci ) {
		// check if made_invisible_item field exists and get it
		Optional<CompoundTag> invisibleItemCompound = nbt.getCompound("eg_invisible_frames:made_invisible_item");
		ItemStack itemStack = invisibleItemCompound.map(
			compoundTag -> ItemStack.parse(this.registryAccess(), compoundTag).orElse(ItemStack.EMPTY)
		).orElse(ItemStack.EMPTY);

		if(itemStack.isEmpty()) {
			// check if glass_pane_item exists if made_invisible_item doesnt
			Optional<CompoundTag> glassPaneCompound = nbt.getCompound("glass_pane_item");
			itemStack = glassPaneCompound.map(
				compoundTag -> ItemStack.parse(this.registryAccess(), compoundTag).orElse(ItemStack.EMPTY)
			).orElse(ItemStack.EMPTY);
		}

		this.invisibleFrames$setGlassPaneItemStack(itemStack);
	}
	
	@Unique
	private void invisibleFrames$dropInvisibleItemStack(Entity entity ) {
		if(!(this.level() instanceof ServerLevel serverWorld)) return;
		if ( this.fixed || !serverWorld.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS) ) {
			return;
		}

		if (entity instanceof Player player) {
            if ( player.getAbilities().instabuild ) {
				return;
			}
		}

		ItemStack paneStack = this.invisibleFrames$getInvisibleItemStack();
		this.spawnAtLocation( serverWorld, paneStack );
		invisibleFrames$setGlassPaneItemStack( ItemStack.EMPTY );
	}

	@Unique
	private ItemStack invisibleFrames$getInvisibleItemStack() {
		return invisibleFrames$GLASS_PANE_ITEM;
	}

	@Unique
	private void invisibleFrames$setGlassPaneItemStack(ItemStack stack ) {
		invisibleFrames$GLASS_PANE_ITEM = stack;
	}

	@Override
	public void startSeenByPlayer(@NotNull ServerPlayer player) {
		invisibleFrames$playersTrackingThisFrame.add(player);
	}

	@Override
	public void stopSeenByPlayer(@NotNull ServerPlayer player){
		invisibleFrames$playersTrackingThisFrame.remove(player);
	}

	@Override
	public ItemStack invisibleFrames$getInvisibleItem() {
		return this.invisibleFrames$getInvisibleItemStack();
	}

	@Override
	public ArrayList<ServerPlayer> invisibleFrames$getTrackedPlayers() {
		return this.invisibleFrames$playersTrackingThisFrame;
	}
}