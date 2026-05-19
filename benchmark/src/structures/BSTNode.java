public class BSTNode {
    public int element;
    public BSTNode left, right;
    public int height;

    public BSTNode(int element) {
        this.element = element;
        this.left = null;
        this.right = null;
        this.height = 0;
    }
}
