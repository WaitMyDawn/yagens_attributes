package yagen.waitmydawn.yagens_attributes.data;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import java.util.Objects;

public final class ExperienceContracts {
    private static int checks;
    private ExperienceContracts() {}
    public static void run() throws Exception {
        var anvil = read("net/minecraft/world/inventory/AnvilMenu");
        check(fields(anvil, "createResult", "net/minecraft/world/entity/player/Abilities", "instabuild") == 2);
        check(calls(anvil, "onTake", "net/minecraft/world/entity/player/Player", "giveExperienceLevels", "(I)V") == 1);
        var enchanting = read("net/minecraft/world/inventory/EnchantmentMenu");
        var lambda = enchanting.methods.stream().filter(method -> method.name.equals("lambda$clickMenuButton$1")).findFirst().orElseThrow();
        check(lambda.desc.equals("(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/entity/player/Player;ILnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"));
        check(calls(enchanting, lambda.name, "net/minecraft/world/entity/player/Player", "onEnchantmentPerformed", "(Lnet/minecraft/world/item/ItemStack;I)V") == 1);
        var screen = read("net/minecraft/client/gui/screens/inventory/AnvilScreen");
        check(fields(screen, "renderLabels", "net/minecraft/world/entity/player/Abilities", "instabuild") == 1);
        check(calls(screen, "renderLabels", "net/minecraft/network/chat/Component", "translatable", "(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;") == 1);
        var enchantScreen = read("net/minecraft/client/gui/screens/inventory/EnchantmentScreen");
        check(calls(enchantScreen, "render", "net/minecraft/network/chat/Component", "translatable", "(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;") > 0);
        check(calls(enchantScreen, "render", "net/minecraft/network/chat/Component", "translatable", "(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;") > 0);
        System.out.println("Passed " + checks + " native menu and client XP injection-target checks");
    }
    private static ClassNode read(String name) throws Exception {
        try (var stream = ExperienceContracts.class.getResourceAsStream("/" + name + ".class")) {
            var node = new ClassNode(); new ClassReader(Objects.requireNonNull(stream)).accept(node, 0); return node;
        }
    }
    private static int fields(ClassNode owner, String method, String fieldOwner, String name) {
        int count = 0;
        for (var target : owner.methods) if (target.name.equals(method)) for (var instruction : target.instructions)
            if (instruction instanceof FieldInsnNode field && field.owner.equals(fieldOwner) && field.name.equals(name)) count++;
        return count;
    }
    private static int calls(ClassNode owner, String method, String callOwner, String name, String descriptor) {
        int count = 0;
        for (var target : owner.methods) if (target.name.equals(method)) for (var instruction : target.instructions)
            if (instruction instanceof MethodInsnNode call && call.owner.equals(callOwner) && call.name.equals(name) && call.desc.equals(descriptor)) count++;
        return count;
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("XP injection target changed"); }
}
