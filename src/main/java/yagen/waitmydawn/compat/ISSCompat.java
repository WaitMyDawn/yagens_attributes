package yagen.waitmydawn.compat;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.lightning.ChargeSpell;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import static yagen.waitmydawn.item.mod.armor_mod.GraceArmorMod.ATTRIBUTE_SET;

public class ISSCompat {
    public static void addAttributeSet() {
        ATTRIBUTE_SET.put(AttributeRegistry.MAX_MANA.value(), 150.0);
        ATTRIBUTE_SET.put(AttributeRegistry.SPELL_POWER.value(), 0.15);
        ATTRIBUTE_SET.put(AttributeRegistry.COOLDOWN_REDUCTION.value(), 0.2);
        ATTRIBUTE_SET.put(AttributeRegistry.CAST_TIME_REDUCTION.value(), 0.2);
        ATTRIBUTE_SET.put(AttributeRegistry.NATURE_SPELL_POWER.value(), 0.2);
    }

    public static boolean isSpellContainer(ItemStack itemStack) {
        return ISpellContainer.isSpellContainer(itemStack);
    }

    public static String getReservoirName(ItemStack itemStack, double factor) {
        if (!isSpellContainer(itemStack)) return "";
        SpellData spellData = ISpellContainer.get(itemStack).getSpellAtIndex(0);
        int level = (int) (spellData.getLevel() * factor);
        return spellData.getSpell().getSpellName() + "_" + level;
    }

    public static int getReservoirDuration(ItemStack itemStack, LivingEntity livingEntity) {
        if (!isSpellContainer(itemStack)) return 0;
        SpellData spellData = ISpellContainer.get(itemStack).getSpellAtIndex(0);
        AbstractSpell spellInstance = spellData.getSpell();
        return (int) (spellInstance.getSpellPower(spellData.getLevel(), livingEntity) * 20);
    }

    public static void addSpellEffect(LivingEntity livingEntity, String string,int duration) {
        int idx = string.lastIndexOf('_');
        String name = string.substring(0, idx);
        int level = Integer.parseInt(string.substring(idx + 1)) - 1;
        DeferredHolder<MobEffect, MobEffect> mobEffect = switch (name) {
            case "charge" -> MobEffectRegistry.CHARGED;
            case "haste" -> MobEffectRegistry.HASTENED;
            case "angel_wing" -> MobEffectRegistry.ANGEL_WINGS;
            case "oakskin" -> MobEffectRegistry.OAKSKIN;
            case "invisibility" ->MobEffectRegistry.TRUE_INVISIBILITY;
            case "spider_aspect" ->MobEffectRegistry.SPIDER_ASPECT;
            default -> null;
        };
        if (mobEffect == null) return;
        if(name.equals("invisibility")) level =0;
        livingEntity.addEffect((new MobEffectInstance(mobEffect, duration, Math.max(0, level))));
    }

    public static boolean isReservoir(ItemStack itemStack) {
        if (!isSpellContainer(itemStack)) return false;
        return switch (ISpellContainer.get(itemStack).getSpellAtIndex(0).getSpell().getSpellName())
        {
            case "charge", "haste", "angel_wing","oakskin","invisibility","spider_aspect" -> true;
            default -> false;
        };
    }
}
