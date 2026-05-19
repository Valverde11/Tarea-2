import javax.swing.*;
import java.awt.*;


public class TreeVisualizerPanel extends JPanel {

    private String treeAType = "BST";
    private String treeBType = "AVL";

    private BST bst;
    private AVLTree avl;
    private SplayTree splay;
    private RedBlackTree rb;

    private SnapshotArray snapshotA;
    private SnapshotArray snapshotB;

    public TreeVisualizerPanel() {
        setBackground(new Color(30, 30, 40));
        setPreferredSize(new Dimension(900, 500));
    }

    public void setTrees(BST bst, AVLTree avl, SplayTree splay, RedBlackTree rb) {
        this.bst   = bst;
        this.avl   = avl;
        this.splay = splay;
        this.rb    = rb;
        refreshSnapshots();
    }

    public void setTreeTypes(String typeA, String typeB) {
        this.treeAType = typeA;
        this.treeBType = typeB;
        refreshSnapshots();
        repaint();
    }

    private void refreshSnapshots() {
        snapshotA = getSnapshot(treeAType);
        snapshotB = getSnapshot(treeBType);
    }

    private SnapshotArray getSnapshot(String type) {
        return switch (type) {
            case "BST"       -> bst   != null ? bst.getSnapshot()   : null;
            case "AVL"       -> avl   != null ? avl.getSnapshot()   : null;
            case "Splay"     -> splay != null ? splay.getSnapshot() : null;
            case "Red-Black" -> rb    != null ? rb.getSnapshot()    : null;
            default -> null;
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), half = w / 2;

        // Separator
        g2.setColor(new Color(60, 60, 80));
        g2.drawLine(half, 0, half, h);

        // Tree A
        drawTreeLabel(g2, treeAType, 10, 20);
        if (snapshotA != null && !snapshotA.isEmpty())
            drawTree(g2, snapshotA, 0, half, h, treeAType.equals("Red-Black"));
        else
            drawEmpty(g2, 0, half, h);

        // Tree B
        drawTreeLabel(g2, treeBType, half + 10, 20);
        if (snapshotB != null && !snapshotB.isEmpty())
            drawTree(g2, snapshotB, half, w, h, treeBType.equals("Red-Black"));
        else
            drawEmpty(g2, half, w, h);
    }

    private void drawTreeLabel(Graphics2D g2, String label, int x, int y) {
        g2.setColor(new Color(180, 200, 255));
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString(label, x, y);
    }

    private void drawEmpty(Graphics2D g2, int xMin, int xMax, int h) {
        g2.setColor(new Color(100, 100, 120));
        g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
        g2.drawString("(empty — run benchmark first)", xMin + 20, h / 2);
    }

    private void drawTree(Graphics2D g2, SnapshotArray nodes, int xMin, int xMax, int h, boolean isRB) {
        if (nodes.isEmpty()) return;

        int maxDepth = 0;
        for (int i = 0; i < nodes.size(); i++) {
            int d = nodes.get(i)[1];
            if (d > maxDepth) maxDepth = d;
        }

        int vSpacing   = Math.max(40, (h - 60) / (maxDepth + 1));
        int nodeRadius = 16;
        int treeWidth  = xMax - xMin;

        int[] xs = new int[nodes.size()];
        int[] ys = new int[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            int[] n     = nodes.get(i);
            int depth   = n[1], pos = n[2];
            int slots   = 1 << depth;
            double slotWidth = (double) treeWidth / (slots + 1);
            xs[i] = (int)(xMin + slotWidth * (pos + 1));
            ys[i] = 35 + depth * vSpacing;
        }

        // Edges
        g2.setStroke(new BasicStroke(1.5f));
        for (int i = 1; i < nodes.size(); i++) {
            int parentIdx = nodes.get(i)[3];
            g2.setColor(new Color(100, 130, 170));
            g2.drawLine(xs[parentIdx], ys[parentIdx], xs[i], ys[i]);
        }

        // Nodes
        for (int i = 0; i < nodes.size(); i++) {
            int[] n = nodes.get(i);
            boolean isRed = isRB && n.length > 5 && n[5] == 1;
            Color fill   = isRed ? new Color(200, 60, 60)  : new Color(50, 100, 180);
            Color border = isRed ? new Color(255, 120, 120) : new Color(100, 160, 255);

            g2.setColor(fill);
            g2.fillOval(xs[i] - nodeRadius, ys[i] - nodeRadius, nodeRadius * 2, nodeRadius * 2);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(xs[i] - nodeRadius, ys[i] - nodeRadius, nodeRadius * 2, nodeRadius * 2);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            String lbl = String.valueOf(n[0]);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, xs[i] - fm.stringWidth(lbl) / 2, ys[i] + 4);
        }
    }
}
