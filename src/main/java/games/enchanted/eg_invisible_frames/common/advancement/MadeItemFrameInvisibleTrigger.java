package games.enchanted.eg_invisible_frames.common.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import games.enchanted.eg_invisible_frames.common.duck.InvisibleFramesAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

//? if minecraft: <= 26.1 {
/*import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
*///? } else {
//? if minecraft: <= 26.2 {
/*import net.minecraft.advancements.predicates.ContextAwarePredicate;
*///? } else {
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
//? }
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
//? }

public class MadeItemFrameInvisibleTrigger extends SimpleCriterionTrigger<MadeItemFrameInvisibleTrigger.TriggerInstance> {
    public MadeItemFrameInvisibleTrigger() {
    }

    public @NotNull Codec<MadeItemFrameInvisibleTrigger.TriggerInstance> codec() {
        return MadeItemFrameInvisibleTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemFrame itemFrameEntity) {
        this.trigger(player, (triggerInstance -> triggerInstance.matches(player, itemFrameEntity)));
    }

    //? if minecraft: <= 26.2 {
    /*public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> itemFrameEntity) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<MadeItemFrameInvisibleTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            (instance) ->
                instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(MadeItemFrameInvisibleTrigger.TriggerInstance::player),
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("item_frame").forGetter(MadeItemFrameInvisibleTrigger.TriggerInstance::itemFrameEntity)
                )
                .apply(instance, MadeItemFrameInvisibleTrigger.TriggerInstance::new)
        );

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
    *///? } else {
    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, Optional<Holder<LootItemCondition>> itemFrameEntity) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<MadeItemFrameInvisibleTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            (instance) ->
                instance.group(
                    LootItemCondition.CODEC.optionalFieldOf("player").forGetter(MadeItemFrameInvisibleTrigger.TriggerInstance::player),
                    LootItemCondition.CODEC.optionalFieldOf("item_frame").forGetter(MadeItemFrameInvisibleTrigger.TriggerInstance::itemFrameEntity)
                )
                .apply(instance, MadeItemFrameInvisibleTrigger.TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, ItemFrame itemFrame) {
            return itemFrame.isInvisible() && !((InvisibleFramesAccess) itemFrame).invisibleFrames$getInvisibleItem().isEmpty();
        }

        public @NotNull Optional<Holder<LootItemCondition>> player() {
            return this.player;
        }

        public @NotNull Optional<Holder<LootItemCondition>> itemFrameEntity() {
            return this.itemFrameEntity;
        }
    }
    //? }

}
