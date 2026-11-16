package yagen.waitmydawn.yagens_attributes.data;

import net.minecraft.world.item.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public final class WeaponFamilies {
    public enum Family {
        SWORD(ModInfo.Category.SWORD), AXE(ModInfo.Category.AXE), TRIDENT(ModInfo.Category.TRIDENT),
        BOW(ModInfo.Category.BOW), CROSSBOW(ModInfo.Category.CROSSBOW), MACE(ModInfo.Category.MACE);
        private final ModInfo.Category category;
        Family(ModInfo.Category category) { this.category = category; }
        public ModInfo.Category category() { return category; }
    }
    private static final ClassDefaults<Family> FAMILIES = new ClassDefaults<>(Map.of(
            SwordItem.class, Family.SWORD, AxeItem.class, Family.AXE, TridentItem.class, Family.TRIDENT,
            BowItem.class, Family.BOW, CrossbowItem.class, Family.CROSSBOW, MaceItem.class, Family.MACE));
    private WeaponFamilies() {}
    public static Optional<Family> match(Class<?> type) { return FAMILIES.match(type); }
}
