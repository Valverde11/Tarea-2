
/**
 * Red-Black Tree implementation.
 * Only insert and search are measured (delete is N/A per assignment spec).
 * Invariants:
 *  1. Every node is RED or BLACK.
 *  2. Root is BLACK.
 *  3. Red nodes have BLACK children.
 *  4. All paths from a node to null leaves have the same number of black nodes.
 */
public class RedBlackTree {

    static final boolean RED = true;
    static final boolean BLACK = false;

    public static class RBNode {
        public int element;
        public RBNode left, right, parent;
        public boolean color;

        public RBNode(int element, boolean color) {
            this.element = element;
            this.color = color;
        }
    }

    private RBNode root;
    private final RBNode NIL; // sentinel null node
    private long comparisons;

    public RedBlackTree() {
        NIL = new RBNode(0, BLACK);
        NIL.left = NIL.right = NIL.parent = NIL;
        root = NIL;
        comparisons = 0;
    }

    public void resetComparisons() { this.comparisons = 0; }
    public long getComparisons() { return comparisons; }

    public void insert(int x) {
        RBNode z = new RBNode(x, RED);
        z.left = z.right = z.parent = NIL;

        RBNode y = NIL;
        RBNode cur = root;
        while (cur != NIL) {
            y = cur;
            comparisons++;
            if (x < cur.element) cur = cur.left;
            else if (x > cur.element) cur = cur.right;
            else return; // duplicate
        }
        z.parent = y;
        if (y == NIL) {
            root = z;
        } else {
            comparisons++;
            if (x < y.element) y.left = z;
            else y.right = z;
        }
        insertFixup(z);
    }

    private void insertFixup(RBNode z) {
        while (z.parent.color == RED) {
            if (z.parent == z.parent.parent.left) {
                RBNode y = z.parent.parent.right; // uncle
                if (y.color == RED) {
                    // Case 1: uncle red → recolor
                    z.parent.color = BLACK;
                    y.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right) {
                        // Case 2: uncle black, z is right child → left rotate parent
                        z = z.parent;
                        leftRotate(z);
                    }
                    // Case 3: uncle black, z is left child → right rotate grandparent
                    z.parent.color = BLACK;
                    z.parent.parent.color = RED;
                    rightRotate(z.parent.parent);
                }
            } else {
                RBNode y = z.parent.parent.left; // uncle
                if (y.color == RED) {
                    z.parent.color = BLACK;
                    y.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left) {
                        z = z.parent;
                        rightRotate(z);
                    }
                    z.parent.color = BLACK;
                    z.parent.parent.color = RED;
                    leftRotate(z.parent.parent);
                }
            }
        }
        root.color = BLACK;
    }

    private void leftRotate(RBNode x) {
        RBNode y = x.right;
        x.right = y.left;
        if (y.left != NIL) y.left.parent = x;
        y.parent = x.parent;
        if (x.parent == NIL) root = y;
        else if (x == x.parent.left) x.parent.left = y;
        else x.parent.right = y;
        y.left = x;
        x.parent = y;
    }

    private void rightRotate(RBNode y) {
        RBNode x = y.left;
        y.left = x.right;
        if (x.right != NIL) x.right.parent = y;
        x.parent = y.parent;
        if (y.parent == NIL) root = x;
        else if (y == y.parent.right) y.parent.right = x;
        else y.parent.left = x;
        x.right = y;
        y.parent = x;
    }

    public boolean search(int x) {
        RBNode cur = root;
        while (cur != NIL) {
            comparisons++;
            if (x < cur.element) cur = cur.left;
            else if (x > cur.element) cur = cur.right;
            else return true;
        }
        return false;
    }

    public int getHeight() {
        return height(root);
    }

    private int height(RBNode node) {
        if (node == NIL) return -1;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    public boolean isEmpty() { return root == NIL; }
    public RBNode getRoot() { return root; }
    public RBNode getNIL() { return NIL; }

    public SnapshotArray  getSnapshot() {
        SnapshotArray nodes = new SnapshotArray();
        collectSnapshot(root, nodes, 0, 0, 0);
        return nodes;
    }

    /** Returns [element, depth, pos, parentIdx, myIdx, color(1=red,0=black)] */
    private void collectSnapshot(RBNode node, SnapshotArray nodes, int depth, int pos, int parentIdx) {
        if (node == NIL) return;
        int myIdx = nodes.size();
        nodes.add(new int[]{node.element, depth, pos, parentIdx, myIdx, node.color ? 1 : 0});
        collectSnapshot(node.left, nodes, depth + 1, pos * 2, myIdx);
        collectSnapshot(node.right, nodes, depth + 1, pos * 2 + 1, myIdx);
    }
}
