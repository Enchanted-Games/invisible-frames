package games.enchanted.invisibleItemFrames.mixin;

import games.enchanted.invisibleItemFrames.duck.InvisibleFramesAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import games.enchanted.invisibleItemFrames.InvisibleFrames;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin extends AbstractDecorationEntity implements InvisibleFramesAccess {
	public ItemFrameEntityMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow
	private boolean fixed;
	@Shadow
	public void onPlace() {
		throw new AssertionError( "onPlace not shadowed" );
	};
	@Shadow
	public ItemStack getHeldItemStack() {
		throw new AssertionError( "getHeldItemStack not shadowed" );
	};

	@Unique
	private ItemStack invisible_frames$GLASS_PANE_ITEM = ItemStack.EMPTY;
	@Unique
	private ArrayList<ServerPlayerEntity> invisible_frames$playersTrackingThisFrame = new ArrayList<>();

	// check if a player attacks ItemFrame while holding any item from #eg-invisible-frames:makes_item_frames_invisible
	//   if so, set ItemFrame to invisible and save the glass pane
	@Inject(
		at = @At( value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;isAlwaysInvulnerableTo(Lnet/minecraft/entity/damage/DamageSource;)Z", shift = At.Shift.AFTER ),
		method = "damage",
		cancellable = true
	)
	public void damage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir ) {
		Entity attacker = source.getAttacker();

		// if player damages an item frame that isn't invisible
		if ( attacker instanceof PlayerEntity player && !this.isInvisible() && !this.getWorld().isClient ) {
			final ItemStack playMainHandStack = player.getMainHandStack();

			if( playMainHandStack.isIn( InvisibleFrames.MAKES_ITEM_FRAMES_INVISIBLE_TAG ) ) {
				ItemStack paneTakenFromPlayer = playMainHandStack.copy();
				paneTakenFromPlayer.setCount(1);
				setGlassPaneItemStack( paneTakenFromPlayer );
				if ( !player.getAbilities().creativeMode ) {
					playMainHandStack.decrement(1);
				}

				this.setInvisible( true );
				this.playSound( SoundEvents.ENTITY_ITEM_FRAME_PLACE, 1.0F, 1.0F );
				
				cir.setReturnValue( true );
			}
		}
		// if player damages an item frame that is invisible, has a glass pane item and has no held item
		else if ( attacker instanceof PlayerEntity && this.isInvisible() && !this.getGlassPaneItemStack().isEmpty() && this.getHeldItemStack().isEmpty() ) {
			this.dropGlassPaneStack( attacker );
			this.setInvisible( false );
			cir.setReturnValue( true );
		}
	}

	// drop glass pane when item frame breaks
	@Inject(at = @At("HEAD"), method = "onBreak")
	private void onBreak(ServerWorld world, Entity breaker, CallbackInfo ci ) {
		this.dropGlassPaneStack( breaker );
	}
	
	// save glass_pane_item to ItemFrame NBT
	@Inject(at = @At("HEAD"), method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	private void writeCustomDataToNbt( NbtCompound nbt, CallbackInfo ci ) {
		if( !getGlassPaneItemStack().isEmpty() ) {
			nbt.put( "glass_pane_item", getGlassPaneItemStack().toNbt( this.getRegistryManager() ) );
		}
	}
	
	// read glass_pane_item from ItemFrame NBT
	@Inject(at = @At("HEAD"), method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	private void readCustomDataFromNbt( NbtCompound nbt, CallbackInfo ci ) {
		ItemStack itemStack;
		if ( nbt.contains( "glass_pane_item", NbtElement.COMPOUND_TYPE ) ) {
			NbtCompound nbtCompound = nbt.getCompound( "glass_pane_item" );
			itemStack = ItemStack.fromNbt( this.getRegistryManager(), nbtCompound ).orElse( ItemStack.EMPTY );
		} else {
			itemStack = ItemStack.EMPTY;
		}
		this.setGlassPaneItemStack( itemStack );
	}
	
	@Unique
	private void dropGlassPaneStack( Entity entity ) {
		if(!(this.getWorld() instanceof ServerWorld serverWorld)) return;
		if ( this.fixed || !serverWorld.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS) ) {
			return;
		}
		
		ItemStack paneStack = this.getGlassPaneItemStack();

		if ( entity instanceof PlayerEntity ) {
			PlayerEntity player = (PlayerEntity) entity;
			if ( player.getAbilities().creativeMode ) {
				return;
			}
		}
		
		this.dropStack( serverWorld, paneStack );
		setGlassPaneItemStack( ItemStack.EMPTY );
	}

	@Unique
	private ItemStack getGlassPaneItemStack() {
		return invisible_frames$GLASS_PANE_ITEM;
	}

	@Unique
	private void setGlassPaneItemStack( ItemStack stack ) {
		invisible_frames$GLASS_PANE_ITEM = stack;
	}
	
	public void onStartedTrackingBy( ServerPlayerEntity player ) {
		invisible_frames$playersTrackingThisFrame.add( player );
	}
	
	public void onStoppedTrackingBy( ServerPlayerEntity player ){
		invisible_frames$playersTrackingThisFrame.remove( player );
	}

	@Override
	public ItemStack invisible_frames$getGlassPaneStack() {
		return this.getGlassPaneItemStack();
	}

	@Override
	public ArrayList<ServerPlayerEntity> invisible_frames$getTrackedPlayers() {
		return this.invisible_frames$playersTrackingThisFrame;
	}
}