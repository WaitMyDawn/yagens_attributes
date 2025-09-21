package yagen.waitmydawn.player;

import yagen.waitmydawn.YagensAttributes;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.InputEvent;
import yagen.waitmydawn.gui.ComboPositionScreen;

import java.util.ArrayList;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientInputEvents {
    private static final ArrayList<KeyState> KEY_STATES = new ArrayList<>();

    private static final KeyState COMBO_COUNT_STATE = register(KeyMappings.COMBO_COUNT_KEYMAP);

    private static int useKeyId = Integer.MIN_VALUE;
    public static boolean isUseKeyDown;
    public static boolean hasReleasedSinceCasting;
    public static boolean isShiftKeyDown;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
//        if (!FMLLoader.isProduction()) {
//            if (event.getKey() == InputConstants.KEY_NUMPAD9 && event.getAction() == InputConstants.PRESS) {
//                YagensAttributes.LOGGER.debug("breakpoint");
//            }
//        }
        handleInputEvent(event.getKey(), event.getAction());
    }

    private static void handleInputEvent(int button, int action) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        if (button == InputConstants.KEY_LSHIFT) {
            isShiftKeyDown = action >= InputConstants.PRESS;
        }

        if (COMBO_COUNT_STATE.wasPressed() && minecraft.screen == null) {
            minecraft.setScreen(new ComboPositionScreen());
        }
        update();
    }

    private static void update() {
        for (KeyState k : KEY_STATES) {
            k.update();
        }
    }

    private static KeyState register(KeyMapping key) {
        var k = new KeyState(key);
        KEY_STATES.add(k);
        return k;
    }
}