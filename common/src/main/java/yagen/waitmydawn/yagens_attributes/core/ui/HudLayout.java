package yagen.waitmydawn.yagens_attributes.core.ui;

public final class HudLayout {
    public static final int MAX_OFFSET = 4096;
    public static final double MIN_SCALE = .5, MIN_BUFF_SCALE = .1, MAX_SCALE = 4;
    private HudLayout() {}

    public record Offsets(int x, int y) {}
    public record Placement(int x, int y, int width, int height, float scale) {
        public boolean contains(double px, double py) {
            return px >= x && py >= y && px < x + width && py < y + height;
        }
        public Offsets move(int viewportWidth, int viewportHeight, double left, double top) {
            if (viewportWidth < width || viewportHeight < height) throw new IllegalArgumentException("HUD exceeds viewport");
            if (!Double.isFinite(left) || !Double.isFinite(top)) throw new IllegalArgumentException("Non-finite HUD position");
            int nextX = (int) Math.round(Math.max(0, Math.min(viewportWidth - width, left)));
            int nextY = (int) Math.round(Math.max(0, Math.min(viewportHeight - height, top)));
            return new Offsets(Math.min(MAX_OFFSET, viewportWidth - width - nextX), Math.min(MAX_OFFSET, viewportHeight - height - nextY));
        }
    }

    public static Placement place(int viewportWidth, int viewportHeight, int logicalWidth, int logicalHeight, double scale, int offsetX, int offsetY) {
        if (viewportWidth <= 0 || viewportHeight <= 0 || logicalWidth <= 0 || logicalHeight <= 0 || !Double.isFinite(scale) || scale <= 0)
            throw new IllegalArgumentException("Invalid HUD geometry");
        // Render and editing share the same bottom-right anchor, including viewport fitting.
        float fitted = (float) Math.min(scale, Math.min(viewportWidth / (double) logicalWidth, viewportHeight / (double) logicalHeight));
        int width = Math.min(viewportWidth, Math.max(1, Math.round(logicalWidth * fitted)));
        int height = Math.min(viewportHeight, Math.max(1, Math.round(logicalHeight * fitted)));
        int x = Math.max(0, viewportWidth - width - Math.max(0, Math.min(MAX_OFFSET, offsetX)));
        int y = Math.max(0, viewportHeight - height - Math.max(0, Math.min(MAX_OFFSET, offsetY)));
        return new Placement(x, y, width, height, fitted);
    }
}
