package yagen.waitmydawn.yagens_attributes.render;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class DamageNumberTransform {
    private DamageNumberTransform() {}

    /** AFTER_PARTICLES already supplies the view rotation through RenderSystem's model-view stack. */
    public static Matrix4f billboard(Vec3 anchor, Vec3 camera, Quaternionf cameraRotation, float rise, float horizontalPixels) {
        return billboard(anchor, camera, cameraRotation, rise, horizontalPixels, 0);
    }
    public static Matrix4f billboard(Vec3 anchor, Vec3 camera, Quaternionf cameraRotation, float rise, float horizontalPixels, float verticalPixels) {
        Vec3 relative = anchor.subtract(camera);
        return new Matrix4f().translation((float) relative.x, (float) relative.y + rise, (float) relative.z)
                .rotate(cameraRotation).scale(0.025F, -0.025F, 0.025F).translate(horizontalPixels, verticalPixels, 0);
    }
}
