package yagen.waitmydawn.player;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import yagen.waitmydawn.YagensAttributes;

@EventBusSubscriber(value = Dist.CLIENT, modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.MOD)
public class KeyMappings {
    public static final String KEY_BIND_GENERAL_CATEGORY = "key.yagens_attributes.group_1";

    public static final KeyMapping COMBO_COUNT_KEYMAP =
            new KeyMapping(getResourceName("client_configs_screen"),
                    KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_G, KEY_BIND_GENERAL_CATEGORY);

    public static final KeyMapping ABILITY_1_KEYMAP =
            new KeyMapping(getResourceName("ability_1"),
                    KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_V, KEY_BIND_GENERAL_CATEGORY);

    public static final KeyMapping ABILITY_2_KEYMAP =
            new KeyMapping(getResourceName("ability_2"),
                    KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_N, KEY_BIND_GENERAL_CATEGORY);

    private static String getResourceName(String name) {
        return String.format("key.yagens_attributes.%s", name);
    }

    @SubscribeEvent
    public static void onRegisterKeybinds(RegisterKeyMappingsEvent event) {
        event.register(COMBO_COUNT_KEYMAP);
        event.register(ABILITY_1_KEYMAP);
        event.register(ABILITY_2_KEYMAP);
    }

}
