import javax.swing.*;
import java.awt.*;


public class SequencePanel extends JDialog {

    private String structureType = "BST";
    private int[] keys;
    private int currentStep = 0;
    private int totalSteps  = 0;
    private int keyLimit    = 40; // max steps for visualization

    private StepTreePanel drawPanel;
    private JLabel stepLabel;
    private JButton prevBtn, nextBtn;
    private StringArray stepDescriptions = new StringArray();

    // Parallel arrays that store partial snapshots for each step
    private SnapshotMatrix snapshots = new SnapshotMatrix();
    // For Red-Black we need to know which nodes are red; store color separately
    private SnapshotMatrix rbColorSnapshots = new SnapshotMatrix(); // not used directly; color embedded in snapshot

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
        styleButton(loadBtn, new Color(55, 130, 220));
        topPanel.add(loadBtn);
        add(topPanel, BorderLayout.NORTH);

        // Drawing panel
        drawPanel = new StepTreePanel();
        drawPanel.setPreferredSize(new Dimension(880, 440));
        JScrollPane scrollPane = new JScrollPane(drawPanel);
        scrollPane.setBackground(new Color(30, 30, 40));
        scrollPane.getViewport().setBackground(new Color(30, 30, 40));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom navigation
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        bottomPanel.setBackground(new Color(40, 40, 55));

        prevBtn = new JButton("◀ Prev");
        nextBtn = new JButton("Next ▶");
        styleButton(prevBtn, new Color(55, 130, 220));
        styleButton(nextBtn, new Color(55, 130, 220));
        stepLabel = new JLabel("Step 0 / 0");
        stepLabel.setForeground(Color.WHITE);
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        bottomPanel.add(prevBtn);
        bottomPanel.add(stepLabel);
        bottomPanel.add(nextBtn);
        add(bottomPanel, BorderLayout.SOUTH);

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

        int limit = Math.min(keys.length, keyLimit);

        if (type.equals("Array") || type.equals("Linked List")) {
            buildLinearSnapshots(type, limit);
        } else {
            buildTreeSnapshots(type, limit);
        }

        totalSteps = snapshots.size();
        updateDisplay();
    }

    /**
     * For each step i (0..limit-1), inserts keys[0..i] into a fresh tree
     * and captures the snapshot. This ensures each step shows the correct partial tree.
     */
    private void buildTreeSnapshots(String type, int limit) {
        for (int i = 0; i < limit; i++) {
            // Build fresh tree with keys[0..i]
            BST bst       = type.equals("BST")        ? new BST()          : null;
            AVLTree avl   = type.equals("AVL")        ? new AVLTree()       : null;
            SplayTree sp  = type.equals("Splay")      ? new SplayTree()     : null;
            RedBlackTree rb = type.equals("Red-Black") ? new RedBlackTree() : null;

            for (int j = 0; j <= i; j++) {
                int v = keys[j];
                if (bst != null) bst.insert(v);
                if (avl != null) avl.insert(v);
                if (sp  != null) sp.insert(v);
                if (rb  != null) rb.insert(v);
            }

            SnapshotArray snap;
            int height;
            if (bst != null) { snap = bst.getSnapshot(); height = bst.getHeight(); }
            else if (avl != null) { snap = avl.getSnapshot(); height = avl.getHeight(); }
            else if (sp != null)  { snap = sp.getSnapshot();  height = sp.getHeight(); }
            else                  { snap = rb.getSnapshot();  height = rb.getHeight(); }

            snapshots.add(snap);
            stepDescriptions.add("Step " + (i + 1) + ": inserted " + keys[i] + " | Height: " + height);
        }
    }

    private void buildLinearSnapshots(String type, int limit) {
        for (int i = 0; i < limit; i++) {
            // Empty snapshot (no tree to draw) — description carries info
            snapshots.add(new SnapshotArray());
            StringBuilder sb = new StringBuilder();
            sb.append("Step ").append(i + 1).append(": inserted ").append(keys[i]);
            sb.append(" | ").append(type).append(" size: ").append(i + 1);
            sb.append(" | últimos: ");
            int show = Math.min(i + 1, 8);
            for (int j = i + 1 - show; j <= i; j++) sb.append(keys[j]).append(" ");
            stepDescriptions.add(sb.toString());
        }
    }

    private void step(int delta) {
        if (totalSteps == 0) return;
        currentStep = Math.max(0, Math.min(currentStep + delta, totalSteps - 1));
        updateDisplay();
    }

    private void updateDisplay() {
        if (totalSteps == 0) {
            stepLabel.setText("No steps loaded");
            return;
        }
        stepLabel.setText("Step " + (currentStep + 1) + " / " + totalSteps
                + "  —  " + stepDescriptions.get(currentStep));
        prevBtn.setEnabled(currentStep > 0);
        nextBtn.setEnabled(currentStep < totalSteps - 1);

        SnapshotArray snap = snapshots.get(currentStep);
        boolean isRB = structureType.equals("Red-Black");
        boolean isLinear = structureType.equals("Array") || structureType.equals("Linked List");
        drawPanel.setSnapshot(snap, structureType, isRB, isLinear,
                stepDescriptions.get(currentStep));
        drawPanel.repaint();
    }

    // ---- Inner panel that draws the tree for a single snapshot ----
    private static class StepTreePanel extends JPanel {
        private SnapshotArray snapshot;
        private String label = "";
        private boolean isRB = false;
        private boolean isLinear = false;
        private String description = "";

        StepTreePanel() {
            setBackground(new Color(30, 30, 40));
        }

        void setSnapshot(SnapshotArray snap, String lbl, boolean rb, boolean linear, String desc) {
            this.snapshot    = snap;
            this.label       = lbl;
            this.isRB        = rb;
            this.isLinear    = linear;
            this.description = desc;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Title
            g2.setColor(new Color(180, 200, 255));
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.drawString(label, 12, 22);

            if (isLinear || snapshot == null || snapshot.isEmpty()) {
                g2.setColor(new Color(150, 180, 150));
                g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
                g2.drawString("(estructura lineal — ver descripción abajo)", 12, h / 2);
                return;
            }

            // Compute layout
            int maxDepth = 0;
            for (int i = 0; i < snapshot.size(); i++) {
                int d = snapshot.get(i)[1];
                if (d > maxDepth) maxDepth = d;
            }

            int vSpacing  = Math.max(40, (h - 60) / (maxDepth + 1));
            int nodeRadius = 16;
            int[] xs = new int[snapshot.size()];
            int[] ys = new int[snapshot.size()];

            for (int i = 0; i < snapshot.size(); i++) {
                int[] n = snapshot.get(i);
                int depth = n[1], pos = n[2];
                int slots = 1 << depth;
                double slotWidth = (double) w / (slots + 1);
                xs[i] = (int)(slotWidth * (pos + 1));
                ys[i] = 35 + depth * vSpacing;
            }

            // Edges
            g2.setStroke(new BasicStroke(1.5f));
            for (int i = 1; i < snapshot.size(); i++) {
                int parentIdx = snapshot.get(i)[3];
                g2.setColor(new Color(100, 130, 170));
                g2.drawLine(xs[parentIdx], ys[parentIdx], xs[i], ys[i]);
            }

            // Nodes
            for (int i = 0; i < snapshot.size(); i++) {
                int[] n = snapshot.get(i);
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
                String lbl2 = String.valueOf(n[0]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lbl2, xs[i] - fm.stringWidth(lbl2) / 2, ys[i] + 4);
            }
        }
    }

    // ---- Styling helpers ----
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setOpaque(true); btn.setContentAreaFilled(true); btn.setBorderPainted(false);
        btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(new Color(50, 55, 75));
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }
}
