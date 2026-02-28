package yagen.waitmydawn.gui.mod_operation;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.DamageType;
import yagen.waitmydawn.api.attribute.DefaultDamageTypeRegistry;
import yagen.waitmydawn.api.events.OperationModEvent;
import yagen.waitmydawn.api.mods.*;
import yagen.waitmydawn.api.util.ModCompat;
import yagen.waitmydawn.capabilities.ModPoolData;
import yagen.waitmydawn.item.Mod;
import yagen.waitmydawn.item.mod.armor_mod.GraceArmorMod;
import yagen.waitmydawn.registries.*;
import yagen.waitmydawn.util.HomologyModGroup;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static yagen.waitmydawn.api.attribute.DefaultDamageTypeRegistry.getBaseAttackDamage;
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

    // Mod Pool
    private final ModPoolData modPoolData;
    private final PageProxyHandler proxyHandler;
    private static final ItemStackHandler EMPTY_HANDLER = new ItemStackHandler(27);

    // Sync Integers
    private final DataSlot pageSlot = DataSlot.standalone();
    private final DataSlot totalPagesSlot = DataSlot.standalone();
    // 0 = Player Inventory, 1 = Mod Pool
    private final DataSlot modeSlot = new DataSlot() {
        private int value = 0;

        @Override
        public int get() {
            return value;
        }

        @Override
        public void set(int pValue) {
            this.value = pValue;
            updateSlotActiveStates();
        }
    };

    private final List<ToggleableSlot> playerInvSlots = new ArrayList<>();
    private final List<ToggleableSlotItemHandler> modPoolSlots = new ArrayList<>();

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

        // Mod Pool
        this.modPoolData = inv.player.getData(DataAttachmentRegistry.MOD_POOL);
        this.proxyHandler = new PageProxyHandler();

        this.addDataSlot(pageSlot);       // ID: 0
        this.addDataSlot(totalPagesSlot); // ID: 1
        this.addDataSlot(modeSlot);       // ID: 2

        // put in item
        itemSlot = new Slot(itemContainer, 0, 80, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ModCompat.isWeaponToolOrArmor(stack);
            }

            @Override
            public void set(ItemStack stack) {
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
                AbstractMod mod = mutable.getModAtIndex(selectedModIndex).getMod();
                if (mod.getUniqueInfo(1).isEmpty()) {
                    clearRivenRawInfo(itemStack);
                } else if (mod.getModName().equals("grace_armor_mod")) {
                    clearGraceAbility(itemStack);
                } else if (mod.getModName().equals("reservoirs_armor_mod")) {
                    clearReservoirsData(itemStack);
                    clearReservoirsAttributes(itemStack);
                }
                mutable.removeModAtIndex(selectedModIndex);
                IModContainer.set(itemStack, mutable.toImmutable());

                super.onTake(player, itemStack);
                rebuildItemByMod(itemStack);
            }
        };

        // --- Operation Slots (0, 1, 2) ---
        this.addSlot(itemSlot);
        this.addSlot(modSlot);
        this.addSlot(resultSlot);

        // --- Player Inventory Slots (3 rows x 9 columns) ---
        // Indices 3 to 29
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                ToggleableSlot slot = new ToggleableSlot(inv, l + i * 9 + 9, 8 + l * 18, 84 + i * 18);
                this.addSlot(slot);
                this.playerInvSlots.add(slot);
            }
        }

        // --- Mod Pool Slots (3 rows x 9 columns) ---
        // Indices 30 to 56
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                ToggleableSlotItemHandler slot = new ToggleableSlotItemHandler(proxyHandler, l + i * 9, 8 + l * 18, 84 + i * 18);
                this.addSlot(slot);
                this.modPoolSlots.add(slot);
            }
        }

        // --- Player Hotbar (Always visible) ---
        // Indices 57 to 65
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }

        // Initial setup
        refreshProxyTarget();
        updateSlotActiveStates();
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

    // --- Mod Pool Logic ---

    public boolean isModPoolMode() {
        return modeSlot.get() == 1;
    }

    public void toggleModPoolMode() {
        int current = modeSlot.get();
        modeSlot.set(current == 0 ? 1 : 0);

        if (!player.level().isClientSide) {
            modPoolData.cleanEmptyPages();
            refreshProxyTarget();
        }
        // Slot states updated by DataSlot.set() hook
    }

    private void updateSlotActiveStates() {
        boolean showPool = isModPoolMode();

        // Use setActive instead of moving coordinates
        for (ToggleableSlot slot : playerInvSlots) {
            slot.setActive(!showPool);
        }
        for (ToggleableSlotItemHandler slot : modPoolSlots) {
            slot.setActive(showPool);
        }
    }

    public void changePage(int offset) {
        if (!isModPoolMode()) return;
        int max = modPoolData.getPageCount();
        int current = pageSlot.get();
        int newPage = current + offset;

        if (newPage >= 0 && newPage < max) {
            pageSlot.set(newPage);
            refreshProxyTarget();
            this.broadcastChanges();
        }
    }

    public void jumpToPage(int pageIndex) {
        if (!isModPoolMode()) return;
        int max = modPoolData.getPageCount();
        int actual = Math.max(0, Math.min(pageIndex, max - 1));

        pageSlot.set(actual);
        refreshProxyTarget();
        this.broadcastChanges();
    }

    public void refreshProxyTarget() {
        if (modPoolData.getPages().isEmpty()) {
            modPoolData.ensurePage(0);
        }

        int max = modPoolData.getPageCount();
        totalPagesSlot.set(max);

        int currentPage = pageSlot.get();
        if (currentPage >= max && max > 0) {
            currentPage = max - 1;
            pageSlot.set(currentPage);
        }
        proxyHandler.setTarget(modPoolData.getPage(currentPage));
    }

    public int getCurrentPage() {
        return pageSlot.get();
    }

    public int getTotalPages() {
        return totalPagesSlot.get();
    }

    public void setSelectedMod(int index) {
        selectedModIndex = index;
        setupResultSlot();
    }

    public void doOperation(int selectedIndex) {
        ItemStack itemStack = getItemSlot().getItem();
        ItemStack modStack = getModSlot().getItem();

        if (!IModContainer.isModContainer(itemStack) || !(modStack.getItem() instanceof Mod)) {
            return;
        }

        var weaponContainer = IModContainer.get(itemStack);
        var modContainer = IModContainer.get(modStack);
        var modData = modContainer.getModAtIndex(0);
        AbstractMod mod = modData.getMod();
        int level = modData.getLevel();

        // only RivenMod by empty getUniqueInfo
        if (mod.getUniqueInfo(level).isEmpty()) {
            Item item = modStack.get(ComponentRegistry.RIVEN_TYPE.get());
            if (item != null && item != itemStack.getItem()) {
                return;
            }
            if (hasRivenModData(itemStack)) {
                player.sendSystemMessage(Component.translatable(
                        "ui.yagens_attributes.conflict_riven"));
                return;
            }
            copyRivenRawInfo(modStack, itemStack);
        } else if (mod.getModName().equals("grace_armor_mod")) {
            copyGraceAbility(modStack, itemStack);
        } else if (mod.getModName().equals("reservoirs_armor_mod")) {
            copyReservoirsData(modStack, itemStack);
            copyReservoirsAttributes(modStack, itemStack);
        }
        // check homology mod
        else {
            String groupName = mod.getModName().split("_")[0];
            for (ModSlot slot : weaponContainer.getActiveMods()) {
                String groupNameCheck = slot.getMod().getModName().split("_")[0];
                if (HomologyModGroup.instance().sameGroup(groupName, groupNameCheck)) {
                    player.sendSystemMessage(Component.translatable(
                            "ui.yagens_attributes.conflict", Component.translatable("mod.yagens_attributes." + slot.getMod().getModName())));
                    return;
                }
            }
        }

        var mutable = weaponContainer.mutableCopy();
        if (mutable.addModAtIndex(mod, level, selectedIndex)) {
            getModSlot().remove(1);
            IModContainer.set(itemStack, mutable.toImmutable());
        }
        rebuildItemByMod(itemStack);
    }

    public static void rebuildItemByMod(ItemStack stack) {
        if (stack.isEmpty()) return;

        Pattern p = Pattern.compile(
                "'[^']*\\.([^.]+)_([^']+)'.*args=\\[([0-9.]+)]");

        Map<DamageType, Float> base = new HashMap<>();
        boolean isWeapon = false;
        if (ModCompat.validLocation(stack.getItem()) == 1) {
            base = DefaultDamageTypeRegistry.get(stack.getItem());
            if (base == null) return;
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
        float damageMultiplier = 1.0f;

        base.keySet().forEach(t -> multiplyMap.put(t, 1.0F));

        IModContainer container = IModContainer.get(stack);
        //IModContainer container = stack.get(ComponentRegistry.MOD_CONTAINER.get());
        List<DamageType> orderDamageType = new ArrayList<>();

        for (ModSlot slot : container.getActiveMods()) {
            AbstractMod mod = slot.getMod();
            int lvl = slot.getLevel();
            // riven info on the weapon
            List<MutableComponent> uniqueInfo;
            if (mod.getUniqueInfo(lvl).isEmpty()) {
                ComponentRegistry.RivenRawInfoList raw = stack.get(ComponentRegistry.RIVEN_RAW_INFO.get());
                uniqueInfo = raw == null ? List.of()
                        : raw.raw().stream()
                        .map(r -> Component.translatable(r.key(), r.base() * lvl))
                        .toList();
            } else {
                uniqueInfo = mod.getUniqueInfo(lvl);
            }
            for (var comp : uniqueInfo) {
                String key = String.valueOf(comp);  // like translation{key='tooltip.yagens_attributes.slash_addition', args=[50]}
                Matcher m = p.matcher(key);
                if (!m.find()) continue;
                // folk by tooltip(s)
                float val = Float.parseFloat(m.group(3)); // 50.0
                if (m.group(0).contains("tooltips")) // attributes
                {
                    Attribute attribute = getModAttribute(getModIdFromRawKey(m.group(0)), m.group(1));
                    if (attribute == null) {
                        continue;
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
                    if (m.group(1).contains("damage")) {
                        if (m.group(2).equals("multiply"))
                            damageMultiplier = damageMultiplier + val / 100.0F;
                        else if (m.group(2).equals("multiplyneg"))
                            damageMultiplier = damageMultiplier - val / 100.0F;
                    } else {
                        DamageType type = DamageType.valueOf(m.group(1).toUpperCase(Locale.ROOT));
                        if (ELEMENTS.contains(type) && !orderDamageType.contains(type)) orderDamageType.add(type);

                        switch (m.group(2)) {
                            case "addition" -> additionMap.merge(type, val / 100F, Float::sum);
                            case "additionneg" -> additionMap.merge(type, -val / 100F, Float::sum);
                            case "multiply" -> {
                                float factor = val / 100.0F;
                                multiplyMap.merge(type, factor, (old, inc) -> old * inc);
                            }
                            case "addlast" -> addLastMap.merge(type, val, Float::sum);
                            case "addlastneg" -> addLastMap.merge(type, -val, Float::sum);
                            case "addfirst" -> addFirstMap.merge(type, val, Float::sum);
                            case "addfirstneg" -> addFirstMap.merge(type, -val, Float::sum);
                        }
                    }
                }
            }
        }

        float baseTotalDamage;
        Map<DamageType, Float> result = new HashMap<>();

        if (isWeapon) {
            baseTotalDamage = base.values().stream().reduce(0f, Float::sum) + addFirstMap.values().stream().reduce(0f, Float::sum);

            float damageMultiplierCopy = damageMultiplier;
            base.forEach((type, dmg) -> {
                float addFirst = addFirstMap.getOrDefault(type, 0F);
                float addLast = addLastMap.getOrDefault(type, 0F);
                float addition = additionMap.getOrDefault(type, 0F);
                float multiply = multiplyMap.getOrDefault(type, 1F);

                float finalDmg = (dmg + addFirst + baseTotalDamage * addition) * damageMultiplierCopy * multiply + addLast;
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
        defaultModifiers = stack.getOrDefault(ComponentRegistry.DEFAULT_ITEM_ATTRIBUTES.get(), new ComponentRegistry.DefaultItemAttributes(defaultModifiers)).modifiers();

        defaultModifiers.modifiers().forEach(e -> {
            builder.add(e.attribute(), e.modifier(), e.slot());
        });
        float baseAttackDamage = getBaseAttackDamage(stack.getItem());
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
        if (stack.getItem() instanceof ProjectileWeaponItem || ModCompat.isCancelAttackDamage(stack.getItem())) {
            total = 0;
        }
        if (isWeapon && (total - baseAttackDamage != 0) && total != 0) {
            builder.add(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "mod_adjust"),
                            total - baseAttackDamage,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            );
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
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
        modStack.set(ComponentRegistry.RIVEN_CYCLE_COUNT.get(),
                rivenStack.getOrDefault(ComponentRegistry.RIVEN_CYCLE_COUNT.get(), 0) + 1);
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

    public static String getModIdFromRawKey(String key) {
        String[] parts = key.split("'")[1].split("\\.");
        return parts[parts.length - 2];
    }

    public static String getModIdFromKey(String key) {
        String[] parts = key.split("\\.");
        if (parts.length < 2) return null;
        return parts[parts.length - 2];
    }

    public static String getOperationFromKey(String key) {
        return key.substring(key.lastIndexOf('_') + 1);
    }

    public static String getModAttributeFromKey(String key) {
        return key.substring(key.lastIndexOf('.') + 1, key.lastIndexOf('_'));
    }

    public static Attribute getModAttribute(String modId, String pathName) {
        if (modId.equals(YagensAttributes.MODID)) {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE
                    .get(ResourceLocation.tryParse(YagensAttributes.MODID + ":" + pathName));
            if (attribute != null) return attribute;

            attribute = BuiltInRegistries.ATTRIBUTE
                    .get(ResourceLocation.tryParse("minecraft:generic." + pathName));
            if (attribute != null) return attribute;

            attribute = BuiltInRegistries.ATTRIBUTE
                    .get(ResourceLocation.tryParse("minecraft:player." + pathName));
            if (attribute != null) return attribute;
        }
        return BuiltInRegistries.ATTRIBUTE
                .get(ResourceLocation.tryParse(modId + ":" + pathName));
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

                    if (modData.getMod().getUniqueInfo(modData.getLevel()).isEmpty()) {
                        copyRivenRawInfo(itemStack, resultStack);
                    } else if (modData.getMod().getModName().equals("grace_armor_mod")) {
                        copyGraceAbility(itemStack, resultStack);
                    } else if (modData.getMod().getModName().equals("reservoirs_armor_mod")) {
                        retrieveReservoirsData(itemStack, resultStack);
                        copyReservoirsAttributes(itemStack, resultStack);
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

    public static final String NBT_KEY_ON_CHEST = "ReservoirInventory";
    private static final String NBT_KEY_ON_MOD = "Inventory";

    public static void copyReservoirsData(ItemStack modStack, ItemStack chest) {
        CustomData modData = modStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag modTag = modData.copyTag();

        if (modTag.contains(NBT_KEY_ON_MOD)) {
            CompoundTag inventoryTag = modTag.getCompound(NBT_KEY_ON_MOD);

            CustomData.update(DataComponents.CUSTOM_DATA, chest, currentTag -> {
                currentTag.put(NBT_KEY_ON_CHEST, inventoryTag);
            });
        } else {
            CustomData.update(DataComponents.CUSTOM_DATA, chest, currentTag -> {
                currentTag.remove(NBT_KEY_ON_CHEST);
            });
        }
    }

    public static void retrieveReservoirsData(ItemStack chest, ItemStack modStack) {
        CustomData chestData = chest.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag chestTag = chestData.copyTag();

        if (chestTag.contains(NBT_KEY_ON_CHEST)) {
            CompoundTag inventoryTag = chestTag.getCompound(NBT_KEY_ON_CHEST);

            CustomData.update(DataComponents.CUSTOM_DATA, modStack, currentTag -> {
                currentTag.put(NBT_KEY_ON_MOD, inventoryTag);
            });

//            CustomData.update(DataComponents.CUSTOM_DATA, chest, currentTag -> {
//                currentTag.remove(NBT_KEY_ON_CHEST);
//            });
        }
    }

    public static void clearReservoirsData(ItemStack chest) {
        CustomData chestData = chest.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag chestTag = chestData.copyTag();
        if (chestTag.contains(NBT_KEY_ON_CHEST)) {
            CustomData.update(DataComponents.CUSTOM_DATA, chest, currentTag -> {
                currentTag.remove(NBT_KEY_ON_CHEST);
            });
        }
    }

    private static void copyGraceAbility(ItemStack from, ItemStack to) {
        Attribute attribute = GraceArmorMod.getGraceAbility(from);
        if (attribute != Attributes.MOVEMENT_SPEED.value())
            GraceArmorMod.setGraceAbility(attribute, to);
    }


    private static void clearGraceAbility(ItemStack itemStack) {
        itemStack.remove(ComponentRegistry.GRACE_ABILITY.get());
    }

    private static void copyReservoirsAttributes(ItemStack from, ItemStack to) {
        ComponentRegistry.ReservoirsAttributes reservoirsAttributes = from.get(
                ComponentRegistry.RESERVOIRS_ATTRIBUTES.get()
        );
        if (reservoirsAttributes != null)
            to.set(ComponentRegistry.RESERVOIRS_ATTRIBUTES.get(), reservoirsAttributes);
    }

    private static void clearReservoirsAttributes(ItemStack itemStack) {
        itemStack.remove(ComponentRegistry.RESERVOIRS_ATTRIBUTES.get());
    }

    private static void copyRivenRawInfo(ItemStack from, ItemStack to) {
        ComponentRegistry.RivenRawInfoList data = from.get(ComponentRegistry.RIVEN_RAW_INFO.get());
        String rivenPolarity = from.get(ComponentRegistry.RIVEN_POLARITY_TYPE.get());
        Item rivenType = from.get(ComponentRegistry.RIVEN_TYPE.get());
        int cycleCount = from.getOrDefault(ComponentRegistry.RIVEN_CYCLE_COUNT.get(), 0);
        if (data != null) {
            to.set(ComponentRegistry.RIVEN_RAW_INFO.get(), data);
            to.set(ComponentRegistry.RIVEN_POLARITY_TYPE.get(), rivenPolarity);
            to.set(ComponentRegistry.RIVEN_TYPE.get(), rivenType);
            to.set(ComponentRegistry.RIVEN_CYCLE_COUNT.get(), cycleCount);
        }
    }

    private static void clearRivenRawInfo(ItemStack weapon) {
        weapon.remove(ComponentRegistry.RIVEN_RAW_INFO.get());
        weapon.remove(ComponentRegistry.RIVEN_POLARITY_TYPE.get());
        weapon.remove(ComponentRegistry.RIVEN_TYPE.get());
        weapon.remove(ComponentRegistry.RIVEN_CYCLE_COUNT.get());
    }

    private static boolean hasRivenModData(ItemStack weapon) {
        return weapon.has(ComponentRegistry.RIVEN_RAW_INFO.get())
                && weapon.has(ComponentRegistry.RIVEN_POLARITY_TYPE.get())
                && weapon.has(ComponentRegistry.RIVEN_TYPE.get());
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
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Constants defining slot ranges
        int OPERATION_END = 3;
        int PLAYER_MAIN_START = 3;
        int PLAYER_MAIN_END = 30; // 27 slots
        int MOD_POOL_START = 30;
        int MOD_POOL_END = 57;    // 27 slots
        int HOTBAR_START = 57;
        int HOTBAR_END = 66;      // 9 slots

        boolean isPoolMode = isModPoolMode();

        // 1. From Operation Slots (0-2) -> To Storage
        if (index < OPERATION_END) {
            // Try Hotbar first
            if (isPoolMode) {
                // If in Pool Mode, try Mod Pool and Hotbar
                if (!moveItemStackTo(sourceStack, MOD_POOL_START, MOD_POOL_END, false))
                    if (!moveItemStackTo(sourceStack, HOTBAR_START, HOTBAR_END, false))
                        return ItemStack.EMPTY;
            } else {
                // If in Inv Mode, try Hotbar and Main Inventory
                if (!moveItemStackTo(sourceStack, HOTBAR_START, HOTBAR_END, false))
                    if (!moveItemStackTo(sourceStack, PLAYER_MAIN_START, PLAYER_MAIN_END, false))
                        return ItemStack.EMPTY;
            }
        }
        // 2. From Storage -> Operation or Other Storage
        else {
            // Try to move to Operation Slots first (if valid)
            if (ModCompat.isWeaponToolOrArmor(sourceStack)) {
                if (!moveItemStackTo(sourceStack, 0, 1, false)) return ItemStack.EMPTY;
            } else if (sourceStack.is(ItemRegistry.MOD.get()) || sourceStack.is(ItemRegistry.FORMA.get())) {
                if (!moveItemStackTo(sourceStack, 1, 2, false)) return ItemStack.EMPTY;
            }

            // Transfer between storages
            if (isPoolMode) {
                // In Pool Mode: Transfer between Mod Pool and Hotbar
                if (index >= MOD_POOL_START && index < MOD_POOL_END) {
                    // Pool -> Hotbar
                    if (!moveItemStackTo(sourceStack, HOTBAR_START, HOTBAR_END, false))
                        return ItemStack.EMPTY;
                } else if (index >= HOTBAR_START && index < HOTBAR_END) {
                    // Hotbar -> Pool
                    if (!moveItemStackTo(sourceStack, MOD_POOL_START, MOD_POOL_END, false))
                        return ItemStack.EMPTY;
                }
            } else {
                // In Inv Mode: Transfer between Player Inv and Hotbar
                if (index >= PLAYER_MAIN_START && index < PLAYER_MAIN_END) {
                    // Player -> Hotbar
                    if (!moveItemStackTo(sourceStack, HOTBAR_START, HOTBAR_END, false)) return ItemStack.EMPTY;
                } else if (index >= HOTBAR_START && index < HOTBAR_END) {
                    // Hotbar -> Player
                    if (!moveItemStackTo(sourceStack, PLAYER_MAIN_START, PLAYER_MAIN_END, false))
                        return ItemStack.EMPTY;
                }
            }
        }

        if (sourceStack.getCount() == 0) sourceSlot.set(ItemStack.EMPTY);
        else sourceSlot.setChanged();

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
            if (!pPlayer.level().isClientSide) {
                modPoolData.cleanEmptyPages();
            }
            this.access.execute((p_39796_, p_39797_) -> {
                this.clearContainer(pPlayer, this.modContainer);
                this.clearContainer(pPlayer, this.itemContainer);
            });
        }
    }

    // --- Custom Slot Classes for "isActive" Control ---
    public static class ToggleableSlot extends Slot {
        private boolean active = true;

        public ToggleableSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public boolean isActive() {
            return active;
        }
    }

    public static class ToggleableSlotItemHandler extends SlotItemHandler {
        private boolean active = true;

        public ToggleableSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ItemRegistry.MOD.get());
        }
    }

    // Proxy Handler
    private static class PageProxyHandler implements IItemHandlerModifiable {
        private IItemHandlerModifiable target = EMPTY_HANDLER;

        public void setTarget(IItemHandlerModifiable target) {
            this.target = target;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            target.setStackInSlot(slot, stack);
        }

        @Override
        public int getSlots() {
            return target.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return target.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return target.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return target.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return target.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return target.isItemValid(slot, stack);
        }
    }
}