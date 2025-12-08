package yagen.waitmydawn.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import yagen.waitmydawn.config.ClientConfigs;

public class ClientConfigsScreen extends Screen {

    public ClientConfigsScreen() {
        super(Component.translatable("ui.yagens_attributes.client_configs_screen_title"));
    }

    private EditBox damageNumberInput;
    private EditBox missionPositionChange;
    private EditBox missionSummonChange;

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("ui.yagens_attributes.open_info")
                        .append(Component.translatable("ui.yagens_attributes.combo_position_title")), b -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new ComboPositionScreen(this));
                })
                .bounds((this.width - 200) / 2, 60, 200, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("ui.yagens_attributes.open_info")
                        .append(Component.translatable("ui.yagens_attributes.mission_position_title")), b -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new MissionPositionScreen(this));
                })
                .bounds((this.width - 200) / 2, 90, 200, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable(
                        "ui.yagens_attributes.damage_number_enlarge"), b -> {
                })
                .bounds((this.width - 200) / 2, 120, 160, 20)
                .build());
        damageNumberInput = new EditBox(
                this.font,
                this.width / 2 + 70, 120,
                30, 20,
                Component.translatable("ui.yagens_attributes.damage_number_enlarge"));
        damageNumberInput.setValue(String.valueOf(ClientConfigs.DAMAGE_NUMBER_ENLARGE.get()));
        damageNumberInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        this.addRenderableWidget(damageNumberInput);

        this.addRenderableWidget(Button.builder(Component.translatable(
                        "ui.yagens_attributes.show_mission_position"), b -> {
                    missionPositionChange.setValue(String.valueOf((!ClientConfigs.SHOW_MISSION_POSITION.get())));
                })
                .bounds((this.width - 200) / 2, 150, 160, 20)
                .build());
        missionPositionChange = new EditBox(
                this.font,
                this.width / 2 + 70, 150,
                30, 20,
                Component.translatable("ui.yagens_attributes.show_mission_position"));
        missionPositionChange.setValue(String.valueOf(ClientConfigs.SHOW_MISSION_POSITION.get()));
        this.addRenderableWidget(missionPositionChange);

        this.addRenderableWidget(Button.builder(Component.translatable(
                        "ui.yagens_attributes.show_mission_summon"), b -> {
                    missionSummonChange.setValue(String.valueOf((!ClientConfigs.SHOW_MISSION_SUMMON.get())));
                })
                .bounds((this.width - 200) / 2, 180, 160, 20)
                .build());
        missionSummonChange = new EditBox(
                this.font,
                this.width / 2 + 70, 180,
                30, 20,
                Component.translatable("ui.yagens_attributes.show_mission_summon"));
        missionSummonChange.setValue(String.valueOf(ClientConfigs.SHOW_MISSION_SUMMON.get()));
        this.addRenderableWidget(missionSummonChange);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            double damageNumberValue = Float.parseFloat(damageNumberInput.getValue());
            boolean showMissionPosition = Boolean.parseBoolean(missionPositionChange.getValue());
            boolean showMissionSummon= Boolean.parseBoolean(missionSummonChange.getValue());
            if (damageNumberValue <= 0) damageNumberValue = ClientConfigs.DAMAGE_NUMBER_ENLARGE.getDefault();
            ClientConfigs.DAMAGE_NUMBER_ENLARGE.set(damageNumberValue);
            ClientConfigs.SHOW_MISSION_POSITION.set(showMissionPosition);
            ClientConfigs.SHOW_MISSION_SUMMON.set(showMissionSummon);
            ClientConfigs.SPEC.save();
            onClose();
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }
}