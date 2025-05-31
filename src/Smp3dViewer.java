/*
 * Smp3dViewer
 * Основний клас програми
 */

import javax.swing.*;

public class Smp3dViewer extends JFrame {
    // Конструктор
    public Smp3dViewer() {
        // Налаштовуємо вікно програми
        setTitle("Simple 3D Viewer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new DrawPanel());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Точка входу в програму
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Smp3dViewer::new);
    }
}
