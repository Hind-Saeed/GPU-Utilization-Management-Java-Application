package gpumanagement;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("GPU Utilization Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);
            frame.add(new JLabel("GPU Utilization Management",
                    SwingConstants.CENTER));
            frame.setVisible(true);
        });
    }
}