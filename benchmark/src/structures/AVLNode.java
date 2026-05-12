
public class AVLNode {
    public int element;
    public AVLNode left, right;
    public int height;

    public AVLNode(int element) {
        this(element, null, null);
    }

    public AVLNode(int element, AVLNode left, AVLNode right) {
        this.element = element;
        this.left = left;
        this.right = right;
        this.height = 0;
    }
}
