
public class CustomArray {
    private int[] data;
    private int size;
    private long comparisons;

    public CustomArray(int capacity) {
        this.data = new int[capacity];
        this.size = 0;
        this.comparisons = 0;
    }

    public void resetComparisons() { this.comparisons = 0; }
    public long getComparisons() { return comparisons; }

    public void insert(int element) {
        if (size == data.length) {
            int[] newData = new int[data.length * 2];
            System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
        data[size++] = element;
    }

    public boolean search(int element) {
        for (int i = 0; i < size; i++) {
            comparisons++;
            if (data[i] == element) return true;
        }
        return false;
    }

    public boolean delete(int element) {
        for (int i = 0; i < size; i++) {
            comparisons++;
            if (data[i] == element) {
                // Shift left
                System.arraycopy(data, i + 1, data, i, size - i - 1);
                size--;
                return true;
            }
        }
        return false;
    }

    public int getSize() { return size; }
    public boolean isEmpty() { return size == 0; }
}
