
public class BenchmarkResult {
    public final String structureName;

    // Times in nanoseconds (averaged over R runs)
    public long insertTimeNs;
    public long searchTimeNs;
    public long deleteTimeNs;  // -1 means N/A (Red-Black delete)

    // Comparisons (averaged over R runs)
    public long insertComparisons;
    public long searchComparisons;
    public long deleteComparisons; // -1 means N/A

    // Height or size
    public int heightOrSize;

    // Theoretical complexities
    public String insertComplexity;
    public String searchComplexity;
    public String deleteComplexity;

    public BenchmarkResult(String structureName) {
        this.structureName = structureName;
        this.deleteTimeNs = -1;
        this.deleteComparisons = -1;
    }

    public String formatTime(long ns) {
        if (ns < 0) return "N/A";
        if (ns < 1_000) return ns + " ns";
        if (ns < 1_000_000) return String.format("%.2f µs", ns / 1_000.0);
        return String.format("%.2f ms", ns / 1_000_000.0);
    }

    public String formatComparisons(long c) {
        if (c < 0) return "N/A";
        return String.valueOf(c);
    }
}
