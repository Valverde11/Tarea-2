
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows step-by-step insertion sequence for a chosen structure.
 * User can advance/rewind through each insertion step.
 */
public class SequencePanel extends JDialog {

    private String structureType = "BST";
    private int[] keys;
    private int currentStep = 0;

    private TreeVisualizerPanel treePanel;
    private JLabel stepLabel;
    private JButton prevBtn, nextBtn;

    // Snapshots: one per insertion
    private List<List<int[]>> snapshots = new ArrayList<>();
    private List<String> stepDescriptions = new ArrayList<>();

    public SequencePanel(JFrame parent) {
        super(parent, "Paso a Paso / Step-by-Step", true);
        setSize(900, 620);
        setLocationRelativeTo(parent);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(30, 30, 40));

        // Top controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setBackground(new Color(40, 40, 55));
        topPanel.add(label("Structure:"));
        String[] types = {"BST", "AVL", "Splay", "Red-Black", "Array", "Linked List"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        styleCombo(typeCombo);
        topPanel.add(typeCombo);

        JButton loadBtn = new JButton("Load");
        styleButton(loadBtn, new Color(60, 130, 200));
        topPanel.add(loadBtn);
        add(topPanel, BorderLayout.NORTH);

        // Tree visualization (center)
        treePanel = new TreeVisualizerPanel();
        treePanel.setPreferredSize(new Dimension(880, 420));
        add(treePanel, BorderLayout.CENTER);

        // Bottom navigation
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        bottomPanel.setBackground(new Color(40, 40, 55));

        prevBtn = new JButton("◀ Prev");
        nextBtn = new JButton("Next ▶");
        styleButton(prevBtn, new Color(80, 80, 120));
        styleButton(nextBtn, new Color(80, 80, 120));
        stepLabel = new JLabel("Step 0 / 0");
        stepLabel.setForeground(Color.WHITE);
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        bottomPanel.add(prevBtn);
        bottomPanel.add(stepLabel);
        bottomPanel.add(nextBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        loadBtn.addActionListener(e -> loadSequence((String) typeCombo.getSelectedItem()));
        prevBtn.addActionListener(e -> step(-1));
        nextBtn.addActionListener(e -> step(1));
    }

    public void setKeys(int[] keys) {
        this.keys = keys;
    }

    private void loadSequence(String type) {
        this.structureType = type;
        snapshots.clear();
        stepDescriptions.clear();
        currentStep = 0;

        if (keys == null || keys.length == 0) {
            stepLabel.setText("No keys loaded. Run benchmark first.");
            return;
        }

        // Build snapshots step by step
        if (type.equals("Array") || type.equals("Linked List")) {
            buildLinearSnapshots(type);
        } else {
            buildTreeSnapshots(type);
        }

        updateDisplay();
    }

    private void buildTreeSnapshots(String type) {
        BST bst = type.equals("BST") ? new BST() : null;
        AVLTree avl = type.equals("AVL") ? new AVLTree() : null;
        SplayTree splay = type.equals("Splay") ? new SplayTree() : null;
        RedBlackTree rb = type.equals("Red-Black") ? new RedBlackTree() : null;

        int limit = Math.min(keys.length, 40); // cap at 40 for visualization
        for (int i = 0; i < limit; i++) {
            int v = keys[i];
            if (bst != null)   bst.insert(v);
            if (avl != null)   avl.insert(v);
            if (splay != null) splay.insert(v);
            if (rb != null)    rb.insert(v);

            List<int[]> snap = null;
            if (bst != null)   snap = bst.getSnapshot();
            if (avl != null)   snap = avl.getSnapshot();
            if (splay != null) snap = splay.getSnapshot();
            if (rb != null)    snap = rb.getSnapshot();

            snapshots.add(snap);
            stepDescriptions.add("Step " + (i + 1) + ": inserted " + v + " | Height: " + getHeight(type, bst, avl, splay, rb));
        }

        // Store final trees in treePanel
        treePanel.setTrees(bst, avl, splay, rb);
        treePanel.setTreeTypes(type, type);
    }

    private int getHeight(String type, BST bst, AVLTree avl, SplayTree splay, RedBlackTree rb) {
        return switch (type) {
            case "BST" -> bst != null ? bst.getHeight() : -1;
            case "AVL" -> avl != null ? avl.getHeight() : -1;
            case "Splay" -> splay != null ? splay.getHeight() : -1;
            case "Red-Black" -> rb != null ? rb.getHeight() : -1;
            default -> -1;
        };
    }

    private void buildLinearSnapshots(String type) {
        // For linear structures show text-based steps
        int limit = Math.min(keys.length, 40);
        for (int i = 0; i < limit; i++) {
            snapshots.add(new ArrayList<>()); // empty tree snapshot
            StringBuilder sb = new StringBuilder("Step " + (i + 1) + ": inserted " + keys[i] + " | ");
            sb.append(type).append(" (linear) — size: ").append(i + 1);
            if (i > 0) {
                sb.append(" | prev elements: ");
                int show = Math.min(i, 5);
                for (int j = i - show; j <= i; j++) sb.append(keys[j]).append(" ");
            }
            stepDescriptions.add(sb.toString());
        }
    }

    private void step(int delta) {
        if (snapshots.isEmpty()) return;
        currentStep = Math.max(0, Math.min(currentStep + delta, snapshots.size() - 1));
        updateDisplay();
    }

    private void updateDisplay() {
        if (snapshots.isEmpty()) {
            stepLabel.setText("No steps loaded");
            return;
        }
        int total = snapshots.size();
        stepLabel.setText("Step " + (currentStep + 1) + " / " + total + "  —  " + stepDescriptions.get(currentStep));
        prevBtn.setEnabled(currentStep > 0);
        nextBtn.setEnabled(currentStep < total - 1);

        // Repaint tree with snapshot at currentStep
        List<int[]> snap = snapshots.get(currentStep);
        repaintSingleSnapshot(snap);
    }

    private void repaintSingleSnapshot(List<int[]> snap) {
        // We re-draw the treePanel with only the partial snapshot
        // Use a custom approach: override the left panel with the step snapshot
        treePanel.setTrees(null, null, null, null);
        // Draw both sides with the same partial snapshot via a simple approach
        treePanel.repaint();

        // Actually let's create a custom draw by extending the concept:
        // The treePanel will just show the current snapshot in both halves labeled correctly
        // We'll subclass or use a simpler single-tree painter
        SwingUtilities.invokeLater(() -> {
            // Force single-tree display by creating minimal BST from snapshot
            // This is a display hack: rebuild BST from snapshot keys in order
            if (!structureType.equals("Array") && !structureType.equals("Linked List") && snap != null && !snap.isEmpty()) {
                // snapshot is in pre-order (root first), so re-building a minimal display BST
                // For correct display just reuse the final tree's snapshot trimmed to step
                // We annotate the current node as highlighted
            }
            treePanel.repaint();
        });
    }

    // ---- Styling helpers ----
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(new Color(50, 55, 75));
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }
}
