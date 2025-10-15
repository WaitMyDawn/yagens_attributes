package yagen.waitmydawn.gui.overlays;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import yagen.waitmydawn.api.attribute.DamageBonusTable;
import yagen.waitmydawn.api.attribute.DamageType;
import yagen.waitmydawn.api.attribute.HealthMaterialType;
import yagen.waitmydawn.api.attribute.HealthMaterialUtils;
import yagen.waitmydawn.config.ClientConfigs;
import yagen.waitmydawn.registries.DataAttachmentRegistry;
import yagen.waitmydawn.util.RayUtils;

import java.util.Formattable;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

import java.util.LinkedHashMap;

public class ScanInfoOverlay implements LayeredDraw.Layer {
    public static final ScanInfoOverlay instance = new ScanInfoOverlay();

    static DamageType[] damageType = {DamageType.PUNCTURE, DamageType.SLASH, DamageType.IMPACT,
            DamageType.COLD, DamageType.HEAT, DamageType.ELECTRICITY, DamageType.TOXIN,
            DamageType.CORROSIVE, DamageType.VIRAL, DamageType.MAGNETIC, DamageType.RADIATION,
            DamageType.BLAST, DamageType.GAS};

    @Override
    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        var player = Minecraft.getInstance().player;

        if (Minecraft.getInstance().options.hideGui || player.isSpectator()) {
            return;
        }
        if (!shouldShowComboCount(player))
            return;

        int color = ChatFormatting.GOLD.getColor();
        LivingEntity target = RayUtils.getTargetedLiving(player, 64.0);
        if (target == null || target == player) return;

        var font = Minecraft.getInstance().font;
        var screenWidth = guiHelper.guiWidth();
        var screenHeight = guiHelper.guiHeight();
        int x = screenWidth / 25;
        int y = screenHeight / 10;

        guiHelper.pose().pushPose();
        float scale = 1f;
        guiHelper.pose().scale(scale, scale, scale);

        Map<DamageType, Float> damageMap = new HashMap<>();
        HealthMaterialType mat = HealthMaterialUtils.getMaterialType(target);
        for (DamageType tempType : damageType) {
            Float factor = DamageBonusTable.getBonus(mat, tempType);
            if (factor != 1f) damageMap.put(tempType, factor);
        }

        Map<DamageType, Float> sorted = damageMap.entrySet()
                .stream()
                .sorted(Map.Entry.<DamageType, Float>comparingByValue().reversed())
                .collect(toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        String entityInfo = target.getName().getString() + " : " + mat.toString().toLowerCase();
        guiHelper.drawString(font, entityInfo,
                (int) (x / scale),
                (int) (y / scale),
                color, true);
        y += font.lineHeight;
        for (DamageType tempType : sorted.keySet()) {
            String damageInfo = tempType + " = " + String.format("%.1f", sorted.get(tempType));
            if(sorted.get(tempType)>1) color = ChatFormatting.GREEN.getColor();
            else color = ChatFormatting.RED.getColor();
            guiHelper.drawString(font, damageInfo,
                    (int) (x / scale),
                    (int) (y / scale),
                    color, true);
            y += font.lineHeight;
        }

        guiHelper.pose().popPose();
    }

    public static boolean shouldShowComboCount(Player player) {
        return player.getPersistentData().getBoolean("is_scan");
    }
}
