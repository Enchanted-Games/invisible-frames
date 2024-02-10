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
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import games.enchanted.invisibleItemFrames.InvisibleFrames;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public class ItemFrameEntityMixin extends AbstractDecorationEntity {
	protected ItemFrameEntityMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
		super(entityType, world);
	}

	private static final TrackedData<ItemStack> GLASS_PANE_ITEM;

	@Inject( at = @At("HEAD"), method = "Lnet/minecraft/entity/decoration/ItemFrameEntity;initDataTracker()V" )
	public void initDataTracker(CallbackInfo ci) {
    this.getDataTracker().startTracking(GLASS_PANE_ITEM, ItemStack.EMPTY);
	}

	static {
		GLASS_PANE_ITEM = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	}
	
	// check if player attacks ItemFrame while holding minecraft:glass_pane or any item from #c:glass_panes or #c:glass_pane
	//   if so, set ItemFrame to invisible and save the glass pane
	@Inject(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;getHeldItemStack()Lnet/minecraft/item/ItemStack;",
			shift = At.Shift.AFTER
		),
		method = "Lnet/minecraft/entity/decoration/ItemFrameEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
		cancellable = true
	)
	public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {

    if ( source.getAttacker() instanceof PlayerEntity && !this.isInvisible() && !this.getWorld().isClient ) {
			final PlayerEntity player = (PlayerEntity) source.getAttacker();
			final ItemStack playMainHandStack = player.getMainHandStack();

			if( 
				playMainHandStack.isOf( Items.GLASS_PANE ) || // fallback in case there are no convention tags loaded
				playMainHandStack.isIn( InvisibleFrames.FABRIC_CONVENTION_GLASS_PANES_TAG ) ||
				playMainHandStack.isIn( InvisibleFrames.FABRIC_CONVENTION_GLASS_PANE_TAG )
			) {
				ItemStack paneTakenFromPlayer = playMainHandStack.copy();
				paneTakenFromPlayer.setCount(1);
				setGlassPaneItemStack(paneTakenFromPlayer);
				if ( !player.getAbilities().creativeMode ) {
					playMainHandStack.decrement(1);
				}

				this.setInvisible( true );
				this.playSound( SoundEvents.ENTITY_ITEM_FRAME_PLACE, 1.0F, 1.0F );
				
				ci.setReturnValue( true );
			}
		}
	}

	// drops the glass_pane_item from NBT when the ItemFrame is killed
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/decoration/ItemFrameEntity;kill()V")
	private void kill(CallbackInfo ci) {
		if( this.isInvisible() && !getGlassPaneItemStack().isEmpty() ) {
			this.dropStack( getGlassPaneItemStack() );
			setGlassPaneItemStack(ItemStack.EMPTY);
		}
	}
	
	// save glass_pane_item to ItemFrame NBT
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/decoration/ItemFrameEntity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		if( !getGlassPaneItemStack().isEmpty() ) {
			nbt.put("glass_pane_item", getGlassPaneItemStack().writeNbt(new NbtCompound()));
		}
	}
	
	// read glass_pane_item from ItemFrame NBT
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/decoration/ItemFrameEntity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		NbtCompound nbtCompound = nbt.getCompound("glass_pane_item");
		if (nbtCompound != null && !nbtCompound.isEmpty()) {
				ItemStack itemStack = ItemStack.fromNbt(nbtCompound);
				this.setGlassPaneItemStack(itemStack);
		}
	}

	@Override
	public void tick() {
		super.tick();
		// summon a particle if ItemFrame is invisible and has a glass_pane_item in its NBT and is empty
		if( !getGlassPaneItemStack().isEmpty() && this.isInvisible() && getHeldItemStack().isEmpty() &&
			random.nextInt(30) == 0
		) {
			BlockPos pos = this.getBlockPos();
			double x = (double) pos.getX() + random.nextDouble();
			double y = (double) pos.getY() + random.nextDouble();
			double z = (double) pos.getZ() + random.nextDouble();
			this.getWorld().addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.0, 0.0);
		}
 }

	private ItemStack getGlassPaneItemStack() {
		return this.getDataTracker().get(GLASS_PANE_ITEM);
	}
	private void setGlassPaneItemStack(ItemStack stack) {
		this.getDataTracker().set(GLASS_PANE_ITEM, stack);
	}
	
	@Shadow
	public void onPlace() {
		throw new AssertionError("not shadowed");
	};
	@Shadow
	public void onBreak(Entity arg0) {
		throw new AssertionError("not shadowed");
	};
	@Shadow
	public int getHeightPixels() {
		throw new AssertionError("not shadowed");
	};
	@Shadow
	public int getWidthPixels() {
		throw new AssertionError("not shadowed");
	};
	@Shadow
	public ItemStack getHeldItemStack() {
		throw new AssertionError("not shadowed");
	};
}