public class SnapshotArray {

    private int[][] data;
    private int size;

    public SnapshotArray() {
        this(32);
    }

    public SnapshotArray(int capacity) {
        data = new int[capacity][];
        size = 0;
    }

    public void add(int[] value) {
        if (size == data.length) {
            resize();
        }

        data[size++] = value;
    }

    private void resize() {
        int[][] newData = new int[data.length * 2][];

        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
        }

        data = newData;
    }

    public int[] get(int index) {
        return data[index];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        size = 0;
    }
}