
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Main application window for the benchmark.
 * CE-1103 Extraclase 2 — BST, AVL, Splay, Red-Black, Array, Linked List benchmark.
 */
public class BenchmarkFrame extends JFrame {

    // Config inputs
    private JTextField nField, seedField, wField, rField, searchCountField;
    private JCheckBox bstCheck, avlCheck, splayCheck, rbCheck, arrayCheck, listCheck;
    private JTextArea manualKeysArea;
    private JRadioButton autoSearchRadio, manualSearchRadio;

    // Results table
    private JTable resultTable;
    private DefaultTableModel tableModel;

    // Status
    private JLabel statusLabel;
    private JTextArea logArea;

    // Last run data
    private List<BenchmarkResult> lastResults;
    private int[] lastInsertionOrder;
    private int[] lastSearchKeys;
    private BenchmarkEngine.Config lastConfig;

    // Trees from last run (for visualizer)
    private BST lastBST;
    private AVLTree lastAVL;
    private SplayTree lastSplay;
    private RedBlackTree lastRB;

    public BenchmarkFrame() {
        super("CE-1103 — Benchmark de Estructuras de Datos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 720);
        setLocationRelativeTo(null);
        setBackground(new Color(30, 30, 42));
        buildUI();
        loadDefaults();
    }

    private void buildUI() {
        getContentPane().setBackground(new Color(30, 30, 42));
        setLayout(new BorderLayout(8, 8));

        // ---- Left panel (config) ----
        JPanel configPanel = buildConfigPanel();
        configPanel.setPreferredSize(new Dimension(240, 0));
        add(configPanel, BorderLayout.WEST);

        // ---- Center (tabs: table + log) ----
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(40, 40, 55));
        tabs.setForeground(Color.BLACK);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabs.addTab("📊 Results Table", buildResultsPanel());
        tabs.addTab("📋 Log", buildLogPanel());

        add(tabs, BorderLayout.CENTER);

        // ---- Bottom status ----
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(25, 25, 35));
        statusLabel = new JLabel("Ready. Load defaults or configure and run.");
        statusLabel.setForeground(new Color(140, 200, 140));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel buildConfigPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(38, 38, 52));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 10, 10, 10));

        // Title
        JLabel title = new JLabel("Configuración");
        title.setForeground(new Color(160, 190, 255));
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));

        // Parameters
        panel.add(sectionLabel("Parámetros"));
        nField = addField(panel, "N (claves a insertar):", "1000");
        seedField = addField(panel, "Semilla (seed):", "42");
        wField = addField(panel, "W (warmup rounds):", "2");
        rField = addField(panel, "R (rondas medidas):", "5");
        searchCountField = addField(panel, "Consultas (búsquedas):", "200");

        panel.add(Box.createVerticalStrut(10));

        // Search mode
        panel.add(sectionLabel("Modo de búsqueda"));
        ButtonGroup bg = new ButtonGroup();
        autoSearchRadio = new JRadioButton("Automático (semilla)");
        manualSearchRadio = new JRadioButton("Manual (teclear/pegar)");
        styleRadio(autoSearchRadio);
        styleRadio(manualSearchRadio);
        autoSearchRadio.setSelected(true);
        bg.add(autoSearchRadio);
        bg.add(manualSearchRadio);
        panel.add(autoSearchRadio);
        panel.add(manualSearchRadio);
        panel.add(Box.createVerticalStrut(4));

        manualKeysArea = new JTextArea(3, 16);
        manualKeysArea.setBackground(new Color(50, 52, 70));
        manualKeysArea.setForeground(Color.WHITE);
        manualKeysArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        manualKeysArea.setLineWrap(true);
        manualKeysArea.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 110)));
        manualKeysArea.setToolTipText("Enter comma or space separated keys");
        JScrollPane keysScroll = new JScrollPane(manualKeysArea);
        keysScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        keysScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        panel.add(keysScroll);

        JButton loadFileBtn = new JButton("📁 Cargar archivo");
        loadFileBtn.setOpaque(true);
        loadFileBtn.setContentAreaFilled(true);
        loadFileBtn.setBorderPainted(false);
        loadFileBtn.setBackground(new Color(100, 120, 160));
        loadFileBtn.setForeground(Color.WHITE);
        loadFileBtn.setFocusPainted(false);
        loadFileBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        loadFileBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        loadFileBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        loadFileBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadFileBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loadFileBtn.addActionListener(e -> loadSearchFile());
        panel.add(Box.createVerticalStrut(3));
        panel.add(loadFileBtn);

        panel.add(Box.createVerticalStrut(10));

        // Structures
        panel.add(sectionLabel("Estructuras activas"));
        bstCheck   = addCheck(panel, "BST");
        avlCheck   = addCheck(panel, "AVL");
        splayCheck = addCheck(panel, "Splay");
        rbCheck    = addCheck(panel, "Red-Black");
        arrayCheck = addCheck(panel, "Array");
        listCheck  = addCheck(panel, "Lista enlazada");

        panel.add(Box.createVerticalStrut(10));

        // Buttons
        Color buttonBlue = new Color(55, 130, 220);
        JButton defaultsBtn = bigButton("⚡ Valores por defecto", buttonBlue);
        JButton runBtn      = bigButton("▶ Ejecutar",              buttonBlue);
        JButton exportBtn   = bigButton("💾 Exportar CSV",           buttonBlue);
        JButton vizBtn      = bigButton("🌳 Visualizar",            buttonBlue);
        JButton seqBtn      = bigButton("🔢 Paso a paso",           buttonBlue);

        JPanel actionsPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        actionsPanel.setBackground(panel.getBackground());
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsPanel.add(defaultsBtn);
        actionsPanel.add(runBtn);
        actionsPanel.add(exportBtn);
        actionsPanel.add(vizBtn);
        actionsPanel.add(seqBtn);

        panel.add(actionsPanel);
        panel.add(Box.createVerticalStrut(6));

        // Actions
        defaultsBtn.addActionListener(e -> loadDefaults());
        runBtn.addActionListener(e -> runBenchmark());
        exportBtn.addActionListener(e -> exportCSV());
        vizBtn.addActionListener(e -> openVisualizer());
        seqBtn.addActionListener(e -> openSequence());

        return panel;
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 42));

        String[] cols = {"Métrica", "BST", "AVL", "Splay", "Red-Black", "Array", "Lista Enlazada"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        resultTable = new JTable(tableModel);
        resultTable.setBackground(new Color(35, 38, 52));
        resultTable.setForeground(Color.WHITE);
        resultTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultTable.setRowHeight(22);
        Color headerBlue = new Color(55, 130, 220);
        resultTable.getTableHeader().setBackground(headerBlue);
        resultTable.getTableHeader().setForeground(Color.BLACK);
        resultTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        resultTable.setGridColor(new Color(60, 65, 85));
        resultTable.setSelectionBackground(new Color(70, 90, 140));

        // Alternate row colors
        resultTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setForeground(Color.WHITE);
                if (isSelected) {
                    setBackground(new Color(70, 90, 140));
                } else if (column == 0) {
                    setBackground(new Color(45, 50, 70));
                } else {
                    setBackground(row % 2 == 0 ? new Color(35, 38, 52) : new Color(42, 45, 62));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(resultTable);
        scroll.getViewport().setBackground(new Color(35, 38, 52));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 65, 85)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 42));
        logArea = new JTextArea();
        logArea.setBackground(new Color(20, 22, 32));
        logArea.setForeground(new Color(140, 220, 140));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 65, 85)));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ---- Actions ----

    private void loadDefaults() {
        nField.setText("1000");
        seedField.setText("42");
        wField.setText("2");
        rField.setText("5");
        searchCountField.setText("200");
        bstCheck.setSelected(true);
        avlCheck.setSelected(true);
        splayCheck.setSelected(true);
        rbCheck.setSelected(true);
        arrayCheck.setSelected(true);
        listCheck.setSelected(true);
        autoSearchRadio.setSelected(true);
        manualKeysArea.setText("");
        log("Valores por defecto cargados. N=1000, seed=42, W=2, R=5, 200 búsquedas.");
        setStatus("Defaults loaded. Click ▶ Ejecutar Benchmark.");
    }

    private void runBenchmark() {
        BenchmarkEngine.Config cfg = buildConfig();
        if (cfg == null) return;

        setStatus("Running benchmark...");
        log("=== Iniciando benchmark ===");
        log("N=" + cfg.N + ", seed=" + cfg.seed + ", W=" + cfg.W + ", R=" + cfg.R);

        tableModel.setRowCount(0);

        SwingWorker<List<BenchmarkResult>, String> worker = new SwingWorker<>() {
            @Override
            protected List<BenchmarkResult> doInBackground() {
                BenchmarkEngine engine = new BenchmarkEngine(cfg);
                engine.setProgressListener(msg -> publish(msg));
                List<BenchmarkResult> results = engine.run();
                lastInsertionOrder = engine.getInsertionOrder();
                lastSearchKeys = engine.getSearchKeys();
                lastConfig = cfg;
                // Build final trees for visualizer
                buildFinalTrees(cfg);
                return results;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String msg : chunks) log(msg);
            }

            @Override
            protected void done() {
                try {
                    lastResults = get();
                    populateTable(lastResults, cfg);
                    setStatus("Benchmark completado. N=" + cfg.N + ", W=" + cfg.W + ", R=" + cfg.R);
                    log("=== Benchmark completado ===");
                } catch (Exception ex) {
                    log("ERROR: " + ex.getMessage());
                    setStatus("Error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void buildFinalTrees(BenchmarkEngine.Config cfg) {
        BenchmarkEngine eng = new BenchmarkEngine(cfg);
        eng.prepare();
        int[] keys = eng.getInsertionOrder();

        if (cfg.useBST)   { lastBST = new BST(); for (int v : keys) lastBST.insert(v); }
        if (cfg.useAVL)   { lastAVL = new AVLTree(); for (int v : keys) lastAVL.insert(v); }
        if (cfg.useSplay) { lastSplay = new SplayTree(); for (int v : keys) lastSplay.insert(v); }
        if (cfg.useRB)    { lastRB = new RedBlackTree(); for (int v : keys) lastRB.insert(v); }
    }

    private void populateTable(List<BenchmarkResult> results, BenchmarkEngine.Config cfg) {
        tableModel.setRowCount(0);

        // Update heights
        if (lastBST != null) { for (BenchmarkResult r : results) if (r.structureName.equals("BST"))         r.heightOrSize = lastBST.getHeight(); }
        if (lastAVL != null) { for (BenchmarkResult r : results) if (r.structureName.equals("AVL"))         r.heightOrSize = lastAVL.getHeight(); }
        if (lastSplay != null){ for (BenchmarkResult r : results) if (r.structureName.equals("Splay"))      r.heightOrSize = lastSplay.getHeight(); }
        if (lastRB != null)  { for (BenchmarkResult r : results) if (r.structureName.equals("Red-Black"))  r.heightOrSize = lastRB.getHeight(); }

        String[] metrics = {
            "Tiempo Inserción", "Tiempo Búsqueda", "Tiempo Borrado",
            "Comparaciones Inserción", "Comparaciones Búsqueda", "Comparaciones Borrado",
            "O() Inserción", "O() Búsqueda", "O() Borrado",
            "Altura / Tamaño"
        };

        // Build rows (metric → structure values)
        for (String metric : metrics) {
            Object[] row = new Object[7];
            row[0] = metric;
            for (int i = 0; i < results.size(); i++) {
                BenchmarkResult r = results.get(i);
                int col = structureColumn(r.structureName);
                if (col < 1) continue;
                row[col] = switch (metric) {
                    case "Tiempo Inserción"           -> r.formatTime(r.insertTimeNs);
                    case "Tiempo Búsqueda"            -> r.formatTime(r.searchTimeNs);
                    case "Tiempo Borrado"             -> r.formatTime(r.deleteTimeNs);
                    case "Comparaciones Inserción"    -> r.formatComparisons(r.insertComparisons);
                    case "Comparaciones Búsqueda"     -> r.formatComparisons(r.searchComparisons);
                    case "Comparaciones Borrado"      -> r.formatComparisons(r.deleteComparisons);
                    case "O() Inserción"              -> r.insertComplexity;
                    case "O() Búsqueda"               -> r.searchComplexity;
                    case "O() Borrado"                -> r.deleteComplexity;
                    case "Altura / Tamaño"            -> String.valueOf(r.heightOrSize);
                    default -> "";
                };
            }
            // Fill empty cells with —
            for (int c = 1; c < 7; c++) if (row[c] == null) row[c] = "—";
            tableModel.addRow(row);
        }

        // Footer row
        tableModel.addRow(new Object[]{"N=" + cfg.N + " W=" + cfg.W + " R=" + cfg.R, "", "", "", "", "", ""});
    }

    private int structureColumn(String name) {
        return switch (name) {
            case "BST"         -> 1;
            case "AVL"         -> 2;
            case "Splay"       -> 3;
            case "Red-Black"   -> 4;
            case "Array"       -> 5;
            case "Linked List" -> 6;
            default -> -1;
        };
    }

    private void exportCSV() {
        if (lastResults == null) {
            showInfo("Run the benchmark first.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save CSV");
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fc.setSelectedFile(new File("benchmark_results.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String path = fc.getSelectedFile().getAbsolutePath();
                if (!path.endsWith(".csv")) path += ".csv";
                CSVExporter.saveToFile(path, lastResults, lastConfig.N, lastConfig.W, lastConfig.R);
                log("CSV exported to: " + path);
                setStatus("CSV saved to " + path);
            } catch (Exception ex) {
                log("Export error: " + ex.getMessage());
            }
        }
    }

    private void openVisualizer() {
        JDialog vizDialog = new JDialog(this, "🌳 Visualizador de Árboles", false);
        vizDialog.setSize(1000, 600);
        vizDialog.setLocationRelativeTo(this);
        vizDialog.getContentPane().setBackground(new Color(30, 30, 42));
        vizDialog.setLayout(new BorderLayout(6, 6));

        // Selectors
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        topPanel.setBackground(new Color(40, 42, 58));
        String[] treeTypes = {"BST", "AVL", "Splay", "Red-Black"};
        JComboBox<String> comboA = new JComboBox<>(treeTypes);
        JComboBox<String> comboB = new JComboBox<>(treeTypes);
        comboB.setSelectedIndex(1);
        styleCombo(comboA); styleCombo(comboB);

        topPanel.add(vizLabel("Árbol A:"));
        topPanel.add(comboA);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(vizLabel("Árbol B:"));
        topPanel.add(comboB);

        JButton refreshBtn = new JButton("🔄 Actualizar");
        styleButton2(refreshBtn, new Color(55, 130, 220));
        topPanel.add(refreshBtn);
        vizDialog.add(topPanel, BorderLayout.NORTH);

        TreeVisualizerPanel vizPanel = new TreeVisualizerPanel();
        vizPanel.setTrees(lastBST, lastAVL, lastSplay, lastRB);
        vizPanel.setTreeTypes("BST", "AVL");
        vizDialog.add(vizPanel, BorderLayout.CENTER);

        JLabel note = new JLabel("  Nodos azules = negro (BST/AVL/Splay), nodos rojos = RED (Red-Black)");
        note.setForeground(new Color(160, 160, 200));
        note.setFont(new Font("SansSerif", Font.ITALIC, 11));
        vizDialog.add(note, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> {
            vizPanel.setTrees(lastBST, lastAVL, lastSplay, lastRB);
            vizPanel.setTreeTypes((String) comboA.getSelectedItem(), (String) comboB.getSelectedItem());
        });

        vizDialog.setVisible(true);
    }

    private void openSequence() {
        if (lastInsertionOrder == null) {
            showInfo("Run the benchmark first to load keys.");
            return;
        }
        SequencePanel seqPanel = new SequencePanel(this);
        seqPanel.setKeys(lastInsertionOrder);
        seqPanel.setVisible(true);
    }

    private void loadSearchFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Cargar archivo de búsquedas");
        fc.setFileFilter(new FileNameExtensionFilter("Text/CSV files", "txt", "csv"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(fc.getSelectedFile().toPath()));
                String[] parts = content.split("[,\\s\\n\\r]+");
                StringBuilder keys = new StringBuilder();
                for (String part : parts) {
                    part = part.trim();
                    if (!part.isEmpty()) {
                        try {
                            Integer.parseInt(part);
                            if (keys.length() > 0) keys.append(" ");
                            keys.append(part);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                manualKeysArea.setText(keys.toString());
                manualSearchRadio.setSelected(true);
                log("Archivo cargado: " + fc.getSelectedFile().getName() + " (" + keys.toString().split("\\s+").length + " claves)");
                setStatus("Búsquedas cargadas del archivo.");
            } catch (Exception ex) {
                showError("Error al cargar archivo: " + ex.getMessage());
            }
        }
    }

    // ---- Helpers ----

    private BenchmarkEngine.Config buildConfig() {
        try {
            BenchmarkEngine.Config cfg = new BenchmarkEngine.Config();
            cfg.N = Integer.parseInt(nField.getText().trim());
            cfg.seed = Long.parseLong(seedField.getText().trim());
            cfg.W = Integer.parseInt(wField.getText().trim());
            cfg.R = Integer.parseInt(rField.getText().trim());
            cfg.searchCount = Integer.parseInt(searchCountField.getText().trim());

            cfg.useBST   = bstCheck.isSelected();
            cfg.useAVL   = avlCheck.isSelected();
            cfg.useSplay = splayCheck.isSelected();
            cfg.useRB    = rbCheck.isSelected();
            cfg.useArray = arrayCheck.isSelected();
            cfg.useList  = listCheck.isSelected();

            if (manualSearchRadio.isSelected()) {
                String raw = manualKeysArea.getText().trim();
                if (!raw.isEmpty()) {
                    String[] parts = raw.split("[,\\s]+");
                    int[] keys = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) keys[i] = Integer.parseInt(parts[i].trim());
                    cfg.searchKeys = keys;
                }
            }

            if (cfg.W < 0) throw new Exception("W debe ser >= 0");
            if (cfg.R < 1) throw new Exception("R debe ser >= 1");
            if (cfg.N < 1) throw new Exception("N debe ser >= 1");
            return cfg;
        } catch (NumberFormatException e) {
            showError("Invalid number format in config fields.");
            return null;
        } catch (Exception e) {
            showError(e.getMessage());
            return null;
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---- UI factory helpers ----

    private JTextField addField(JPanel panel, String labelText, String defaultValue) {
        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(new Color(180, 185, 210));
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);

        JTextField field = new JTextField(defaultValue);
        field.setBackground(new Color(50, 52, 72));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Monospaced", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 85, 115)),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(field);
        panel.add(Box.createVerticalStrut(4));
        return field;
    }

    private JCheckBox addCheck(JPanel panel, String label) {
        JCheckBox cb = new JCheckBox(label, true);
        cb.setBackground(new Color(38, 38, 52));
        cb.setForeground(new Color(200, 210, 240));
        cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cb);
        return cb;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(120, 160, 220));
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(6, 0, 2, 0));
        return l;
    }

    private JButton bigButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleRadio(JRadioButton rb) {
        rb.setBackground(new Color(38, 38, 52));
        rb.setForeground(new Color(200, 210, 240));
        rb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rb.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(new Color(50, 55, 75));
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    private JLabel vizLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

    private void styleButton2(JButton btn, Color bg) {
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
    }
}



