package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.client.gui.*;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yagen.waitmydawn.yagens_attributes.client.ShieldHud;

@Mixin(Gui.class)
public abstract class ShieldHeartMixin {
    @Unique private boolean yagens$lockDrawn;
    @Inject(method = "renderHearts", at = @At("HEAD"))
    private void yagens$begin(CallbackInfo callback) { yagens$lockDrawn = false; }
    @WrapOperation(method = "renderHealthLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAbsorptionAmount()F"))
    private float yagens$boundedHearts(Player player, Operation<Float> original) {
        return Math.min(40, original.call(player));
    }
    @Inject(method = "renderHealthLevel", at = @At("TAIL"))
    private void yagens$amount(GuiGraphics graphics, CallbackInfo callback) {
        var minecraft = net.minecraft.client.Minecraft.getInstance(); var player = minecraft.player;
        if (player != null && player.getAbsorptionAmount() > 40)
            graphics.drawString(minecraft.font, "+" + String.format(java.util.Locale.ROOT, "%.0f", player.getAbsorptionAmount()), graphics.guiWidth() / 2 - 91, graphics.guiHeight() - 70, ShieldHud.amountColor(), true);
    }
    @WrapOperation(method = "renderHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIZZZ)V", ordinal = 1))
    private void yagens$shield(Gui gui, GuiGraphics graphics, Gui.HeartType type, int x, int y, boolean hardcore, boolean highlight, boolean half, Operation<Void> original) {
        if (!ShieldHud.shield(graphics, x, y, half, highlight)) original.call(gui, graphics, type, x, y, hardcore, highlight, half);
    }
    @Inject(method = "renderHeart", at = @At("TAIL"))
    private void yagens$lock(GuiGraphics graphics, Gui.HeartType type, int x, int y, boolean hardcore, boolean highlight, boolean half, CallbackInfo callback) {
        if (!yagens$lockDrawn && ShieldHud.locked() && type != Gui.HeartType.CONTAINER && type != Gui.HeartType.ABSORBING && !highlight) {
            ShieldHud.lock(graphics, x, y); yagens$lockDrawn = true;
        }
    }
}
