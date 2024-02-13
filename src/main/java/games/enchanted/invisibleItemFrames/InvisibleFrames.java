package games.enchanted.invisibleItemFrames;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvisibleFrames implements DedicatedServerModInitializer, ClientModInitializer {
	public static final String MOD_ID = "eg-invisible-frames";
	public static final Logger log = LoggerFactory.getLogger( MOD_ID );
	
  public static final TagKey<Item> MAKES_ITEM_FRAMES_INVISIBLE_TAG = TagKey.of( RegistryKeys.ITEM, new Identifier( MOD_ID, "makes_item_frames_invisible" ) );

	@Override
	public void onInitializeServer() {
	}

	@Override
	public void onInitializeClient() {
		log.info(MOD_ID + " is installed on the client. It will only work in singleplayer worlds or servers that also have the mod installed.");
	}
}