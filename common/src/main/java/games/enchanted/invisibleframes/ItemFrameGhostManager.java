package games.enchanted.invisibleframes;

import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import games.enchanted.invisibleframes.duck.InvisibleFramesAccess;
import games.enchanted.invisibleframes.mixin.access.DisplayAccess;
import games.enchanted.invisibleframes.mixin.access.HangingEntityAccess;
import games.enchanted.invisibleframes.mixin.access.TextDisplayAccessor;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ItemFrameGhostManager {
    private static final int FADE_OUT_TICKS = 25;
    private static final int FADE_OUT_DELAY = 7;

    private static final int STARTING_COLOUR = 0xe1F3E7DD;
    private static final int ENDING_COLOUR = 0x00F3E7DD;

    private final ItemFrame itemFrame;
    private final ServerLevel level;

    @Nullable private Display.TextDisplay display;

    private int timeAlive = 0;

    public ItemFrameGhostManager(ItemFrame itemFrame, ServerLevel level) {
        this.itemFrame = itemFrame;
        this.level = level;
    }

    public void tick() {
        // summon a particle if ItemFrame is invisible, has an item it was made invisible by, is empty, and is less than 15 blocks away from player
        if(timeAlive <= 1 && !((InvisibleFramesAccess) itemFrame).invisibleFrames$getInvisibleItem().isEmpty() && itemFrame.isInvisible() && itemFrame.getItem().isEmpty() && itemFrame.getRandom().nextInt(30) == 0) {
            spawnRandomParticle();
        }
        if(display == null) return;

        timeAlive++;

        if(timeAlive > 1 && itemFrame.getRandom().nextFloat() * (itemFrame.getItem().isEmpty() ? 1 : 2.5) < getPercentageTimeAlive()) {
            // spawn more particles as the fade out animation finishes, spawns less if the item frame has an item in it
            spawnRandomParticle();
        }

        if(timeAlive == 1) {
            ((TextDisplayAccessor) display).invisibleFrames$setBackgroundColor(ENDING_COLOUR);
        }
        if(timeAlive >= FADE_OUT_TICKS + FADE_OUT_DELAY) {
            removeEntities();
        }
    }

    private float getPercentageTimeAlive() {
        return ((float) timeAlive) / (FADE_OUT_TICKS + FADE_OUT_DELAY);
    }

    public void removeEntities() {
        if (display != null) {
            display.discard();
            display = null;
            timeAlive = 0;
        }
    }

    public void createGhost() {
        if(display != null) {
            removeEntities();
        }
        Direction direction = itemFrame.getDirection();

        // create text display and set position
        display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, this.level);
        display.setPos(
            itemFrame.getX() + (-0.03f * direction.getStepX()),
            itemFrame.getY() + (-0.03f * direction.getStepY()),
            itemFrame.getZ() + (-0.03f * direction.getStepZ())
        );

        // rotate text display to match the item frame
        float xRot;
        float yRot;
        if (direction.getAxis().isHorizontal()) {
            xRot = 0.0f;
            yRot = 180.0f - direction.getOpposite().toYRot();
        } else {
            xRot = 90 * direction.getAxisDirection().getStep();
            yRot = 180.0f;
        }
        Quaternionf rotationQuaternion = Axis.XP.rotationDegrees(xRot);
        rotationQuaternion.mul(Axis.YP.rotationDegrees(yRot));

        ((DisplayAccess) display).invisibleFrames$setTransformation(new Transformation(
            new Vector3f(-0.075f, -0.37f, 0f).rotate(rotationQuaternion),
            rotationQuaternion,
            new Vector3f(6, 3, 6),
            new Quaternionf(0f, 0f, 0f, 1f)
        ));

        // setup text display visuals
        ((TextDisplayAccessor) display).invisibleFrames$setText(Component.literal(" "));
        ((DisplayAccess) display).invisibleFrames$setInterpolationDuration(FADE_OUT_TICKS);
        ((DisplayAccess) display).invisibleFrames$setInterpolationDelay(FADE_OUT_DELAY);
        ((TextDisplayAccessor) display).invisibleFrames$setBackgroundColor(STARTING_COLOUR);

        // add data to prevent saving the text display entity
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putBoolean(InvisibleFramesConstants.ENTITY_NO_SAVE_TAG, true);
        display.setComponent(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));

        this.level.addFreshEntity(display);
    }

    public void spawnRandomParticle() {
        RandomSource random = itemFrame.getRandom();
        AABB boundingBox = itemFrame.getBoundingBox().deflate(0.1);
        Direction frameDirection = ((HangingEntityAccess) itemFrame).invisible_frames$getDirection();

        double x = random.nextFloat() * boundingBox.getXsize();
        double y = random.nextFloat() * boundingBox.getYsize();
        double z = random.nextFloat() * boundingBox.getZsize();

        x += boundingBox.minX;
        y += boundingBox.minY;
        z += boundingBox.minZ;

        if(frameDirection != Direction.DOWN) {
            y += 0.125;
        }

        for (ServerPlayer player : ((InvisibleFramesAccess) itemFrame).invisibleFrames$getTrackedPlayers()) {
            if(player.distanceTo(itemFrame) > 15 && player.hasLineOfSight(itemFrame)) return;
            player.connection.send(new ClientboundLevelParticlesPacket(ParticleTypes.END_ROD, false, true, x, y, z, 0.0f, 0.0f, 0.0f, 0.0f, 1));
        }
    }
}
