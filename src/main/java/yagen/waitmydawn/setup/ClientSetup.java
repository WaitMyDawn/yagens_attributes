package yagen.waitmydawn.setup;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.NoneMod;
import yagen.waitmydawn.api.registry.ModRegistry;
import yagen.waitmydawn.gui.mod_operation.ModOperationScreen;
import yagen.waitmydawn.gui.mod_recycle.ModRecycleScreen;
import yagen.waitmydawn.particle.DamageNumberParticle;
import yagen.waitmydawn.registries.*;
import yagen.waitmydawn.render.ModModel;
import yagen.waitmydawn.render.ModOperationRenderer;
import yagen.waitmydawn.util.IMinecraftInstanceHelper;
import yagen.waitmydawn.util.MinecraftInstanceHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static void init() {
        BlockEntityRenderers.register(
                BlockRegistry.MOD_OPERATION_TABLE_ENTITY.get(),
                ModOperationRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerMenuScreen(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.MOD_OPERATION_MENU.get(), ModOperationScreen::new);
        event.register(MenuRegistry.MOD_RECYCLE_MENU.get(), ModRecycleScreen::new);
    }

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent e) {
        //Item Properties
        e.enqueueWork(() -> {
            Attributes.ATTACK_DAMAGE.value().setSyncable(true);
            MinecraftInstanceHelper.instance = new IMinecraftInstanceHelper() {
                @Nullable
                @Override
                public Player player() {
                    return Minecraft.getInstance().player;
                }
            };
        });
        e.enqueueWork(ClientSetup::init);
    }

//    @SubscribeEvent
//    public static void rendererRegister(EntityRenderersEvent.RegisterRenderers event) {
//        BlockEntityRenderers.register(BlockRegistry.MOD_OPERATION_TABLE_ENTITY.get(), ModOperationRenderer::new);
//    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.DAMAGE_NUMBER.get(), DamageNumberParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerSpecialModels(ModelEvent.RegisterAdditional event) {
        ModRegistry.getEnabledMods()
                .stream()
                .filter(m -> !(m instanceof NoneMod))
                .forEach(mod -> event.register(ModelResourceLocation.standalone(ModModel.getModModelLocation(mod))));
    }

    @SubscribeEvent
    public static void replaceItemModels(ModelEvent.ModifyBakingResult event) {
        var key = new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "mod"), "inventory");
        event.getModels().computeIfPresent(key, (k, oldModel) -> new ModModel(oldModel, event.getModelBakery()));
    }
}

