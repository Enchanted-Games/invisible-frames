# Invisible Frames
A survival-friendly way to make Invisible Item Frames. To make an Item Frame invisible, simply punch it with any Glass Pane. Punch the item frame again drop the Glass Pane on the ground and make it visible again.

This mod is fully server-side which means it only needs to be installed on the server and any client (even vanilla) can make Invisible Item Frames!

## Features
- Invisible Item Frames made with the mod will emit a glowing white particle when they are not holding an item.
- Items that can make a frame invisible can be changed via a datapack.
- Items that were used to make the frame invisible are dropped when the Item Frame is broken.

## Customisation
Items that can make Item Frames invisible are controlled by an item tag, this is basically just list of items.

To add items to this tag, you can make a datapack and add the file `data/eg-invisible-frames/tags/items/makes_item_frames_invisible.json`. See the [default one here](https://github.com/Enchanted-Games/invisible-frames/tree/main/src/main/resources/data/eg-invisible-frames/tags/items) or [read more about tags](https://minecraft.wiki/w/Tag#JSON_format).

By default, this contains `minecraft:glass_pane` and `#c:glass_panes` (this is a [Fabric Conventional Tag](https://fabricmc.net/wiki/community:common_tags) that includes vanilla and most modded glass panes).

## License

<p xmlns:cc="http://creativecommons.org/ns#" >Invisible Frames by <a rel="cc:attributionURL dct:creator" property="cc:attributionName" href="https://enchanted.games">ioblackshaw (a.k.a. Enchanted_Games)</a> is licensed under <a href="http://creativecommons.org/licenses/by-nc/4.0/?ref=chooser-v1" target="_blank" rel="license noopener noreferrer" style="display:inline-block;">CC BY-NC 4.0<img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/cc.svg?ref=chooser-v1"><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/by.svg?ref=chooser-v1"><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/nc.svg?ref=chooser-v1"></a></p>
Video content creators may monetise videos including this work provided the license is followed.
