package yagen.waitmydawn.registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yagen.waitmydawn.YagensAttributes;

public class PotionRegistry {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(BuiltInRegistries.POTION, YagensAttributes.MODID);

    private static final int DURATION_3_MIN = 3 * 60 * 20;
    private static final int DURATION_8_MIN = 8 * 60 * 20;

    public static final DeferredHolder<Potion, Potion> KUVA_POTION = POTIONS.register("kuva_potion",
            () -> new Potion("kuva",
                    new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION_3_MIN),
                    new MobEffectInstance(MobEffects.WATER_BREATHING, DURATION_3_MIN),
                    new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION_3_MIN, 0),
                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_3_MIN, 0),
                    new MobEffectInstance(MobEffects.ABSORPTION, DURATION_3_MIN, 1)
            ));

    public static final DeferredHolder<Potion, Potion> STRONG_KUVA_POTION = POTIONS.register("strong_kuva_potion",
            () -> new Potion("kuva",
                    new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION_3_MIN),
                    new MobEffectInstance(MobEffects.WATER_BREATHING, DURATION_3_MIN),
                    new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION_3_MIN, 1),
                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_3_MIN, 1),
                    new MobEffectInstance(MobEffects.ABSORPTION, DURATION_3_MIN, 3)
            ));

    public static final DeferredHolder<Potion, Potion> LONG_KUVA_POTION = POTIONS.register("long_kuva_potion",
            () -> new Potion("kuva",
                    new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION_8_MIN),
                    new MobEffectInstance(MobEffects.WATER_BREATHING, DURATION_8_MIN),
                    new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION_3_MIN, 0),
                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_3_MIN, 0),
                    new MobEffectInstance(MobEffects.ABSORPTION, DURATION_3_MIN, 1)
            ));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }

    public static void onBrewingRecipeRegister(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();

        builder.addMix(
                Potions.AWKWARD,
                ItemRegistry.KUVA.get(),
                KUVA_POTION
        );

        builder.addMix(
                KUVA_POTION,
                ItemRegistry.KUVA.get(),
                STRONG_KUVA_POTION
        );

        builder.addMix(
                KUVA_POTION,
                Items.REDSTONE,
                LONG_KUVA_POTION
        );
    }
}
