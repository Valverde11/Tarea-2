



import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Exports benchmark results to CSV format.
 */
public class CSVExporter {

    public static String toCSV(List<BenchmarkResult> results, int N, int W, int R) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // Header
        pw.print("Metric");
        for (BenchmarkResult r : results) pw.print("," + r.structureName);
        pw.println();

        // Insert time
        pw.print("Insert Time");
        for (BenchmarkResult r : results) pw.print("," + r.formatTime(r.insertTimeNs));
        pw.println();

        // Search time
        pw.print("Search Time");
        for (BenchmarkResult r : results) pw.print("," + r.formatTime(r.searchTimeNs));
        pw.println();

        // Delete time
        pw.print("Delete Time");
        for (BenchmarkResult r : results) pw.print("," + r.formatTime(r.deleteTimeNs));
        pw.println();

        // Insert comparisons
        pw.print("Insert Comparisons");
        for (BenchmarkResult r : results) pw.print("," + r.formatComparisons(r.insertComparisons));
        pw.println();

        // Search comparisons
        pw.print("Search Comparisons");
        for (BenchmarkResult r : results) pw.print("," + r.formatComparisons(r.searchComparisons));
        pw.println();

        // Delete comparisons
        pw.print("Delete Comparisons");
        for (BenchmarkResult r : results) pw.print("," + r.formatComparisons(r.deleteComparisons));
        pw.println();

        // Insert complexity
        pw.print("Insert O()");
        for (BenchmarkResult r : results) pw.print("," + r.insertComplexity);
        pw.println();

        // Search complexity
        pw.print("Search O()");
        for (BenchmarkResult r : results) pw.print("," + r.searchComplexity);
        pw.println();

        // Delete complexity
        pw.print("Delete O()");
        for (BenchmarkResult r : results) pw.print("," + r.deleteComplexity);
        pw.println();

        // Height/size
        pw.print("Height/Size");
        for (BenchmarkResult r : results) pw.print("," + r.heightOrSize);
        pw.println();

        pw.println();
        pw.println("N=" + N + ",W=" + W + ",R=" + R);

        return sw.toString();
    }

    public static void saveToFile(String path, List<BenchmarkResult> results, int N, int W, int R) throws IOException {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write(toCSV(results, N, W, R));
        }
    }
}
