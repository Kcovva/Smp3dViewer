/*
 * Point3D
 * Клас для зберігання 3D координат однієї точки
 */

import java.awt.*;

public class Point3D {
    double x, y, z; // Координати точки

    // Конструктор
    public Point3D(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    // Метод для виконання ортогональної чи перспективної проекції точки з 3D в 2D простір
    // 2D простір обмежується шириною width та висотою height вікна програми
    // scale задає коефіцієнт збільшення/зменшення координат
    // perspective вмикає перспективну проекцію замість ортогональної
    public Point project(int width, int height, double scale, boolean perspective) {
        double px = x, py = y;
        if (perspective) {
            // Обчислюємо коефіцієнт зміни координат в залежності від віддаленості по осі Z
            double factor = 2.0 / (Math.max(z, 0.1) + 5);
            px *= factor;
            py *= factor;
        }
        // Розміщуємо точки відносно центру вікна
        int sx = (int) (px * scale + width / 2.0);
        int sy = (int) (-py * scale + height / 2.0);
        return new Point(sx, sy);
    }

    // Перетворює координати на масив з трьох значень
    public double[] toArray() {
        return new double[] { x, y, z };
    }

    // Застосовує до координат коефіцієнт збільшення/зменшення
    public Point3D scale(double s) {
        return new Point3D(x * s, y * s, z * s);
    }
}
