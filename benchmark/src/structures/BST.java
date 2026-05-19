

import java.util.ArrayList;
import java.util.List;

/**
 * Binary Search Tree (BST) implementation.
 * Supports insert, search, delete with comparison counting.
 * Based on CE-1103 Lesson 05 - Hierarchical Data Structures.
 */
public class BST {
    private BSTNode root;
    private long comparisons;
    // History of snapshots produced during the last operation (e.g., insert)
    private List<List<int[]>> opSnapshots = new ArrayList<>();

    public BST() {
        this.root = null;
        this.comparisons = 0;
    }

    public void resetComparisons() { this.comparisons = 0; }
    public long getComparisons() { return comparisons; }

    public void insert(int element) {
        opSnapshots.clear();
        root = insert(element, root);
        // record final state after insertion
        recordSnapshot();
    }

    private BSTNode insert(int element, BSTNode node) {
        if (node == null) {
            BSTNode nn = new BSTNode(element);
            recordSnapshot();
            return nn;
        }
        comparisons++;
        if (element < node.element) {
            node.left = insert(element, node.left);
        } else if (element > node.element) {
            node.right = insert(element, node.right);
        }
        // update height and balance
        node.height = Math.max(height(node.left), height(node.right)) + 1;
        node = balance(node);
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
        if (node != null) node.height = Math.max(height(node.left), height(node.right)) + 1;
        node = balance(node);
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
        return node == null ? -1 : node.height;
    }

    public boolean isEmpty() { return root == null; }

    public BSTNode getRoot() { return root; }

    /** Returns all nodes in insertion order (level-order snapshot for visualization) */
    public List<int[]> getSnapshot() {
        List<int[]> nodes = new ArrayList<>();
        collectSnapshot(root, nodes, 0, 0, 0);
        return nodes;
    }

    // --- Rotation / balancing (AVL-like) ---
    private BSTNode balance(BSTNode t) {
        if (t == null) return null;
        if (height(t.left) - height(t.right) > 1) {
            if (height(t.left.left) >= height(t.left.right))
                t = rotateWithLeftChild(t); // LL
            else
                t = doubleWithLeftChild(t); // LR
        } else if (height(t.right) - height(t.left) > 1) {
            if (height(t.right.right) >= height(t.right.left))
                t = rotateWithRightChild(t); // RR
            else
                t = doubleWithRightChild(t); // RL
        }
        return t;
    }

    private BSTNode rotateWithLeftChild(BSTNode k2) {
        BSTNode k1 = k2.left;
        k2.left = k1.right;
        k1.right = k2;
        k2.height = Math.max(height(k2.left), height(k2.right)) + 1;
        k1.height = Math.max(height(k1.left), k2.height) + 1;
        recordSnapshot();
        return k1;
    }

    private BSTNode rotateWithRightChild(BSTNode k1) {
        BSTNode k2 = k1.right;
        k1.right = k2.left;
        k2.left = k1;
        k1.height = Math.max(height(k1.left), height(k1.right)) + 1;
        k2.height = Math.max(k1.height, height(k2.right)) + 1;
        recordSnapshot();
        return k2;
    }

    private BSTNode doubleWithLeftChild(BSTNode k3) {
        k3.left = rotateWithRightChild(k3.left);
        return rotateWithLeftChild(k3);
    }

    private BSTNode doubleWithRightChild(BSTNode k1) {
        k1.right = rotateWithLeftChild(k1.right);
        return rotateWithRightChild(k1);
    }

    // Record a snapshot of current tree state to opSnapshots
    private void recordSnapshot() {
        List<int[]> snap = new ArrayList<>();
        collectSnapshot(root, snap, 0, 0, 0);
        opSnapshots.add(snap);
    }

    // Return and clear the operation snapshots collected during last operation
    public List<List<int[]>> getSnapshotsHistory() {
        List<List<int[]>> out = new ArrayList<>(opSnapshots);
        opSnapshots.clear();
        return out;
    }

    private void collectSnapshot(BSTNode node, List<int[]> nodes, int depth, int pos, int parentIdx) {
        if (node == null) return;
        int myIdx = nodes.size();
        nodes.add(new int[]{node.element, depth, pos, parentIdx, myIdx});
        collectSnapshot(node.left, nodes, depth + 1, pos * 2, myIdx);
        collectSnapshot(node.right, nodes, depth + 1, pos * 2 + 1, myIdx);
    }
}
