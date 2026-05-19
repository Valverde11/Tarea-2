/**
 * Runs the benchmark for all selected structures.
 * Protocol:
 *  1. W warmup rounds (not counted)
 *  2. R measured rounds → averaged
 *  Phases per round: (1) insert → (2) search → (3) delete (except RB)
 *  Same insertion sequence (from seed), same search keys, same deletion order.
 *
 * NOTE: No java.util.* used. Custom structures only.
 */
public class BenchmarkEngine {

    public static class Config {
        public int N = 1000;
        public long seed = 42;
        public int W = 2;
        public int R = 5;
        public int[] searchKeys = null; // null = auto-generate
        public int searchCount = 100;

        // Active structures
        public boolean useBST   = true;
        public boolean useAVL   = true;
        public boolean useSplay = true;
        public boolean useRB    = true;
        public boolean useArray = true;
        public boolean useList  = true;
    }

    public interface ProgressListener {
        void onProgress(String message);
    }

    // Simple custom result list (no java.util.List)
    public static class ResultList {
        private BenchmarkResult[] data = new BenchmarkResult[16];
        private int size = 0;

        public void add(BenchmarkResult r) {
            if (size == data.length) {
                BenchmarkResult[] nd = new BenchmarkResult[data.length * 2];
                for (int i = 0; i < size; i++) nd[i] = data[i];
                data = nd;
            }
            data[size++] = r;
        }
        public BenchmarkResult get(int i) { return data[i]; }
        public int size() { return size; }
    }

    // ---- Simple LCG random (no java.util.Random) ----
    private static class SimpleRandom {
        private long state;
        SimpleRandom(long seed) { this.state = seed ^ 0x5DEECE66DL; }
        int nextInt(int bound) {
            state = (state * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
            int bits = (int)(state >>> 17);
            int val = bits % bound;
            return val < 0 ? val + bound : val;
        }
    }

    private final Config cfg;
    private int[] insertionOrder;
    private int[] searchKeys;
    private ProgressListener listener;

    public BenchmarkEngine(Config cfg) {
        this.cfg = cfg;
    }

    public void setProgressListener(ProgressListener l) {
        this.listener = l;
    }

    private void progress(String msg) {
        if (listener != null) listener.onProgress(msg);
    }

    /** Pre-generates insertion order and search keys from seed */
    public void prepare() {
        SimpleRandom rng = new SimpleRandom(cfg.seed);

        // Generate N distinct random integers using a custom open-addressing set
        int capacity = cfg.N * 4;
        int[] setKeys  = new int[capacity];
        boolean[] used = new boolean[capacity];
        insertionOrder = new int[cfg.N];
        int count = 0;
        while (count < cfg.N) {
            int v = rng.nextInt(cfg.N * 10) + 1;
            int h = (v & 0x7FFFFFFF) % capacity;
            while (used[h]) {
                if (setKeys[h] == v) { h = -1; break; } // duplicate
                h = (h + 1) % capacity;
            }
            if (h == -1) continue; // duplicate, skip
            setKeys[h] = v;
            used[h] = true;
            insertionOrder[count++] = v;
        }

        if (cfg.searchKeys != null) {
            searchKeys = cfg.searchKeys;
        } else {
            searchKeys = new int[cfg.searchCount];
            for (int j = 0; j < cfg.searchCount; j++) {
                if (j % 2 == 0) {
                    searchKeys[j] = insertionOrder[rng.nextInt(cfg.N)];
                } else {
                    searchKeys[j] = rng.nextInt(cfg.N * 10) + cfg.N * 10 + 1;
                }
            }
        }
    }

    public int[] getInsertionOrder() { return insertionOrder; }
    public int[] getSearchKeys()     { return searchKeys; }

    public ResultList run() {
        prepare();

        int totalRounds = cfg.W + cfg.R;

        long[] insertTime = new long[6];
        long[] searchTime = new long[6];
        long[] deleteTime = new long[6];
        long[] insertComp = new long[6];
        long[] searchComp = new long[6];
        long[] deleteComp = new long[6];

        for (int round = 0; round < totalRounds; round++) {
            boolean measure = (round >= cfg.W);
            progress("Round " + (round + 1) + "/" + totalRounds + (measure ? " [measured]" : " [warmup]"));

            if (cfg.useBST)   runBST(  measure, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, 0);
            if (cfg.useAVL)   runAVL(  measure, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, 1);
            if (cfg.useSplay) runSplay(measure, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, 2);
            if (cfg.useRB)    runRB(   measure, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, 3);
            if (cfg.useArray) runArray(measure, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, 4);
            if (cfg.useList)  runList( measure, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, 5);
        }

        ResultList results = new ResultList();
        if (cfg.useBST)   results.add(buildResult("BST",         0, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, false, "O(log n)*", "O(log n)*", "O(log n)*"));
        if (cfg.useAVL)   results.add(buildResult("AVL",         1, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, false, "O(log n)",  "O(log n)",  "O(log n)"));
        if (cfg.useSplay) results.add(buildResult("Splay",       2, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, false, "O(log n)*", "O(log n)*", "O(log n)*"));
        if (cfg.useRB)    results.add(buildResult("Red-Black",   3, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, true,  "O(log n)",  "O(log n)",  "N/A"));
        if (cfg.useArray) results.add(buildResult("Array",       4, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, false, "O(1)*",     "O(n)",      "O(n)"));
        if (cfg.useList)  results.add(buildResult("Linked List", 5, insertTime, searchTime, deleteTime, insertComp, searchComp, deleteComp, false, "O(n)",      "O(n)",      "O(n)"));

        return results;
    }

    private BenchmarkResult buildResult(String name, int idx,
            long[] it, long[] st, long[] dt,
            long[] ic, long[] sc, long[] dc,
            boolean noDelete,
            String iC, String sC, String dC) {
        BenchmarkResult r = new BenchmarkResult(name);
        r.insertTimeNs     = it[idx] / cfg.R;
        r.searchTimeNs     = st[idx] / cfg.R;
        r.deleteTimeNs     = noDelete ? -1 : dt[idx] / cfg.R;
        r.insertComparisons = ic[idx] / cfg.R;
        r.searchComparisons = sc[idx] / cfg.R;
        r.deleteComparisons = noDelete ? -1 : dc[idx] / cfg.R;
        r.insertComplexity = iC;
        r.searchComplexity = sC;
        r.deleteComplexity = dC;
        r.heightOrSize = cfg.N;
        return r;
    }

    // ---- per-structure runners ----

    private void runBST(boolean measure, long[] it, long[] st, long[] dt, long[] ic, long[] sc, long[] dc, int idx) {
        BST bst = new BST();
        bst.resetComparisons();
        long t0 = System.nanoTime();
        for (int v : insertionOrder) bst.insert(v);
        long tIns = System.nanoTime() - t0; long cIns = bst.getComparisons();

        bst.resetComparisons(); t0 = System.nanoTime();
        for (int v : searchKeys) bst.search(v);
        long tSrch = System.nanoTime() - t0; long cSrch = bst.getComparisons();

        bst.resetComparisons(); t0 = System.nanoTime();
        for (int v : insertionOrder) bst.delete(v);
        long tDel = System.nanoTime() - t0; long cDel = bst.getComparisons();

        if (measure) { it[idx]+=tIns; st[idx]+=tSrch; dt[idx]+=tDel; ic[idx]+=cIns; sc[idx]+=cSrch; dc[idx]+=cDel; }
    }

    private void runAVL(boolean measure, long[] it, long[] st, long[] dt, long[] ic, long[] sc, long[] dc, int idx) {
        AVLTree avl = new AVLTree();
        avl.resetComparisons();
        long t0 = System.nanoTime();
        for (int v : insertionOrder) avl.insert(v);
        long tIns = System.nanoTime() - t0; long cIns = avl.getComparisons();

        avl.resetComparisons(); t0 = System.nanoTime();
        for (int v : searchKeys) avl.search(v);
        long tSrch = System.nanoTime() - t0; long cSrch = avl.getComparisons();

        avl.resetComparisons(); t0 = System.nanoTime();
        for (int v : insertionOrder) avl.delete(v);
        long tDel = System.nanoTime() - t0; long cDel = avl.getComparisons();

        if (measure) { it[idx]+=tIns; st[idx]+=tSrch; dt[idx]+=tDel; ic[idx]+=cIns; sc[idx]+=cSrch; dc[idx]+=cDel; }
    }

    private void runSplay(boolean measure, long[] it, long[] st, long[] dt, long[] ic, long[] sc, long[] dc, int idx) {
        SplayTree splay = new SplayTree();
        splay.resetComparisons();
        long t0 = System.nanoTime();
        for (int v : insertionOrder) splay.insert(v);
        long tIns = System.nanoTime() - t0; long cIns = splay.getComparisons();

        splay.resetComparisons(); t0 = System.nanoTime();
        for (int v : searchKeys) splay.search(v);
        long tSrch = System.nanoTime() - t0; long cSrch = splay.getComparisons();

        splay.resetComparisons(); t0 = System.nanoTime();
        for (int v : insertionOrder) splay.delete(v);
        long tDel = System.nanoTime() - t0; long cDel = splay.getComparisons();

        if (measure) { it[idx]+=tIns; st[idx]+=tSrch; dt[idx]+=tDel; ic[idx]+=cIns; sc[idx]+=cSrch; dc[idx]+=cDel; }
    }

    private void runRB(boolean measure, long[] it, long[] st, long[] dt, long[] ic, long[] sc, long[] dc, int idx) {
        RedBlackTree rb = new RedBlackTree();
        rb.resetComparisons();
        long t0 = System.nanoTime();
        for (int v : insertionOrder) rb.insert(v);
        long tIns = System.nanoTime() - t0; long cIns = rb.getComparisons();

        rb.resetComparisons(); t0 = System.nanoTime();
        for (int v : searchKeys) rb.search(v);
        long tSrch = System.nanoTime() - t0; long cSrch = rb.getComparisons();

        if (measure) { it[idx]+=tIns; st[idx]+=tSrch; ic[idx]+=cIns; sc[idx]+=cSrch; }
    }

    private void runArray(boolean measure, long[] it, long[] st, long[] dt, long[] ic, long[] sc, long[] dc, int idx) {
        CustomArray arr = new CustomArray(cfg.N + 16);
        arr.resetComparisons();
        long t0 = System.nanoTime();
        for (int v : insertionOrder) arr.insert(v);
        long tIns = System.nanoTime() - t0; long cIns = arr.getComparisons();

        arr.resetComparisons(); t0 = System.nanoTime();
        for (int v : searchKeys) arr.search(v);
        long tSrch = System.nanoTime() - t0; long cSrch = arr.getComparisons();

        arr.resetComparisons(); t0 = System.nanoTime();
        for (int v : insertionOrder) arr.delete(v);
        long tDel = System.nanoTime() - t0; long cDel = arr.getComparisons();

        if (measure) { it[idx]+=tIns; st[idx]+=tSrch; dt[idx]+=tDel; ic[idx]+=cIns; sc[idx]+=cSrch; dc[idx]+=cDel; }
    }

    private void runList(boolean measure, long[] it, long[] st, long[] dt, long[] ic, long[] sc, long[] dc, int idx) {
        CustomLinkedList list = new CustomLinkedList();
        list.resetComparisons();
        long t0 = System.nanoTime();
        for (int v : insertionOrder) list.insert(v);
        long tIns = System.nanoTime() - t0; long cIns = list.getComparisons();

        list.resetComparisons(); t0 = System.nanoTime();
        for (int v : searchKeys) list.search(v);
        long tSrch = System.nanoTime() - t0; long cSrch = list.getComparisons();

        list.resetComparisons(); t0 = System.nanoTime();
        for (int v : insertionOrder) list.delete(v);
        long tDel = System.nanoTime() - t0; long cDel = list.getComparisons();

        if (measure) { it[idx]+=tIns; st[idx]+=tSrch; dt[idx]+=tDel; ic[idx]+=cIns; sc[idx]+=cSrch; dc[idx]+=cDel; }
    }
}
