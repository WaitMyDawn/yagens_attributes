package yagen.waitmydawn.setup;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.NoneMod;
import yagen.waitmydawn.api.registry.ModRegistry;
import yagen.waitmydawn.gui.mod_operation.ModOperationScreen;
import yagen.waitmydawn.gui.mod_recycle.ModRecycleScreen;
import yagen.waitmydawn.gui.reservoirs.ReservoirsScreen;
import yagen.waitmydawn.particle.*;
import yagen.waitmydawn.registries.*;
import yagen.waitmydawn.render.*;
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
        event.register(MenuRegistry.RESERVOIRS_MENU.get(), ReservoirsScreen::new);
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
            EntityRenderers.register(EntityRegistry.BLADE.get(), BladeEntityRenderer::new);
            EntityRenderers.register(EntityRegistry.RESERVOIR.get(), ReservoirRenderer::new);
            EntityRenderers.register(EntityRegistry.ENERGY_ORB.get(), EnergyOrbRenderer::new);
        });
        e.enqueueWork(ClientSetup::init);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.DAMAGE_NUMBER.get(), DamageNumberParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.ELECTRICITY.get(), ElectricityParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.HEAT.get(), HeatParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLAST.get(), BlastParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.COLD.get(), ColdParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.TOXIN.get(), ToxinParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.GAS.get(), GasParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerSpecialModels(ModelEvent.RegisterAdditional event) {
        ModRegistry.getEnabledMods()
                .stream()
                .filter(m -> !(m instanceof NoneMod))
                .forEach(mod -> event.register(ModelResourceLocation.standalone(ModModel.getModModelLocation(mod))));
        for (int level : new int[]{0, 1, 2})
            event.register(ModelResourceLocation.standalone(EndoModel.getModModelLocation(level)));
    }

    @SubscribeEvent
    public static void replaceItemModels(ModelEvent.ModifyBakingResult event) {
        var keyMod = new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "mod"), "inventory");
        var keyEndo = new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "endo"), "inventory");
        event.getModels().computeIfPresent(keyMod, (k, oldModel) -> new ModModel(oldModel, event.getModelBakery()));
        event.getModels().computeIfPresent(keyEndo, (k, oldModel) -> new EndoModel(oldModel, event.getModelBakery()));
    }
}

