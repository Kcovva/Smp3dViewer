/*
 * Matrix3x3
 * Клас для роботи з матрицями 3х3, які використовуються для обертання 3D координат
 */

public class Matrix3x3 {
    private final double[][] m; // Поточна матриця

    // Конструктор
    public Matrix3x3(double[][] m) {
        this.m = m;
    }

    // Метод повертає матрицю, яка повернута на кут angle відносно осі X
    public static Matrix3x3 rotationX(double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Matrix3x3(new double[][] {
            {1, 0, 0}, {0, c, -s}, {0, s, c}
        });
    }

    // Метод повертає матрицю, яка повернута на кут angle відносно осі Y
    public static Matrix3x3 rotationY(double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Matrix3x3(new double[][] {
            {c, 0, s}, {0, 1, 0}, {-s, 0, c}
        });
    }

    // Метод повертає матрицю, яка повернута на кут angle відносно осі Z
    public static Matrix3x3 rotationZ(double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Matrix3x3(new double[][] {
            {c, -s, 0}, {s, c, 0}, {0, 0, 1}
        });
    }

    // Метод множення збереженої і зовнішньої матриць
    public Matrix3x3 multiply(Matrix3x3 other) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                for (int k = 0; k < 3; k++)
                    r[i][j] += this.m[i][k] * other.m[k][j];
        return new Matrix3x3(r);
    }

    // Метод виконує обертання координат вказаної точки
    public Point3D applyTo(Point3D p) {
        double x = m[0][0] * p.x + m[0][1] * p.y + m[0][2] * p.z;
        double y = m[1][0] * p.x + m[1][1] * p.y + m[1][2] * p.z;
        double z = m[2][0] * p.x + m[2][1] * p.y + m[2][2] * p.z;
        return new Point3D(x, y, z);
    }
}
