
/**
 * AVL Tree implementation.
 * Balances via single and double rotations.
 * Based on CE-1103 Lesson 05 - Hierarchical Data Structures.
 */
public class AVLTree {
    private static final int ALLOWED_IMBALANCE = 1;
    private AVLNode root;
    private long comparisons;

    public AVLTree() {
        this.root = null;
        this.comparisons = 0;
    }

    public void resetComparisons() { this.comparisons = 0; }
    public long getComparisons() { return comparisons; }

    private int height(AVLNode t) {
        return t == null ? -1 : t.height;
    }

    public void insert(int x) {
        root = insert(x, root);
    }

    private AVLNode insert(int x, AVLNode t) {
        if (t == null) return new AVLNode(x);
        comparisons++;
        if (x < t.element) {
            t.left = insert(x, t.left);
        } else if (x > t.element) {
            t.right = insert(x, t.right);
        }
        return balance(t);
    }

    private AVLNode balance(AVLNode t) {
        if (t == null) return t;
        if (height(t.left) - height(t.right) > ALLOWED_IMBALANCE) {
            if (height(t.left.left) >= height(t.left.right))
                t = rotateWithLeftChild(t);   // LL single
            else
                t = doubleWithLeftChild(t);   // LR double
        } else if (height(t.right) - height(t.left) > ALLOWED_IMBALANCE) {
            if (height(t.right.right) >= height(t.right.left))
                t = rotateWithRightChild(t);  // RR single
            else
                t = doubleWithRightChild(t);  // RL double
        }
        t.height = Math.max(height(t.left), height(t.right)) + 1;
        return t;
    }

    // Single right rotation (LL case)
    private AVLNode rotateWithLeftChild(AVLNode k2) {
        AVLNode k1 = k2.left;
        k2.left = k1.right;
        k1.right = k2;
        k2.height = Math.max(height(k2.left), height(k2.right)) + 1;
        k1.height = Math.max(height(k1.left), k2.height) + 1;
        return k1;
    }

    // Single left rotation (RR case)
    private AVLNode rotateWithRightChild(AVLNode k1) {
        AVLNode k2 = k1.right;
        k1.right = k2.left;
        k2.left = k1;
        k1.height = Math.max(height(k1.left), height(k1.right)) + 1;
        k2.height = Math.max(k1.height, height(k2.right)) + 1;
        return k2;
    }

    // LR double rotation
    private AVLNode doubleWithLeftChild(AVLNode k3) {
        k3.left = rotateWithRightChild(k3.left);
        return rotateWithLeftChild(k3);
    }

    // RL double rotation
    private AVLNode doubleWithRightChild(AVLNode k1) {
        k1.right = rotateWithLeftChild(k1.right);
        return rotateWithRightChild(k1);
    }

    public boolean search(int x) {
        return search(x, root);
    }

    private boolean search(int x, AVLNode node) {
        if (node == null) return false;
        comparisons++;
        if (x < node.element) return search(x, node.left);
        else if (x > node.element) return search(x, node.right);
        return true;
    }

    public void delete(int x) {
        root = delete(x, root);
    }

    private AVLNode delete(int x, AVLNode t) {
        if (t == null) return null;
        comparisons++;
        if (x < t.element) {
            t.left = delete(x, t.left);
        } else if (x > t.element) {
            t.right = delete(x, t.right);
        } else if (t.left != null && t.right != null) {
            t.element = findMin(t.right).element;
            t.right = delete(t.element, t.right);
        } else {
            t = (t.left != null) ? t.left : t.right;
        }
        return balance(t);
    }

    private AVLNode findMin(AVLNode node) {
        while (node.left != null) node = node.left;
        return node;
    }

    public int getHeight() { return height(root); }
    public boolean isEmpty() { return root == null; }
    public AVLNode getRoot() { return root; }

    public SnapshotArray getSnapshot() {
        SnapshotArray nodes = new SnapshotArray();
        collectSnapshot(root, nodes, 0, 0, 0);
        return nodes;
    }

    private void collectSnapshot(AVLNode node, SnapshotArray nodes, int depth, int pos, int parentIdx) {
        if (node == null) return;
        int myIdx = nodes.size();
        nodes.add(new int[]{node.element, depth, pos, parentIdx, myIdx});
        collectSnapshot(node.left, nodes, depth + 1, pos * 2, myIdx);
        collectSnapshot(node.right, nodes, depth + 1, pos * 2 + 1, myIdx);
    }
}
