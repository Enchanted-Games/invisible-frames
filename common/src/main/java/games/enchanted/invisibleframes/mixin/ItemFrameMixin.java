package games.enchanted.invisibleframes.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import games.enchanted.invisibleframes.InvisibleFramesConstants;
import games.enchanted.invisibleframes.ItemFrameGhostManager;
import games.enchanted.invisibleframes.advancement.ModCriteriaTriggers;
import games.enchanted.invisibleframes.duck.InvisibleFramesAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Optional;

@Debug(export = true)
@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin extends HangingEntity implements InvisibleFramesAccess {
	public ItemFrameMixin(EntityType<? extends HangingEntity> entityType, Level world) {
		super(entityType, world);
	}

	@Shadow
	private boolean fixed;
	@Shadow
	public abstract ItemStack getItem();

	@Shadow public abstract void playPlacementSound();

	@Unique
	private ItemStack invisibleFrames$MADE_INVISIBLE_ITEM = ItemStack.EMPTY;
	@Unique
	private final ArrayList<ServerPlayer> invisibleFrames$playersTrackingThisFrame = new ArrayList<>();

	@Unique @Nullable
	private ItemFrameGhostManager invisibleFrames$ghostManager;

	@Unique
	private ItemStack invisibleFrames$getInvisibleItemStack() {
		return invisibleFrames$MADE_INVISIBLE_ITEM;
	}
	@Unique
	private void invisibleFrames$setGlassPaneItemStack(ItemStack stack) {
		invisibleFrames$MADE_INVISIBLE_ITEM = stack;
	}

	@Unique
	private void invisibleFrames$summonGhost() {
		if(this.level() instanceof ServerLevel && invisibleFrames$ghostManager != null) {
			invisibleFrames$ghostManager.createGhost();
		}
	}

	@Unique
	private void invisibleFrames$cancelGhostAnimation() {
		if(this.level() instanceof ServerLevel && invisibleFrames$ghostManager != null) {
			invisibleFrames$ghostManager.removeEntities();
		}
	}

	@Inject(
		at = @At("TAIL"),
		method = {"<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V", "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V"}
	)
	private void invisibleFrames$initialise(CallbackInfo ci, @Local(argsOnly = true) Level level) {
		if(level instanceof ServerLevel serverLevel) {
			invisibleFrames$ghostManager = new ItemFrameGhostManager((ItemFrame) (Object) this, serverLevel);
		}
	}

	// check if a player attacks ItemFrame while holding any item from #eg-invisible-frames:makes_item_frames_invisible
	//   if so, set ItemFrame to invisible and save the glass pane
	@ModifyExpressionValue(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/decoration/ItemFrame;isInvulnerableToBase(Lnet/minecraft/world/damagesource/DamageSource;)Z"
		),
		method = "hurtServer"
	)
	public boolean invisibleFrames$onServerDamage(boolean original, @Local(argsOnly = true) DamageSource source, @Cancellable CallbackInfoReturnable<Boolean> cir) {
		if(original) return original;
		if(!(source.getEntity() instanceof ServerPlayer player)) return original;
		if(!(this.level() instanceof ServerLevel level)) return original;

		final ItemStack playerMainHandStack = player.getMainHandItem();

		// if player damages an item frame that is invisible and has a glass pane item
		// make it visible again if the player is sneaking or the item frame has no item
		if (this.isInvisible() && !this.invisibleFrames$getInvisibleItemStack().isEmpty() && (player.isShiftKeyDown() || this.getItem().isEmpty())) {
			this.invisibleFrames$dropInvisibleItemStack(player);

			this.setInvisible(false);
			this.playPlacementSound();
			invisibleFrames$cancelGhostAnimation();

			cir.setReturnValue(true);
		}
		return original;
	}

	@WrapOperation(
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ItemFrame;setRotation(I)V"),
		method = "interact"
	)
	public void invisibleFrames$onInteract(ItemFrame instance, int rotation, Operation<Void> original, @Local(argsOnly = true) Player player, @Local(argsOnly = true) InteractionHand hand, @Cancellable CallbackInfoReturnable<InteractionResult> cir) {
		if(!(player instanceof ServerPlayer serverPlayer)) {
			original.call(instance, rotation);
			return;
		}
		final ItemStack playerMainHandStack = serverPlayer.getMainHandItem();

		// if item frame has an item, try make it invisible instead of rotating if player is holding a glass pane
		if(!this.isInvisible() && this.invisibleFrames$getInvisibleItemStack().isEmpty() && playerMainHandStack.is(InvisibleFramesConstants.MAKES_ITEM_FRAMES_INVISIBLE_TAG)) {
			ItemStack copiedStack = playerMainHandStack.copy();
			copiedStack.setCount(1);
			invisibleFrames$setGlassPaneItemStack(copiedStack);
			if (!player.getAbilities().instabuild) {
				playerMainHandStack.shrink(1);
			}

			this.setInvisible(true);
			invisibleFrames$summonGhost();
			ModCriteriaTriggers.MADE_ITEM_FRAME_INVISIBLE.trigger(serverPlayer, (ItemFrame) (Object) this);

			cir.setReturnValue(InteractionResult.PASS);
		} else {
			original.call(instance, rotation);
		}
	}

	// drop glass pane when item frame breaks
	@Inject(at = @At("HEAD"), method = "dropItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)V")
	private void invisibleFrames$onDropItem(ServerLevel world, Entity breaker, CallbackInfo ci ) {
		this.invisibleFrames$dropInvisibleItemStack(breaker);
	}
	
	// save glass_pane_item to ItemFrame NBT
	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	private void invisibleFrames$saveMadeInvisibleItem(CompoundTag nbt, CallbackInfo ci ) {
        if(!invisibleFrames$getInvisibleItemStack().isEmpty()) {
			nbt.put("eg_invisible_frames:made_invisible_item", invisibleFrames$getInvisibleItemStack().save(this.registryAccess()));
		}
	}
	
	// read glass_pane_item from ItemFrame NBT
	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
	private void invisibleFrames$readMadeInvisibleItem(CompoundTag nbt, CallbackInfo ci ) {
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
	private void invisibleFrames$dropInvisibleItemStack(Entity entity) {
		if(!(this.level() instanceof ServerLevel serverWorld)) return;
		if(this.fixed || !serverWorld.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			return;
		}

		if (entity instanceof Player player && !player.getAbilities().instabuild) {
			// if player is not in creative drop the made invisible item
			ItemStack paneStack = this.invisibleFrames$getInvisibleItemStack();
			this.spawnAtLocation(serverWorld, paneStack);
		}

		invisibleFrames$setGlassPaneItemStack(ItemStack.EMPTY);
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

	@Override
	public void invisibleFrames$tickGhostManager() {
		if(invisibleFrames$ghostManager == null) return;
		invisibleFrames$ghostManager.tick();
	}
}