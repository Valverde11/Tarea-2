

import java.util.ArrayList;
import java.util.List;

/**
 * Splay Tree implementation.
 * Uses Zig, Zig-Zig and Zig-Zag rotations to bring accessed nodes to the root.
 * Based on CE-1103 Lesson 05 - Hierarchical Data Structures.
 */
public class SplayTree {

    public static class SplayNode {
        public int element;
        public SplayNode left, right;

        public SplayNode(int element) {
            this.element = element;
        }
    }

    private SplayNode root;
    private long comparisons;

    public SplayTree() {
        this.root = null;
        this.comparisons = 0;
    }

    public void resetComparisons() { this.comparisons = 0; }
    public long getComparisons() { return comparisons; }

    /**
     * Splay: brings node with key x to root using top-down splaying.
     */
    private SplayNode splay(int x, SplayNode t) {
        if (t == null) return null;

        SplayNode header = new SplayNode(0);
        SplayNode leftTree = header, rightTree = header;
        header.left = header.right = null;

        while (true) {
            comparisons++;
            if (x < t.element) {
                if (t.left == null) break;
                comparisons++;
                if (x < t.left.element) {
                    // Zig-Zig: rotate right
                    SplayNode y = t.left;
                    t.left = y.right;
                    y.right = t;
                    t = y;
                    if (t.left == null) break;
                }
                // Link right
                rightTree.left = t;
                rightTree = t;
                t = t.left;
            } else if (x > t.element) {
                if (t.right == null) break;
                comparisons++;
                if (x > t.right.element) {
                    // Zig-Zig: rotate left
                    SplayNode y = t.right;
                    t.right = y.left;
                    y.left = t;
                    t = y;
                    if (t.right == null) break;
                }
                // Link left
                leftTree.right = t;
                leftTree = t;
                t = t.right;
            } else {
                break;
            }
        }
        // Assemble
        leftTree.right = t.left;
        rightTree.left = t.right;
        t.left = header.right;
        t.right = header.left;
        return t;
    }

    public void insert(int x) {
        SplayNode newNode = new SplayNode(x);
        if (root == null) {
            root = newNode;
            return;
        }
        root = splay(x, root);
        comparisons++;
        if (x < root.element) {
            newNode.left = root.left;
            newNode.right = root;
            root.left = null;
            root = newNode;
        } else if (x > root.element) {
            newNode.right = root.right;
            newNode.left = root;
            root.right = null;
            root = newNode;
        }
        // duplicate: ignore
    }

    public boolean search(int x) {
        if (root == null) return false;
        root = splay(x, root);
        comparisons++;
        return root.element == x;
    }

    public void delete(int x) {
        if (root == null) return;
        root = splay(x, root);
        comparisons++;
        if (root.element != x) return;
        if (root.left == null) {
            root = root.right;
        } else {
            SplayNode newRoot = splay(x, root.left);
            newRoot.right = root.right;
            root = newRoot;
        }
    }

    public int getHeight() { return height(root); }

    private int height(SplayNode node) {
        if (node == null) return -1;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    public boolean isEmpty() { return root == null; }
    public SplayNode getRoot() { return root; }

    public List<int[]> getSnapshot() {
        List<int[]> nodes = new ArrayList<>();
        collectSnapshot(root, nodes, 0, 0, 0);
        return nodes;
    }

    private void collectSnapshot(SplayNode node, List<int[]> nodes, int depth, int pos, int parentIdx) {
        if (node == null) return;
        int myIdx = nodes.size();
        nodes.add(new int[]{node.element, depth, pos, parentIdx, myIdx});
        collectSnapshot(node.left, nodes, depth + 1, pos * 2, myIdx);
        collectSnapshot(node.right, nodes, depth + 1, pos * 2 + 1, myIdx);
    }
}
