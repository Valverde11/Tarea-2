public class SnapshotMatrix {

    private SnapshotArray[] data;
    private int size;

    public SnapshotMatrix() {
        data = new SnapshotArray[32];
        size = 0;
    }

    public void add(SnapshotArray value) {

        if (size == data.length) {

            SnapshotArray[] newData =
                    new SnapshotArray[data.length * 2];

            for (int i = 0; i < size; i++) {
                newData[i] = data[i];
            }

            data = newData;
        }

        data[size++] = value;
    }

    public SnapshotArray get(int index) {
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