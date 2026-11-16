package yagen.waitmydawn.yagens_attributes.core.ui;

public record DamageScatter(float x, float y, float velocityX, float velocityY) {
    public DamageScatter forTextWidth(int width) {
        float horizontalScale = Math.max(1, (width / 2F + 18) / 28F);
        return new DamageScatter(x * horizontalScale, y, velocityX * horizontalScale, velocityY);
    }

    public static DamageScatter sample(long sequence, double jitter, double radiusNoise) {
        double angle = sequence * 2.4 + (jitter - .5) * .5;
        double radius = 28 + Math.sqrt(Math.max(0, Math.min(1, radiusNoise))) * 24;
        float dx = (float) Math.cos(angle), dy = (float) Math.sin(angle);
        return new DamageScatter(dx * (float) radius, dy * (float) radius - 8, dx * .3F, dy * .2F - .15F);
    }
}
