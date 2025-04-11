package games.enchanted.invisibleframes.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import games.enchanted.invisibleframes.duck.InvisibleFramesAccess;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MadeItemFrameInvisibleTrigger extends SimpleCriterionTrigger<MadeItemFrameInvisibleTrigger.TriggerInstance> {
    public MadeItemFrameInvisibleTrigger() {
    }

    public @NotNull Codec<MadeItemFrameInvisibleTrigger.TriggerInstance> codec() {
        return MadeItemFrameInvisibleTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemFrame itemFrameEntity) {
        this.trigger(player, (triggerInstance -> triggerInstance.matches(player, itemFrameEntity)));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> itemFrameEntity) implements SimpleInstance {
        public static final Codec<MadeItemFrameInvisibleTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            (instance) ->
                instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(MadeItemFrameInvisibleTrigger.TriggerInstance::player),
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("item_frame").forGetter(MadeItemFrameInvisibleTrigger.TriggerInstance::itemFrameEntity)
                )
                .apply(instance, MadeItemFrameInvisibleTrigger.TriggerInstance::new)
        );

        public void validate(@NotNull CriterionValidator validator) {
            validator.validateEntity(this.itemFrameEntity, ".item_frame");
        }

        public boolean matches(ServerPlayer player, ItemFrame itemFrame) {
            return itemFrame.isInvisible() && !((InvisibleFramesAccess) itemFrame).invisibleFrames$getInvisibleItem().isEmpty();
        }

        public @NotNull Optional<ContextAwarePredicate> player() {
            return this.player;
        }

        public @NotNull Optional<ContextAwarePredicate> itemFrameEntity() {
            return this.itemFrameEntity;
        }
    }
}
