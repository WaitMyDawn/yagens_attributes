package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.registries.Components;

@EventBusSubscriber(modid = YagensAttributes.MODID, value = Dist.CLIENT)
public final class ModModels {
    private ModModels() {}
    @SubscribeEvent public static void additional(ModelEvent.RegisterAdditional event) {
        Minecraft.getInstance().getResourceManager().listResources("models/item/mods", path -> path.getPath().endsWith(".json")).keySet().forEach(path ->
                event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(path.getNamespace(), path.getPath().substring(7, path.getPath().length() - 5)))));
    }
    @SubscribeEvent public static void baked(ModelEvent.ModifyBakingResult event) {
        var key = ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "mod"));
        var original = event.getModels().get(key);
        if (original == null) return;
        var overrides = new ItemOverrides() {
            @Override public BakedModel resolve(BakedModel base, ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
                var data = stack.get(Components.MOD_DATA.get()); if (data == null) return base;
                var manager = Minecraft.getInstance().getModelManager();
                var model = manager.getModel(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(data.type().getNamespace(), "item/mods/" + data.type().getPath())));
                return model == manager.getMissingModel() ? base : model;
            }
        };
        event.getModels().put(key, new BakedModelWrapper<BakedModel>(original) { @Override public ItemOverrides getOverrides() { return overrides; } });
    }
}
