//package yagen.waitmydawn.api.events;
//
//import net.minecraft.Util;
//import net.minecraft.core.Holder;
//import net.minecraft.core.Registry;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.LivingEntity;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.event.server.ServerStartedEvent;
//import yagen.waitmydawn.YagensAttributes;
//import yagen.waitmydawn.api.attribute.HealthMaterialType;
//import yagen.waitmydawn.api.attribute.HealthMaterialUtils;
//import yagen.waitmydawn.util.LineFileUtil;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.MOD)
//public class AutoEntityTypeClassifier {
//
//    private static boolean executed = false;
//
//    @SubscribeEvent
//    public static void onServerStarted(ServerStartedEvent event) {
//        if (executed) return;
//        executed = true;
//
//        Util.backgroundExecutor().execute(() -> {
//            Registry<EntityType<?>> reg = event.getServer().registryAccess()
//                    .registryOrThrow(Registries.ENTITY_TYPE);
//            reg.holders().forEach(ref -> {
//                EntityType<?> type = ref.value();
//                HealthMaterialType mat = getMaterialType(type);
//                ResourceLocation id = ref.key().location();
//                String filePath = "config/yagens_attributes/entity_type/" + mat.toString().toLowerCase() + "_material.json";
//                Path file = Paths.get(filePath);
//                String appendLine = "{ \"id\": \""+id.toString()+"\", \"required\": false },";
//                try {
//                    LineFileUtil.appendLine(file, appendLine);
//                } catch (IOException e) {
//                    YagensAttributes.LOGGER.warn("Failed to write seen entity file", e);
//                }
//            });
//            YagensAttributes.LOGGER.info("AutoEntityTypeClassifier finished!");
//        });
//    }
//    public static HealthMaterialType getMaterialType(EntityType<?> type) {
//        if (type.is(HealthMaterialUtils.BONE_MATERIAL)) return HealthMaterialType.BONE;
//        if (type.is(HealthMaterialUtils.FLESH_MATERIAL)) return HealthMaterialType.FLESH;
//        if (type.is(HealthMaterialUtils.METAL_MATERIAL)) return HealthMaterialType.METAL;
//        if (type.is(HealthMaterialUtils.ARTHROPOD_MATERIAL)) return HealthMaterialType.ARTHROPOD;
//        if (type.is(HealthMaterialUtils.GHOST_MATERIAL)) return HealthMaterialType.GHOST;
//        if (type.is(HealthMaterialUtils.ELEMENT_MATERIAL)) return HealthMaterialType.ELEMENT;
//        if (type.is(HealthMaterialUtils.PLANT_MATERIAL)) return HealthMaterialType.PLANT;
//        if (type.is(HealthMaterialUtils.STONE_MATERIAL)) return HealthMaterialType.STONE;
//        // flesh default
//        return HealthMaterialType.NONE;
//    }
//}