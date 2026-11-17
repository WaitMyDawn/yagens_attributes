package yagen.waitmydawn.yagens_attributes.compat;

import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.init.ModItems;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import net.minecraft.world.item.*;
import yagen.waitmydawn.yagens_attributes.core.AttackKind;
import java.util.function.ToDoubleFunction;

/** References to Cataclysm are confined to this conditionally initialized adapter. */
final class CataclysmCompat {
    private CataclysmCompat() {}
    static void register() {
        ranged(ModItems.CURSED_BOW.get(), 3, 0, stack -> CMCommonConfig.CursedBow.damage * 3);
        ranged(ModItems.WRATH_OF_THE_DESERT.get(), 0, 0, stack -> CMCommonConfig.WrathOfTheDesert.damage);
        ranged(ModItems.LASER_GATLING.get(), 0, 2, stack -> CMCommonConfig.LaserGatling.damage);
        ranged(ModItems.VOID_ASSULT_SHOULDER_WEAPON.get(), 0, 0, stack -> 8);
        ranged(ModItems.WITHER_ASSULT_SHOULDER_WEAPON.get(), 0, 0, stack -> CMCommonConfig.WASW.missileDamage);
        for (Item item : new Item[]{ModItems.CORAL_SPEAR.get(), ModItems.CORAL_BARDICHE.get(), ModItems.ASTRAPE.get(), ModItems.CERAUNUS.get(), ModItems.BRONTES.get()})
            ranged(item, 0, 0, null);
        for (Item item : new Item[]{ModItems.ANCIENT_SPEAR.get(), ModItems.GAUNTLET_OF_BULWARK.get(), ModItems.GAUNTLET_OF_GUARD.get(),
                ModItems.GAUNTLET_OF_MAELSTROM.get(), ModItems.INFERNAL_FORGE.get(), ModItems.VOID_FORGE.get(), ModItems.THE_INCINERATOR.get(),
                ModItems.SOUL_RENDER.get(), ModItems.THE_ANNIHILATOR.get(), ModItems.THE_IMMOLATOR.get(), ModItems.TIDAL_CLAWS.get(), ModItems.MEAT_SHREDDER.get()})
            NativeWeapons.register(item, new NativeWeapons.Adapter(AttackKind.MELEE, true, false, 0, 0, null));
        NativeAttacks.effect(Void_Rune_Entity.class);
        NativeAttacks.effect(Flame_Jet_Entity.class);
        NativeAttacks.effect(Phantom_Halberd_Entity.class);
        NativeAttacks.effect(Tidal_Tentacle_Entity.class);
        NativeAttacks.effect(com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity.class);
        NativeAttacks.effect(com.github.L_Ender.cataclysm.entity.effect.Sandstorm_Entity.class);
        NativeAttacks.effect(com.github.L_Ender.cataclysm.entity.effect.Void_Vortex_Entity.class);
        NativeAttacks.effect(com.github.L_Ender.cataclysm.entity.effect.Wave_Entity.class);
        NativeAttacks.effect(com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity.class);
        NativeAttacks.effect(com.github.L_Ender.cataclysm.entity.effect.Lightning_Area_Effect_Entity.class);
        NativeAttacks.effect(com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity.class);
    }
    private static void ranged(Item item, int countBaseline, int period, ToDoubleFunction<ItemStack> baseline) {
        NativeWeapons.register(item, new NativeWeapons.Adapter(AttackKind.RANGED, true, true, countBaseline, period, baseline));
    }
}
