
/**
 * Binary Search Tree (BST) implementation.
 * Supports insert, search, delete with comparison counting.
 * Based on CE-1103 Lesson 05 - Hierarchical Data Structures.
 */
public class BST {
    private BSTNode root;
    private long comparisons;

    public BST() {
        this.root = null;
        this.comparisons = 0;
    }

    public void resetComparisons() { this.comparisons = 0; }
    public long getComparisons() { return comparisons; }

    public void insert(int element) {
        root = insert(element, root);
    }

    private BSTNode insert(int element, BSTNode node) {
        if (node == null) return new BSTNode(element);
        comparisons++;
        if (element < node.element) {
            node.left = insert(element, node.left);
        } else if (element > node.element) {
            node.right = insert(element, node.right);
        }
        return node;
    }

    public boolean search(int element) {
        return search(element, root);
    }

    private boolean search(int element, BSTNode node) {
        if (node == null) return false;
        comparisons++;
        if (element < node.element) return search(element, node.left);
        else if (element > node.element) return search(element, node.right);
        return true;
    }

    public void delete(int element) {
        root = delete(element, root);
    }

    private BSTNode delete(int element, BSTNode node) {
        if (node == null) return null;
        comparisons++;
        if (element < node.element) {
            node.left = delete(element, node.left);
        } else if (element > node.element) {
            node.right = delete(element, node.right);
        } else {
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            BSTNode min = findMin(node.right);
            node.element = min.element;
            node.right = delete(min.element, node.right);
        }
        return node;
    }

    private BSTNode findMin(BSTNode node) {
        while (node.left != null) node = node.left;
        return node;
    }

    public int getHeight() {
        return height(root);
    }

    private int height(BSTNode node) {
        if (node == null) return -1;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    public boolean isEmpty() { return root == null; }

    public BSTNode getRoot() { return root; }

    /** Returns all nodes in insertion order (level-order snapshot for visualization) */
    public SnapshotArray getSnapshot() {
        SnapshotArray nodes = new SnapshotArray();
        collectSnapshot(root, nodes, 0, 0, 0);
        return nodes;
    }

    private void collectSnapshot(BSTNode node, SnapshotArray nodes, int depth, int pos, int parentIdx) {
        if (node == null) return;
        int myIdx = nodes.size();
        nodes.add(new int[]{node.element, depth, pos, parentIdx, myIdx});
        collectSnapshot(node.left, nodes, depth + 1, pos * 2, myIdx);
        collectSnapshot(node.right, nodes, depth + 1, pos * 2 + 1, myIdx);
    }
}
