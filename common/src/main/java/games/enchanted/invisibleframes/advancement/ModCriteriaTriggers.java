package games.enchanted.invisibleframes.advancement;

import games.enchanted.invisibleframes.InvisibleFramesConstants;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ModCriteriaTriggers {
    public static final MadeItemFrameInvisibleTrigger MADE_ITEM_FRAME_INVISIBLE;

    private static <T extends CriterionTrigger<?>> T register(Identifier id, T trigger) {
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES, id, trigger);
    }

    public static void init() {}

    static {
        MADE_ITEM_FRAME_INVISIBLE = register(Identifier.fromNamespaceAndPath(InvisibleFramesConstants.MOD_ID, "made_item_frame_invisible"), new MadeItemFrameInvisibleTrigger());
    }
}
