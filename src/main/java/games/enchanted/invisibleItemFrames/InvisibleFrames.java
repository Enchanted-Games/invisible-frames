package games.enchanted.invisibleItemFrames;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvisibleFrames implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("modid");
	public static final String MOD_ID = "eg-invisible-frames";
	
  public static final TagKey<Item> FABRIC_CONVENTION_GLASS_PANES_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "glass_panes"));
  public static final TagKey<Item> FABRIC_CONVENTION_GLASS_PANE_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "glass_pane"));

	@Override
	public void onInitialize() {
	}
}