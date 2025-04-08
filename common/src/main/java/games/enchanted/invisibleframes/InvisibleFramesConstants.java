package games.enchanted.invisibleframes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class InvisibleFramesConstants {
	public static final String MOD_ID = "eg_invisible_frames";
	public static final String MOD_NAME = "Invisible Frames";

	// this uses the old namespace for backwards compatibility with existing datapacks
	public static final TagKey<Item> MAKES_ITEM_FRAMES_INVISIBLE_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("eg-invisible-frames", "makes_item_frames_invisible"));
}