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
    private int currentPage = 0;
    private final int TOTAL_PAGES = 2;

    @Override
    protected void init() {
        if (this.currentPage == 0) {
            initPage0();
        } else if (this.currentPage == 1) {
            initPage1();
        }
        initNavigationButtons();
    }

    private void initPage1(){
        this.addRenderableWidget(Button.builder(Component.translatable("ui.yagens_attributes.open_info")
                        .append(Component.translatable("ui.yagens_attributes.reservoirs_position_title")), b -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new ReservoirsPositionScreen(this));
                })
                .bounds((this.width - 200) / 2, 30, 200, 20)
                .build());
    }

    private void initPage0() {
        this.addRenderableWidget(Button.builder(Component.translatable("ui.yagens_attributes.open_info")
                        .append(Component.translatable("ui.yagens_attributes.energy_bar_position_title")), b -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new EnergyBarPositionScreen(this));
                })
                .bounds((this.width - 200) / 2, 30, 200, 20)
                .build());
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

    private void initNavigationButtons() {
        int buttonWidth = 80;
        int buttonHeight = 20;
        int bottomY = this.height - 30;

        Button prevBtn = Button.builder(Component.literal("<<"), b -> {
                    this.currentPage--;
                    this.rebuildWidgets();
                })
                .bounds(this.width / 2 - 100, bottomY, buttonWidth, buttonHeight)
                .build();
        prevBtn.active = currentPage > 0;
        this.addRenderableWidget(prevBtn);

        this.addRenderableWidget(Button.builder(Component.literal((currentPage + 1) + " / " + TOTAL_PAGES), b -> {})
                .bounds(this.width / 2 - 10, bottomY, 20, buttonHeight)
                .build()).active = false;

        Button nextBtn = Button.builder(Component.literal(">>"), b -> {
                    this.currentPage++;
                    this.rebuildWidgets();
                })
                .bounds(this.width / 2 + 20, bottomY, buttonWidth, buttonHeight)
                .build();
        nextBtn.active = currentPage < TOTAL_PAGES - 1;
        this.addRenderableWidget(nextBtn);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            double damageNumberValue = Float.parseFloat(damageNumberInput.getValue());
            boolean showMissionPosition = Boolean.parseBoolean(missionPositionChange.getValue());
            boolean showMissionSummon = Boolean.parseBoolean(missionSummonChange.getValue());
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