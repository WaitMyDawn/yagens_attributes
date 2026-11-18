package yagen.waitmydawn.yagens_attributes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yagen.waitmydawn.yagens_attributes.client.WeakPointDebugRenderer;

@Mixin(EntityRenderDispatcher.class)
public abstract class WeakPointDebugMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderHitbox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;FFFF)V"))
    private void yagens$renderWeakPoints(PoseStack pose, VertexConsumer lines, Entity entity, float partialTick,
                                        float red, float green, float blue, Operation<Void> original) {
        original.call(pose, lines, entity, partialTick, red, green, blue);
        WeakPointDebugRenderer.render(pose, lines, entity, partialTick);
    }
}
