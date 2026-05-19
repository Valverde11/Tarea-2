import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Exports benchmark results to CSV format.
 * No java.util.* used.
 */
public class CSVExporter {

    public static String toCSV(BenchmarkEngine.ResultList results, int N, int W, int R) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.print("Metric");
        for (int i = 0; i < results.size(); i++) pw.print("," + results.get(i).structureName);
        pw.println();

        pw.print("Insert Time");
        for (int i = 0; i < results.size(); i++) pw.print("," + results.get(i).formatTime(results.get(i).insertTimeNs));
        pw.println();

        pw.print("Search Time");
        for (int i = 0; i < results.size(); i++) pw.print("," + results.get(i).formatTime(results.get(i).searchTimeNs));
        pw.println();

        pw.print("Delete Time");
        for (int i = 0; i < results.size(); i++) pw.print("," + results.get(i).formatTime(results.get(i).deleteTimeNs));
        pw.println();

        pw.print("Insert Comparisons");
        for (int i = 0; i < results.size(); i++) pw.print("," + results.get(i).formatComparisons(results.get(i).insertComparisons));
        pw.println();

        pw.print("Search Comparisons");
        for (int i = 0; i < results.size(); i++) pw.print("," + results.get(i).formatComparisons(results.get(i).searchComparisons));
        pw.println();

        pw.print("Delete Comparisons");
        for (int i = 0; i < results.size(); i++) pw.print("," + results.get(i).formatComparisons(results.get(i).deleteComparisons));
        pw.println();

        pw.print("Insert O()");
        for (int i = 0; i < results.size(); i++) pw.print("," + results.get(i).insertComplexity);
        pw.println();

        pw.print("Search O()");
        for (int i = 0; i < results.size(); i++) pw.print("," + results.get(i).searchComplexity);
        pw.println();

        pw.print("Delete O()");
        for (int i = 0; i < results.size(); i++) pw.print("," + results.get(i).deleteComplexity);
        pw.println();

        pw.print("Height/Size");
        for (int i = 0; i < results.size(); i++) pw.print("," + results.get(i).heightOrSize);
        pw.println();

        pw.println();
        pw.println("N=" + N + ",W=" + W + ",R=" + R);

        return sw.toString();
    }

    public static void saveToFile(String path, BenchmarkEngine.ResultList results, int N, int W, int R) throws IOException {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write(toCSV(results, N, W, R));
        }
    }
}
