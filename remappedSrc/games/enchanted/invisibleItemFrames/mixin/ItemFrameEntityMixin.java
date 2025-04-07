package games.enchanted.invisibleItemFrames.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import games.enchanted.invisibleItemFrames.InvisibleFrames;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public class ItemFrameEntityMixin extends AbstractDecorationEntity {
	@Shadow @Final private static Logger ITEM_FRAME_LOGGER;

	protected ItemFrameEntityMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
		super(entityType, world);
	}

	@Unique
	private static final TrackedData<ItemStack> GLASS_PANE_ITEM;
	@Unique
	private ArrayList<ServerPlayerEntity> playersTrackingThisFrame = new ArrayList<>();

	@Inject( at = @At("HEAD"), method = "initDataTracker(Lnet/minecraft/entity/data/DataTracker$Builder;)V" )
	public void initDataTracker( DataTracker.Builder builder, CallbackInfo ci ) {
    builder.add( GLASS_PANE_ITEM, ItemStack.EMPTY );
	}

	static {
		GLASS_PANE_ITEM = DataTracker.registerData( ItemFrameEntity.class, TrackedDataHandlerRegistry.ITEM_STACK );
	}
	
	// check if a player attacks ItemFrame while holding any item from #eg-invisible-frames:makes_item_frames_invisible
	//   if so, set ItemFrame to invisible and save the glass pane
	@Inject(
		at = @At( value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;getHeldItemStack()Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER ),
		method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
		cancellable = true
	)
	public void damage( DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci ) {
		Entity attacker = source.getAttacker();

		// if player damages an item frame that isn't invisible
    if ( attacker instanceof PlayerEntity && !this.isInvisible() && !this.method_48926().isClient ) {
			final PlayerEntity player = (PlayerEntity) attacker;
			final ItemStack playMainHandStack = player.getMainHandStack();

			if(
				playMainHandStack.isIn( InvisibleFrames.MAKES_ITEM_FRAMES_INVISIBLE_TAG )
			) {
				ItemStack paneTakenFromPlayer = playMainHandStack.copy();
				paneTakenFromPlayer.setCount(1);
				setGlassPaneItemStack( paneTakenFromPlayer );
				if ( !player.getAbilities().creativeMode ) {
					playMainHandStack.decrement(1);
				}

				this.setInvisible( true );
				this.playSound( SoundEvents.ENTITY_ITEM_FRAME_PLACE, 1.0F, 1.0F );
				
				ci.setReturnValue( true );
			}
		}
		// if player damages an item frame that is invisible, has a glass pane item and has no held item
		else if ( attacker instanceof PlayerEntity && this.isInvisible() && !this.getGlassPaneItemStack().isEmpty() && this.getHeldItemStack().isEmpty() ) {
			this.dropGlassPaneStack( attacker );
			this.setInvisible( false );
			ci.setReturnValue( true );
		}
	}

	// drop glass pane when item frame breaks
	@Inject(at = @At("HEAD"), method = "onBreak(Lnet/minecraft/entity/Entity;)V")
	private void onBreak( @Nullable Entity entity, CallbackInfo ci ) {
		this.dropGlassPaneStack( entity );
	}
	
	// save glass_pane_item to ItemFrame NBT
	@Inject(at = @At("HEAD"), method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	private void writeCustomDataToNbt( NbtCompound nbt, CallbackInfo ci ) {
		if( !getGlassPaneItemStack().isEmpty() ) {
			nbt.put( "glass_pane_item", getGlassPaneItemStack().encode( this.getRegistryManager() ) );
		}
	}
	
	// read glass_pane_item from ItemFrame NBT
	@Inject(at = @At("HEAD"), method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	private void readCustomDataFromNbt( NbtCompound nbt, CallbackInfo ci ) {
		NbtCompound nbtCompound = nbt.getCompound( "glass_pane_item" );
		ItemStack itemStack = ItemStack.fromNbt( this.getRegistryManager(), nbtCompound ).orElse(ItemStack.EMPTY);
		this.setGlassPaneItemStack( itemStack );
	}

	@Override
	public void tick() {
		super.tick();
		// summon a particle if ItemFrame is invisible, has a glass_pane_item in its NBT, is empty, and is less than 15 blocks away from player
		if( !getGlassPaneItemStack().isEmpty() && this.isInvisible() && getHeldItemStack().isEmpty() && random.nextInt(30) == 0) {
			Box boundingBox = this.getBoundingBox();

			double x = boundingBox.minX + (boundingBox.getLengthX() * random.nextDouble());
			double y = boundingBox.minY + (boundingBox.getLengthY() * random.nextDouble());
			double z = boundingBox.minZ + (boundingBox.getLengthZ() * random.nextDouble());
			if(this.facing == Direction.UP) {
				y += 0.125;
			}

			for ( ServerPlayerEntity player : playersTrackingThisFrame ) {
				if(player.distanceTo(this) > 15 && player.canSee(this)) {
					return;
				}
				player.networkHandler.sendPacket( new ParticleS2CPacket( ParticleTypes.END_ROD, true, x, y, z, 0.0f, 0.0f, 0.0f, 0.0f, 1 ) );
			}
		}
	}
	
	@Unique
	private void dropGlassPaneStack( Entity entity ) {
		if ( this.fixed || !this.method_48926().getGameRules().getBoolean( GameRules.DO_ENTITY_DROPS ) ) {
			return;
		}
		
		ItemStack paneStack = this.getGlassPaneItemStack();

		if ( entity instanceof PlayerEntity ) {
			PlayerEntity player = (PlayerEntity) entity;
			if ( player.getAbilities().creativeMode ) {
				return;
			}
		}
		
		this.dropStack( paneStack );
		setGlassPaneItemStack( ItemStack.EMPTY );
	}

	@Unique
	private ItemStack getGlassPaneItemStack() {
		return this.getDataTracker().get( GLASS_PANE_ITEM );
	}

	@Unique
	private void setGlassPaneItemStack( ItemStack stack ) {
		this.getDataTracker().set( GLASS_PANE_ITEM, stack );
	}
	
	public void onStartedTrackingBy( ServerPlayerEntity player ) {
		playersTrackingThisFrame.add( player );
	}
	
	public void onStoppedTrackingBy( ServerPlayerEntity player ){
		playersTrackingThisFrame.remove( player );
	}

	@Shadow
	public boolean fixed;
	@Shadow
	public void onPlace() {
		throw new AssertionError( "onPlace not shadowed" );
	};
	@Shadow
	public void onBreak(Entity arg0) {
		throw new AssertionError( "onBreak not shadowed" );
	};
	@Shadow
	public int getHeightPixels() {
		throw new AssertionError( "getHeightPixels not shadowed" );
	};
	@Shadow
	public int getWidthPixels() {
		throw new AssertionError( "getWidthPixels not shadowed" );
	};
	@Shadow
	public ItemStack getHeldItemStack() {
		throw new AssertionError( "getHeldItemStack not shadowed" );
	};
	
}