/*
 * ShapeFactory
 * Клас для створення і зберігання даних про одну з фігур
 */

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

class ShapeFactory {
    // Перелік фігур
    public enum ShapeType {
        CUBE, PYRAMID, TETRAHEDRON, OCTAHEDRON, SPHERE, TORUS, SURFACE
    }

    // Допоміжний клас для зберігання даних про фігуру
    public static class ShapeData {
        // Тип фігури
        public ShapeType type;
        // Список вершин фігури. Кожна вершина зберігає свої 3D координати
        public List<Point3D> vertices;
        // Список граней фігури. Кожна грань зберігає індекси вершин зі списку вершин
        public List<int[]> faces;
        // Список ребер фігури. Кожне ребро зберігає індекси двох вершин зі списку вершин
        public List<Edge> edges;

        // Конструктор
        public ShapeData(ShapeType type, List<Point3D> vertices, List<int[]> faces, List<Edge> edges) {
            this.type = type;
            this.vertices = vertices;
            this.faces = faces;
            this.edges = edges;
        }
    }

    private static ShapeData currentShape; // дані поточної фігури

    // Метод для генерації фігури бажаного типу
    public static ShapeData generate(ShapeType type) {
        // Якщо тип не вказано, виконується генерація наступної за списком фігури
        if (type == null) {
            ShapeType current = currentShape != null ? currentShape.type : ShapeType.CUBE;
            type = ShapeType.values()[(current.ordinal() + 1) % ShapeType.values().length];
        }

        // Генерація фігури в залежності від типу
        switch (type) {
            case CUBE -> generateCube();
            case PYRAMID -> generatePyramid();
            case TETRAHEDRON -> generateTetrahedron();
            case OCTAHEDRON -> generateOctahedron();
            case SPHERE -> generateSphere(30, 30, 1.8);
            case TORUS -> generateTorus(24, 12, 1.5, 0.5);
            case SURFACE -> generateSurface(100, 100, 0.05, 1.0);
        }

        return currentShape;
    }

    // Метод зберігнання типу, вершин та граней нової фігури
    // Дані ребер формуються на основі даних граней
    public static void setNewShape(ShapeType type, List<Point3D> vertices, List<int[]> faces) {
        Set<String> edgeSet = new HashSet<>(); // хеш для фільтрації дублювань ребер
        List<Edge> edges = new ArrayList<>(); // список унікальних ребер фігури

        // Цикл формування унікального списку ребер
        for (int[] face : faces) {
            for (int i = 0; i < face.length; i++) {
                int a = face[i];
                int b = face[(i + 1) % face.length];
                int min = Math.min(a, b);
                int max = Math.max(a, b);
                String key = min + ":" + max;
                if (edgeSet.add(key)) {
                    edges.add(new Edge(min, max));
                }
            }
        }
        currentShape = new ShapeData(type, vertices, faces, edges);
    }

    // Метод генерації КУБА
    public static void generateCube() {
        List<Point3D> vertices = Arrays.asList(
            new Point3D(-1, -1, -1), new Point3D(-1, -1, 1),
            new Point3D(-1, 1, -1), new Point3D(-1, 1, 1),
            new Point3D(1, -1, -1), new Point3D(1, -1, 1),
            new Point3D(1, 1, -1), new Point3D(1, 1, 1)
        );

        int[][] faces = {
            {0, 1, 3, 2}, {4, 6, 7, 5},
            {0, 4, 5, 1}, {2, 3, 7, 6},
            {1, 5, 7, 3}, {0, 2, 6, 4}
            
        };
                
        setNewShape(ShapeType.CUBE, vertices, Arrays.asList(faces));
    }

    // Метод генерації ПІРАМІДИ
    public static void generatePyramid() {
        List<Point3D> vertices = Arrays.asList(
            new Point3D(-1, -1, 0), new Point3D(1, -1, 0),
            new Point3D(1, 1, 0), new Point3D(-1, 1, 0),
            new Point3D(0, 0, 2)
        );

        int[][] faces = {
            {3, 2, 1, 0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}
        };

        setNewShape(ShapeType.PYRAMID, vertices, Arrays.asList(faces));
    }

    // Метод генерації ТЕТРАЕДРА
    public static void generateTetrahedron() {
        List<Point3D> vertices = Arrays.asList(
            new Point3D(1, 1, 1), new Point3D(-1, -1, 1),
            new Point3D(-1, 1, -1), new Point3D(1, -1, -1)
        );

        int[][] faces = {
            {0, 2, 1}, {0, 1, 3}, {1, 2, 3}, {2, 0, 3}
        };

        setNewShape(ShapeType.TETRAHEDRON, vertices, Arrays.asList(faces));
    }

    // Метод генерації ОКТАЕДРА
    public static void generateOctahedron() {
        List<Point3D> vertices = Arrays.asList(
            new Point3D(1, 0, 0), new Point3D(-1, 0, 0),
            new Point3D(0, 1, 0), new Point3D(0, -1, 0),
            new Point3D(0, 0, 1), new Point3D(0, 0, -1)
        );

        int[][] faces = {
            {0, 2, 4}, {2, 1, 4}, {1, 3, 4}, {3, 0, 4},
            {0, 5, 2}, {2, 5, 1}, {1, 5, 3}, {3, 5, 0}
        };

        setNewShape(ShapeType.OCTAHEDRON, vertices, Arrays.asList(faces));
    }

    // Метод генерації СФЕРИ
    public static void generateSphere(int latDiv, int lonDiv, double radius) {
        List<Point3D> vertices = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();

        // Верхній полюс
        vertices.add(new Point3D(0, 0, radius));

        // Генерація вершин
        for (int i = 1; i < latDiv; i++) {
            double theta = Math.PI * i / latDiv;
            for (int j = 0; j < lonDiv; j++) {
                double phi = 2 * Math.PI * j / lonDiv;
                double x = radius * Math.sin(theta) * Math.cos(phi);
                double y = radius * Math.sin(theta) * Math.sin(phi);
                double z = radius * Math.cos(theta);
                vertices.add(new Point3D(x, y, z));
            }
        }

        // Нижній полюс
        vertices.add(new Point3D(0, 0, -radius));

        // Грані для верхнього полюсу
        for (int j = 0; j < lonDiv; j++) {
            int a = 0;
            int b = j + 1;
            int c = (j + 1) % lonDiv + 1;
            faces.add(new int[] {a, b, c});
        }

        // Грані між широтами
        for (int i = 0; i < latDiv - 2; i++) {
            for (int j = 0; j < lonDiv; j++) {
                int a = 1 + i * lonDiv + j;
                int b = 1 + i * lonDiv + (j + 1) % lonDiv;
                int c = 1 + (i + 1) * lonDiv + (j + 1) % lonDiv;
                int d = 1 + (i + 1) * lonDiv + j;
                faces.add(new int[] {d, c, b, a});
            }
        }

        // Грані для нижнього полюсу
        int southPole = vertices.size() - 1;
        int offset = 1 + (latDiv - 2) * lonDiv;
        for (int j = 0; j < lonDiv; j++) {
            int a = offset + j;
            int b = offset + (j + 1) % lonDiv;
            faces.add(new int[] {a, southPole, b});
        }

        setNewShape(ShapeType.SPHERE, vertices, faces);
    }

    // Метод генерації ТОРА
    public static void generateTorus(int segU, int segV, double R, double r) {
        List<Point3D> vertices = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();

        // Генерація вершин
        for (int i = 0; i < segU; i++) {
            double u = 2 * Math.PI * i / segU;
            for (int j = 0; j < segV; j++) {
                double v = 2 * Math.PI * j / segV;
                double x = (R + r * Math.cos(v)) * Math.cos(u);
                double y = (R + r * Math.cos(v)) * Math.sin(u);
                double z = r * Math.sin(v);
                vertices.add(new Point3D(x, y, z));
            }
        }

        // Генерація граней
        for (int i = 0; i < segU; i++) {
            for (int j = 0; j < segV; j++) {
                int a = i * segV + j;
                int b = ((i + 1) % segU) * segV + j;
                int c = ((i + 1) % segU) * segV + (j + 1) % segV;
                int d = i * segV + (j + 1) % segV;
                faces.add(new int[] {a, b, c, d});
            }
        }

        setNewShape(ShapeType.TORUS, vertices, faces);
    }

    // Метод генерації ПОВЕРХНІ на основі синуса й косинуса
    public static void generateSurface(int w, int h, double spacing, double scale) {
        List<Point3D> vertices = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();

        // Генерація вершин
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double px = (x - w / 2.0) * spacing * scale;
                double py = (y - h / 2.0) * spacing * scale;
                double z = 0.5 * Math.sin(px * 2) * Math.cos(py * 2)
                                + 0.3 * Math.sin(px * 4) * Math.cos(py * 3); // псевдо-шум
                vertices.add(new Point3D(px, py, z));
            }
        }

        // Генерація граней
        for (int y = 0; y < h - 1; y++) {
            for (int x = 0; x < w - 1; x++) {
                int a = y * w + x;
                int b = a + 1;
                int c = a + w + 1;
                int d = a + w;
                faces.add(new int[] {a, b, c, d});
            }
        }

        setNewShape(ShapeType.SURFACE, vertices, faces);
    }
}