package yagen.waitmydawn.gui.mod_operation;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.DamageType;
import yagen.waitmydawn.api.attribute.DefaultDamageTypeRegistry;
import yagen.waitmydawn.api.events.OperationModEvent;
import yagen.waitmydawn.api.mods.*;
import yagen.waitmydawn.api.util.ModCompat;
import yagen.waitmydawn.item.Mod;
import yagen.waitmydawn.registries.MenuRegistry;
import yagen.waitmydawn.registries.ItemRegistry;
import yagen.waitmydawn.registries.BlockRegistry;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.util.HomologyModGroup;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static yagen.waitmydawn.item.UnknownRivenItem.createRandomModItem;

public class ModOperationMenu extends AbstractContainerMenu {

    private final Player player;
    private final Level level;
    private final Slot itemSlot;
    private final Slot modSlot;
    private final Slot resultSlot;
    private int selectedModIndex = -1;

    protected final ResultContainer resultContainer = new ResultContainer();

    protected final Container modContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            ModOperationMenu.this.slotsChanged(this);
        }
    };

    protected final Container itemContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            ModOperationMenu.this.slotsChanged(this);
        }

        @Override
        public boolean canPlaceItem(int pSlot, ItemStack pStack) {
            return super.canPlaceItem(pSlot, pStack);
        }
    };

    public ModOperationMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, ContainerLevelAccess.NULL);
    }

    protected final ContainerLevelAccess access; // container access permission control

    public ModOperationMenu(int containerId, Inventory inv, ContainerLevelAccess access) {
        super(MenuRegistry.MOD_OPERATION_MENU.get(), containerId);
        this.access = access;
        checkContainerSize(inv, 3);
        this.level = inv.player.level();
        this.player = inv.player;

        // default player inventory
        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // put in item
        itemSlot = new Slot(itemContainer, 0, 80, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ModCompat.isWeaponToolOrArmor(stack);
            }

            @Override
            public void set(ItemStack stack) {
                // create mod container if it is not
                //super.set(ModCompat.ensureModContainer(stack, 4, level));
                super.set(ModCompat.ensureModContainer(stack, 8));
            }

            @Override
            public void onTake(Player pPlayer, ItemStack pStack) {
                ModOperationMenu.this.setSelectedMod(-1); // clear selected status
                super.onTake(pPlayer, pStack);
            }
        };

        // put in mod
        modSlot = new Slot(modContainer, 0, 80, 58) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemRegistry.MOD.get()) || stack.is(ItemRegistry.FORMA.get());
            }
        };

        resultSlot = new Slot(resultContainer, 2, 208, 131) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                ItemStack itemStack = itemSlot.getItem();

                var mutable = IModContainer.get(itemStack).mutableCopy();
                if (mutable.getModAtIndex(selectedModIndex).getMod().getUniqueInfo(1, null).isEmpty()) {
                    clearRivenRawInfo(itemStack);
                }
                mutable.removeModAtIndex(selectedModIndex);
                IModContainer.set(itemStack, mutable.toImmutable());

                super.onTake(player, itemStack);
                rebuildItemByMod(itemStack);
            }
        };

        this.addSlot(itemSlot);
        this.addSlot(modSlot);
        this.addSlot(resultSlot);

    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public void setSelectedMod(int index) {
        selectedModIndex = index;
        setupResultSlot();
    }

    public void doOperation(int selectedIndex) {
        ItemStack weaponStack = getItemSlot().getItem();
        ItemStack modStack = getModSlot().getItem();

        if (!IModContainer.isModContainer(weaponStack) || !(modStack.getItem() instanceof Mod)) {
            return;
        }

        var weaponContainer = IModContainer.get(weaponStack);
        var modContainer = IModContainer.get(modStack);
        var modData = modContainer.getModAtIndex(0);
        AbstractMod mod = modData.getMod();
        int level = modData.getLevel();

        // only RivenMod by empty getUniqueInfo
        if (mod.getUniqueInfo(level, player).isEmpty()) {
            Item item = getModSlot().getItem().get(ComponentRegistry.RIVEN_TYPE.get());
//            System.out.println("2 Riven Type: " + getModSlot().getItem().getItem() + " Item: " + item);
            if (item != null && item != getItemSlot().getItem().getItem()) {
//                System.out.println("3 Riven Type: " + getModSlot().getItem().getItem() + " Item: " + item);
                return;
            }
            if (hasRivenModData(weaponStack)) {
                player.sendSystemMessage(Component.translatable(
                        "ui.yagens_attributes.conflict_riven"));
                return;
            }
            copyRivenRawInfo(modStack, weaponStack);
        }// check homology mod
        else {
            String groupName = mod.getModName().split("_")[0];
            for (ModSlot slot : weaponContainer.getActiveMods()){
                String groupNameCheck = slot.getMod().getModName().split("_")[0];
                if (HomologyModGroup.instance().sameGroup(groupName, groupNameCheck)) {
                    player.sendSystemMessage(Component.translatable(
                            "ui.yagens_attributes.conflict", Component.translatable("mod.yagens_attributes." + slot.getMod().getModName())));
                    return;
                }
            }
        }
//        else {
//            Pattern p = Pattern.compile(
//                    "'[^']*\\.([^.]+)_([^']+)'.*args=\\[([0-9.]+)]");
//            List<MutableComponent> uniqueInfo = mod.getUniqueInfo(level, player);
//            for (var comp : uniqueInfo) {
//                String key = String.valueOf(comp);
//                // like translation{key='killbonus.yagens_attributes.multishot_galvanized_tool_mod.1', args=[50]}
//                System.out.println("Test Homo: UniqueInfo: " + key);
//                Matcher m = p.matcher(key);
//                if (!m.find()) continue;
//                if (m.group(0).contains("bonus")) continue;
//                String attributeInfo = m.group(1);
//                for (ModSlot slot : weaponContainer.getActiveMods()) {
//                    List<MutableComponent> uniqueInfoCheck = slot.getMod().getUniqueInfo(level, player);
//                    for (var compCheck : uniqueInfoCheck) {
//                        Matcher mCheck = p.matcher(String.valueOf(compCheck));
//                        if (!mCheck.find()) continue;
//                        String attributeInfoCheck = mCheck.group(1);
//                        if (HomologyModGroup.instance().sameGroup(attributeInfo, attributeInfoCheck)) {
//                            player.sendSystemMessage(Component.translatable(
//                                    "ui.yagens_attributes.conflict", Component.translatable("mod.yagens_attributes." + slot.getMod().getModName())));
//                            return;
//                        }
//                    }
//                }
//            }
//        }


        var mutable = weaponContainer.mutableCopy();
        if (mutable.addModAtIndex(mod, level, selectedIndex)) {
            getModSlot().remove(1);
            IModContainer.set(weaponStack, mutable.toImmutable());
        }
        rebuildItemByMod(weaponStack);
    }

    private void rebuildItemByMod(ItemStack stack) {
        if (stack.isEmpty()) return;

        Pattern p = Pattern.compile(
                "'[^']*\\.([^.]+)_([^']+)'.*args=\\[([0-9.]+)]");

        Map<DamageType, Float> base = new HashMap<>();
        boolean isWeapon = false;
        if (ModCompat.validLocation(stack.getItem()) == 1) {
            base = new HashMap<>(DefaultDamageTypeRegistry.get(stack.getItem()));
            if (base.isEmpty()) {
                System.out.println("rebuild Item is Weapon but empty!!!");
                return;
            }
            isWeapon = true;
        }
        Map<Attribute, List<AttributeModifier>> map = new HashMap<>();
        //if (base.isEmpty()) return;

        Map<DamageType, Float> addFirstMap = new HashMap<>();
        Map<DamageType, Float> addLastMap = new HashMap<>();
        Map<DamageType, Float> additionMap = new HashMap<>();
        Map<DamageType, Float> multiplyMap = new HashMap<>();

        base.keySet().forEach(t -> multiplyMap.put(t, 1.0F));

        IModContainer container = IModContainer.get(stack);
        //IModContainer container = stack.get(ComponentRegistry.MOD_CONTAINER.get());
        List<DamageType> orderDamageType = new ArrayList<>();

        for (ModSlot slot : container.getActiveMods()) {
            AbstractMod mod = slot.getMod();
            int lvl = slot.getLevel();
            // riven info on the weapon
            List<MutableComponent> uniqueInfo;
            if (mod.getUniqueInfo(lvl, player).isEmpty()) {
                ComponentRegistry.RivenRawInfoList raw = stack.get(ComponentRegistry.RIVEN_RAW_INFO.get());
                uniqueInfo = raw == null ? List.of()
                        : raw.raw().stream()
                        .map(r -> Component.translatable(r.key(), r.base() * lvl))
                        .toList();
            } else {
                uniqueInfo = mod.getUniqueInfo(lvl, player);
            }
            for (var comp : uniqueInfo) {
                String key = String.valueOf(comp);  // like translation{key='tooltip.yagens_attributes.slash_addition', args=[50]}
                System.out.println("TestBuild: UniqueInfo: " + key);
                Matcher m = p.matcher(key);
                if (!m.find()) continue;
                // folk by tooltip(s)
                float val = Float.parseFloat(m.group(3)); // 50.0
                if (m.group(0).contains("tooltips")) // attributes
                {
                    String attributeName = YagensAttributes.MODID + ":" + m.group(1);
                    Attribute attribute = BuiltInRegistries.ATTRIBUTE
                            .get(ResourceLocation.tryParse(attributeName));
                    if (attribute == null) {
                        attribute = BuiltInRegistries.ATTRIBUTE
                                .get(ResourceLocation.tryParse("minecraft:generic." + m.group(1)));
                        if (attribute == null) {
                            attribute = BuiltInRegistries.ATTRIBUTE
                                    .get(ResourceLocation.tryParse("minecraft:player." + m.group(1)));
                            if (attribute == null) {
                                continue;
                            }
                        }
                    }

                    AttributeModifier.Operation op = null;
                    switch (m.group(2)) {
                        case "add" -> op = AttributeModifier.Operation.ADD_VALUE;
                        case "addneg" -> {
                            op = AttributeModifier.Operation.ADD_VALUE;
                            val = -val;
                        }
                        case "multibase" -> {
                            op = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                            val = val / 100;
                        }
                        case "multibaseneg" -> {
                            op = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                            val = -val / 100;
                        }
                        case "multiply" -> {
                            op = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                            val = val / 100 - 1;
                        }
                    }
                    ;
                    if (op == null) continue;

                    map.computeIfAbsent(attribute, k -> new ArrayList<>())
                            .add(new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(
                                            YagensAttributes.MODID,
                                            "mod_" + mod.getModName() + "_" + m.group(1) + "_" + m.group(2)),
                                    val, op));
                } else if (m.group(0).contains("tooltip")) // damage map
                {
                    if (!isWeapon) return;
                    //System.out.printf("%s, %s, %.1f%n", m.group(1), m.group(2), val);
                    DamageType type = DamageType.valueOf(m.group(1).toUpperCase(Locale.ROOT));
                    if (ELEMENTS.contains(type) && !orderDamageType.contains(type)) orderDamageType.add(type);

                    switch (m.group(2)) {
                        case "addition" -> additionMap.merge(type, val / 100F, Float::sum);
                        case "additionneg" -> additionMap.merge(type, -val / 100F, Float::sum);
                        case "multiply" -> {
                            float factor = val / 100.0F;
                            multiplyMap.merge(type, factor, (old, inc) -> old * inc); // 连乘
                        }
                        case "addlast" -> addLastMap.merge(type, val, Float::sum);
                        case "addlastneg" -> addLastMap.merge(type, -val, Float::sum);
                        case "addfirst" -> addFirstMap.merge(type, val, Float::sum);
                        case "addfirstneg" -> addFirstMap.merge(type, -val, Float::sum);
                    }
                }
            }
        }

        float baseTotalDamage;
        Map<DamageType, Float> result = new HashMap<>();

        if (isWeapon) {
            baseTotalDamage = base.values().stream().reduce(0f, Float::sum) + addFirstMap.values().stream().reduce(0f, Float::sum);

            base.forEach((type, dmg) -> {
                float addFirst = addFirstMap.getOrDefault(type, 0F);
                float addLast = addLastMap.getOrDefault(type, 0F);
                float addition = additionMap.getOrDefault(type, 0F);
                float multiply = multiplyMap.getOrDefault(type, 1F);

                //System.out.println("TestBuild: build Damage args: baseTotalDamage " + baseTotalDamage + " addLast " + addLast + " addition " + addition + " multiply " + multiply);
                float finalDmg = (dmg + addFirst + baseTotalDamage * addition) * multiply + addLast;
                if (finalDmg <= 0f && orderDamageType.contains(type)) orderDamageType.remove(type);
                result.put(type, Math.max(0F, finalDmg));
            });
            // combine damage type
            if (orderDamageType.size() >= 2) {
                // take order by orderDamageType, processed is an escape map
                Set<DamageType> processed = new HashSet<>();

                // deal with existed composites
                for (DamageType comp : COMPOSITES.keySet()) {
                    if (orderDamageType.contains(comp)) {
                        Set<DamageType> parts = COMPOSITES.get(comp);
                        float sum = 0f;
                        for (DamageType part : parts) {
                            sum += result.getOrDefault(part, 0f);
                            result.put(part, 0f);
                            processed.add(part);
                        }
                        result.merge(comp, sum, Float::sum);
                    }
                }

                ListIterator<DamageType> it = orderDamageType.listIterator();
                while (it.hasNext()) {
                    DamageType first = it.next();
                    if (processed.contains(first)) continue;

                    while (it.hasNext()) {
                        DamageType second = it.next();
                        if (processed.contains(second)) continue;

                        // get composites
                        DamageType comp = null;
                        for (var e : COMPOSITES.entrySet()) {
                            if (e.getValue().contains(first) && e.getValue().contains(second)) {
                                comp = e.getKey();
                                break;
                            }
                        }
                        if (comp != null) {
                            float sum = result.getOrDefault(first, 0f) + result.getOrDefault(second, 0f);
                            result.put(first, 0f);
                            result.put(second, 0f);
                            result.merge(comp, sum, Float::sum);
                            processed.add(first);
                            processed.add(second);
                        }
                        break; // every turn once
                    }
                }
            }

            container.setDamageProfile(result);
        } else {
            baseTotalDamage = 0;
        }
        container.setAttributeProfile(new ComponentRegistry.AttributeProfile(map));
        stack.set(
                ComponentRegistry.MOD_CONTAINER.get(),
                container
        );

        // get total damage
        float total = result.values().stream().reduce(0F, Float::sum);
        if (total <= 0F) total = 0F;

        // create builder
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        // get default attributes
        ItemAttributeModifiers defaultModifiers = stack.getItem().getDefaultAttributeModifiers(stack);
//        if (isWeapon) //fix bug of remove some added default attribute modifiers by other Modules
            defaultModifiers = stack.getOrDefault(ComponentRegistry.DEFAULT_ITEM_ATTRIBUTES.get(), new ComponentRegistry.DefaultItemAttributes(defaultModifiers)).modifiers();
        //System.out.println("DefaultItemAttributes: " + defaultModifiers);

        defaultModifiers.modifiers().forEach(e -> {
            if (e.attribute() != Attributes.ATTACK_DAMAGE)
                builder.add(e.attribute(), e.modifier(), e.slot());
        });

        switch (ModCompat.validLocation(stack.getItem())) {
            case 1, 0 -> {
                map.forEach((attr, list) ->
                        list.forEach(mod ->
                                builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr), mod, EquipmentSlotGroup.MAINHAND)));
            }
            case 2 -> {
                map.forEach((attr, list) ->
                        list.forEach(mod ->
                                builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr), mod, EquipmentSlotGroup.ARMOR)));
            }
            case 3 -> {
                map.forEach((attr, list) ->
                        list.forEach(mod ->
                                builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr), mod, EquipmentSlotGroup.OFFHAND)));
            }
        }
        if (stack.getItem() instanceof ProjectileWeaponItem) {
            total = 0;
        }
        if (isWeapon) {
            builder.add(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "mod_adjust"),
                            total,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            );
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
//        System.out.println("Final AttributeModifiers: " +
//                stack.get(DataComponents.ATTRIBUTE_MODIFIERS));
    }

    private static final Map<DamageType, Set<DamageType>> COMPOSITES = Map.of(
            DamageType.VIRAL, Set.of(DamageType.COLD, DamageType.TOXIN),
            DamageType.GAS, Set.of(DamageType.HEAT, DamageType.TOXIN),
            DamageType.CORROSIVE, Set.of(DamageType.ELECTRICITY, DamageType.TOXIN),
            DamageType.RADIATION, Set.of(DamageType.HEAT, DamageType.ELECTRICITY),
            DamageType.BLAST, Set.of(DamageType.HEAT, DamageType.COLD),
            DamageType.MAGNETIC, Set.of(DamageType.COLD, DamageType.ELECTRICITY)
    );

    private static final Set<DamageType> ELEMENTS =
            Set.of(DamageType.COLD, DamageType.HEAT,
                    DamageType.ELECTRICITY, DamageType.TOXIN,
                    DamageType.VIRAL, DamageType.GAS,
                    DamageType.CORROSIVE, DamageType.RADIATION,
                    DamageType.MAGNETIC, DamageType.BLAST);


    public void doUpgrade(ServerPlayer pPlayer, int selectedIndex) {
        // don't check the capacity cost for more possibilities
        ItemStack itemStack = getItemSlot().getItem();
        if (!IModContainer.isModContainer(itemStack)) return;

        IModContainer container = IModContainer.get(itemStack);
        if (selectedIndex < 0 || selectedIndex >= container.getMaxModCount()) return;

        @NotNull ModData slot = container.getModAtIndex(selectedIndex);
        AbstractMod mod = slot.getMod();
        int currentLevel = slot.getLevel();

        if (currentLevel >= mod.getMaxLevel()) return;
        int need = getModEssenceCost(slot);
        if (need <= 0) return;

        boolean isCreative = pPlayer.isCreative();

        int has = getPlayerModEssenceCount(pPlayer);
        if (!isCreative) {
            if (has < need) return;
            if (!consumeModEssence(pPlayer, need)) return;
        }

        IModContainerMutable mutable = container.mutableCopy();
        mutable.removeModAtIndex(selectedIndex);
        mutable.addModAtIndex(mod, currentLevel + 1, selectedIndex);
        IModContainer.set(itemStack, mutable.toImmutable());

        rebuildItemByMod(itemStack);
    }

    public void doPolarity(ServerPlayer pPlayer, int selectedIndex) {
        ItemStack itemStack = getItemSlot().getItem();
        if (!IModContainer.isModContainer(itemStack)) return;
        ComponentRegistry.UpgradeData data = ComponentRegistry.getUpgrade(itemStack);
//        if (data.level() < 30 && !pPlayer.isCreative()) return;

        ItemStack forma = getModSlot().getItem();
        String formaType = forma.getOrDefault(ComponentRegistry.FORMA_TYPE, "");
        if (formaType.isEmpty()) return;

        IModContainer container = IModContainer.get(itemStack);
        int slotCount = container.getMaxModCount();

        List<String> polarities = new ArrayList<>(ComponentRegistry.getPolarities(itemStack));
        while (polarities.size() < slotCount) {
            polarities.add("");
        }
        polarities.set(selectedIndex, formaType);
        ComponentRegistry.setPolarities(itemStack, polarities);
        ComponentRegistry.setUpgrade(itemStack, data.withPolarity(1));
        getModSlot().remove(1);
    }

    public void doCycle(ServerPlayer pPlayer) {
        int need = 4;

        boolean isCreative = pPlayer.isCreative();

        int has = getPlayerKuvaCount(pPlayer);
        if (!isCreative) {
            if (has < need) return;
            if (!consumeKuva(pPlayer, need)) return;
        }

        int bonus, penalty;
        switch (RandomSource.create().nextInt(4)) {
            case 0 -> {
                bonus = 2;
                penalty = 0;
            }
            case 1 -> {
                bonus = 2;
                penalty = 1;
            }
            case 2 -> {
                bonus = 3;
                penalty = 0;
            }
            default -> {
                bonus = 3;
                penalty = 1;
            }
        }
        ItemStack rivenStack = getModSlot().getItem();
        var rivenContainer = IModContainer.get(rivenStack).getModAtIndex(0);
        Item rivenType = rivenStack.get(ComponentRegistry.RIVEN_TYPE.get());
        String polarity = rivenStack.getOrDefault(ComponentRegistry.RIVEN_POLARITY_TYPE.get(), "Riven");
        ItemStack modStack = createRandomModItem(bonus, penalty, rivenContainer.getLevel(), polarity, rivenType);
        getModSlot().set(modStack);
    }

    private int getModEssenceCost(@NotNull ModData modSlot) {
        var modRarity = modSlot.getMod().getRarity();
        var modLevel = modSlot.getLevel();
        if (modLevel == modSlot.getMod().getMaxLevel()) return 0;
        return (int) ((modRarity.getValue() + 1) * Math.pow(2, modLevel - 1));
    }

    private int getPlayerModEssenceCount(ServerPlayer player) {
        int count = 0;
        for (ItemStack s : player.getInventory().items) {
            if (s.is(ItemRegistry.MOD_ESSENCE.get())) count += s.getCount();
        }
        return count;
    }

    private int getPlayerKuvaCount(ServerPlayer player) {
        int count = 0;
        for (ItemStack s : player.getInventory().items) {
            if (s.is(ItemRegistry.KUVA.get())) count += s.getCount();
        }
        return count;
    }

    private boolean consumeModEssence(ServerPlayer player, int amount) {
        int remain = amount;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && remain > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.is(ItemRegistry.MOD_ESSENCE.get())) continue;

            int take = Math.min(stack.getCount(), remain);
            stack.shrink(take);
            remain -= take;
            if (stack.isEmpty()) inv.setItem(i, ItemStack.EMPTY);
        }
        return remain == 0;
    }

    private boolean consumeKuva(ServerPlayer player, int amount) {
        int remain = amount;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && remain > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.is(ItemRegistry.KUVA.get())) continue;

            int take = Math.min(stack.getCount(), remain);
            stack.shrink(take);
            remain -= take;
            if (stack.isEmpty()) inv.setItem(i, ItemStack.EMPTY);
        }
        return remain == 0;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        //Called whenever the client clicks on a button. The ID passed in is the mod slot index or -1. If it is positive, it is to select that slot. If it is negative, it is to inscribe
        if (pId == -1) {
            var modStack = getModSlot().getItem();
            if (selectedModIndex >= 0 && modStack.getItem() instanceof Mod mod) {
                ModData modData = IModContainer.get(modStack).getModAtIndex(0);
                if (NeoForge.EVENT_BUS.post(new OperationModEvent(pPlayer, modData)).isCanceled())
                    return false;
                doOperation(selectedModIndex);
            }
        } else if (pId == -2) {
            doUpgrade((ServerPlayer) pPlayer, selectedModIndex);
        } else if (pId == -3) {
            doPolarity((ServerPlayer) pPlayer, selectedModIndex);
        } else if (pId == -4) {
            doCycle((ServerPlayer) pPlayer);
        } else {
            setSelectedMod(pId);
        }
        return true;
    }

    private void setupResultSlot() {
        ItemStack resultStack = ItemStack.EMPTY;
        ItemStack itemStack = itemSlot.getItem();

        if (IModContainer.isModContainer(itemStack)) {
            var modList = IModContainer.get(itemStack);
            if (selectedModIndex >= 0) {
                var modData = modList.getModAtIndex(selectedModIndex);
                if (modData != ModData.EMPTY && modData.canRemove()) {
                    resultStack = new ItemStack(ItemRegistry.MOD.get());
                    resultStack.setCount(1);
                    IModContainer.createModContainer(modData.getMod(), modData.getLevel(), resultStack);

                    if (modData.getMod().getUniqueInfo(modData.getLevel(), player).isEmpty()) {
                        copyRivenRawInfo(itemStack, resultStack);
                    }
                }
            }
            // update result slot
            if (!ItemStack.matches(resultStack, this.resultSlot.getItem())) {
                this.resultSlot.set(resultStack);
            }
        } else {
            if (!ItemStack.matches(resultStack, this.resultSlot.getItem())) {
                this.resultSlot.set(resultStack);
            }
        }
    }

    public Slot getItemSlot() {
        return itemSlot;
    }


    public Slot getModSlot() {
        return modSlot;
    }

    public Slot getResultSlot() {
        return resultSlot;
    }

    private static void copyRivenRawInfo(ItemStack from, ItemStack to) {
        ComponentRegistry.RivenRawInfoList data = from.get(ComponentRegistry.RIVEN_RAW_INFO.get());
        String rivenPolarity = from.get(ComponentRegistry.RIVEN_POLARITY_TYPE.get());
        Item rivenType = from.get(ComponentRegistry.RIVEN_TYPE.get());
        if (data != null) {
            to.set(ComponentRegistry.RIVEN_RAW_INFO.get(), data);
            to.set(ComponentRegistry.RIVEN_POLARITY_TYPE.get(), rivenPolarity);
            to.set(ComponentRegistry.RIVEN_TYPE.get(), rivenType);
        }
    }

    private static void clearRivenRawInfo(ItemStack weapon) {
        weapon.remove(ComponentRegistry.RIVEN_RAW_INFO.get());
        weapon.remove(ComponentRegistry.RIVEN_POLARITY_TYPE.get());
        weapon.remove(ComponentRegistry.RIVEN_TYPE.get());
    }

    private static boolean hasRivenModData(ItemStack weapon) {
        return weapon.has(ComponentRegistry.RIVEN_RAW_INFO.get()) && weapon.has(ComponentRegistry.RIVEN_POLARITY_TYPE.get()) && weapon.has(ComponentRegistry.RIVEN_TYPE.get());
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    private static final int TE_INVENTORY_SLOT_COUNT = 3;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.access.evaluate((level, blockPos) -> {
            return level.getBlockState(blockPos).is(BlockRegistry.MOD_OPERATION_BLOCK.get()) && pPlayer.distanceToSqr((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D) <= 64.0D;
        }, true);
    }

    @Override
    public void removed(Player pPlayer) {
        if (pPlayer instanceof ServerPlayer) {
            super.removed(pPlayer);
            this.access.execute((p_39796_, p_39797_) -> {
                this.clearContainer(pPlayer, this.modContainer);
                this.clearContainer(pPlayer, this.itemContainer);
            });
        }
    }
}
