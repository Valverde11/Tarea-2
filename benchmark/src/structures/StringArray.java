public class StringArray {

    private String[] data;
    private int size;

    public StringArray() {
        data = new String[32];
        size = 0;
    }

    public void add(String value) {

        if (size == data.length) {

            String[] newData =
                    new String[data.length * 2];

            for (int i = 0; i < size; i++) {
                newData[i] = data[i];
            }

            data = newData;
        }

        data[size++] = value;
    }

    public String get(int index) {
        return data[index];
    }

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
    }
}