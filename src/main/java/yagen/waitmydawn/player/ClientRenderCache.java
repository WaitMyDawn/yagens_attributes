package yagen.waitmydawn.player;

public class ClientRenderCache {

    public static int[] getRowCounts(int modCount) {
        return new int[]{
                MOD_LAYOUT[modCount - 1][0],
                MOD_LAYOUT[modCount - 1][1],
                MOD_LAYOUT[modCount - 1][2],
                MOD_LAYOUT[modCount - 1][3]};
    }

    public static final int[][] MOD_LAYOUT = {
            {1, 0, 0, 0}, //1
            {2, 0, 0, 0}, //2
            {2, 1, 0, 0}, //3
            {2, 2, 0, 0}, //4
            {2, 2, 1, 0}, //5
            {2, 2, 2, 0}, //6
            {2, 2, 2, 1}, //7
            {2, 2, 2, 2}, //8
            {3, 2, 2, 2}, //9
            {3, 3, 2, 2}, //10
            {3, 3, 3, 2}, //11
            {3, 3, 3, 3}  //12
    };
}
