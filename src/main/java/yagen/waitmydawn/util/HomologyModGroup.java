package yagen.waitmydawn.util;

import java.util.*;

public class HomologyModGroup {
    private static volatile HomologyModGroup INSTANCE;

    private final Map<String, Set<String>> memberToGroup;

    public HomologyModGroup(List<Set<String>> groups) {
        memberToGroup = new HashMap<>();
        for (Set<String> group : groups) {
            for (String member : group) {
                memberToGroup.computeIfAbsent(member, k -> new HashSet<>())
                        .addAll(group);
            }
        }
    }

    public static HomologyModGroup instance() {
        if (INSTANCE == null) {
            synchronized (HomologyModGroup.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HomologyModGroup(buildDefault());
                }
            }
        }
        return INSTANCE;
    }

    private static List<Set<String>> buildDefault() {
        return List.of(
                Set.of("puncture", "slash", "impact"),
                Set.of("multishot")
        );
    }

    public Set<String> getGroup(String member) {
        return memberToGroup.getOrDefault(member, Collections.emptySet());
    }

    public boolean sameGroup(String a, String b) {
        Set<String> ga = getGroup(a);
        return !ga.isEmpty() && ga.equals(getGroup(b));
    }

    public static void main(String[] args) {
        // create new Group
//        List<Set<String>> newGroups = List.of(
//                Set.of("puncture", "slash", "impact"),
//                Set.of("multishot")
//        );
//        HomologyModGroup pool = new HomologyModGroup(newGroups);
    }
}