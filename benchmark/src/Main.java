import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Nimbus respeta setBackground/setForeground en todos los OS
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            BenchmarkFrame frame = new BenchmarkFrame();
            frame.setVisible(true);
        });
    }
}