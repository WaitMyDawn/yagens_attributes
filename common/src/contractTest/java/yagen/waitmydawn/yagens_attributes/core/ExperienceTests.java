package yagen.waitmydawn.yagens_attributes.core;

public final class ExperienceTests {
    private static int checks;
    private ExperienceTests() {}
    public static void run() {
        check(ExperiencePoints.total(7) == 91);
        check(ExperiencePoints.total(30) == 1395);
        check(ExperiencePoints.enchanting(30, 3) == 306);
        check(ExperiencePoints.enchanting(24, 2) == 149);
        check(ExperiencePoints.enchanting(1, 3) == 7);
        long accumulated = 0;
        for (int level = 0; level <= 100000; level++) {
            if (level <= 40 || level == 100 || level == 10000 || level == 100000) {
                check(ExperiencePoints.total(level) == accumulated);
                var boundary = ExperiencePoints.state(accumulated);
                check(boundary.level() == level && boundary.progress() == 0);
                for (float progress : new float[] {0, .25F, .9F}) {
                    long points = ExperiencePoints.balance(level, progress);
                    var state = ExperiencePoints.state(points);
                    check(ExperiencePoints.balance(state.level(), state.progress()) == points);
                    if (points >= 91) {
                        var after = ExperiencePoints.state(points - 91);
                        check(ExperiencePoints.balance(after.level(), after.progress()) == points - 91);
                    }
                }
            }
            accumulated += level < 15 ? 2L * level + 7 : level < 30 ? 5L * level - 38 : 9L * level - 158;
        }
        check(ExperiencePoints.affordable(91, 91));
        check(!ExperiencePoints.affordable(90, 91));
        check(!ExperiencePoints.affordable(-1, 91));
        check(!ExperiencePoints.affordable(100, -1));
        check(!ExperiencePoints.affordable(Long.MAX_VALUE, Long.MAX_VALUE));
        check(ExperiencePoints.total(Integer.MAX_VALUE) == Long.MAX_VALUE);
        check(ExperiencePoints.enchanting(Integer.MAX_VALUE, 3) == Long.MAX_VALUE);
        check(ExperiencePoints.balance(Integer.MAX_VALUE, 0) == -1);
        check(ExperiencePoints.balance(20, Float.NaN) == -1);
        check(ExperiencePoints.balance(20, 1) == -1);
        rejects(() -> ExperiencePoints.total(-1));
        rejects(() -> ExperiencePoints.state(-1));
        rejects(() -> ExperiencePoints.state(Long.MAX_VALUE));
        rejects(() -> ExperiencePoints.enchanting(30, 4));
        System.out.println("Passed " + checks + " XP curve, fixed-price debit and overflow checks");
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Experience calculation mismatch"); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected invalid XP rejection"); }
}
