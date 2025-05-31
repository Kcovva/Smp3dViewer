/*
 * DrawPanel
 * Клас що відповідає за візуалізацію та процес керування фігурою
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

class DrawPanel extends JPanel implements KeyListener, ActionListener {
    public enum ShapeView { WIREFRAME, POLYGONS, ILLUMINATED } // Тип відображення фігури
    public enum ProjectionType { ORTHOGONAL, PERSPECTIVE } // Тип проекції

    private final double rotationStep = Math.toRadians(3); // Крок обертання фігури (3 градуси на крок)
    private final double perspectiveScale = 250; // Початковий коефіцієнт наближення для перспективної проекції
    private final double orthographicScale = 100; // Початковий коефіцієнт наближення для ортогональної проекції
    private final Color vertexColor = new Color(255, 255, 0); // Колір вершин
    private final Color edgeColor = new Color(255, 204, 0); // Колір ребер
    private final Color faceColor = new Color(255, 255, 204); // Колір граней
    private final double[] lightDir = {0.5, 0.5, -1}; // Початкові координати джерела світла

    ShapeFactory.ShapeData currentShape; // Дані поточної фігури
    private double scale; // Поточний коефіцієнт наближення
    ShapeView shapeView = ShapeView.WIREFRAME; // Поточний тип відображення фігури
    ProjectionType projectionType = ProjectionType.ORTHOGONAL; // Поточний тип проекції
    private boolean autoRotating = true; // Ознака автообертання фігури
    private boolean showDebug = false; // Ознака виводу налагоджувальної інформації
    private Timer timer; // Таймер для автообертання
    private Matrix3x3 rotationMatrix; // Поточна матриця обертання фігури

    // Конструктор
    public DrawPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // Задаємо початкове обертання фігури
        rotationMatrix = Matrix3x3.rotationX(-Math.PI / 2).multiply(Matrix3x3.rotationY(-Math.PI / 4));

        // Генеруємо початкову фігуру
        currentShape = ShapeFactory.generate(ShapeFactory.ShapeType.CUBE);

        // В становлюємо коеф.наближення в залежності від типу проекції
        scale = projectionType == ProjectionType.PERSPECTIVE ? perspectiveScale : orthographicScale;

        // Запускаємо таймер автообертання фігури
        timer = new Timer(33, this); // ~30 FPS
        timer.start();
    }

    // Метод для обчислення нормалі до площини (грані), заданої трьома точками у 3D-просторі
    // Використовується для обчислення яскравості освітленої грані
    private double[] computeNormal(double[] a, double[] b, double[] c) {
        double[] u = { b[0] - a[0], b[1] - a[1], b[2] - a[2] };
        double[] v = { c[0] - a[0], c[1] - a[1], c[2] - a[2] };
        return normalize(new double[] {
            u[1]*v[2] - u[2]*v[1],
            u[2]*v[0] - u[0]*v[2],
            u[0]*v[1] - u[1]*v[0]
        });
    }

    // Метод приводить вектор до одиничної довжини, зберігаючи його напрямок.
    // Використовується для стабільних обчислень напрямку світла та нормалей.
    private double[] normalize(double[] v) {
        double len = Math.sqrt(dotProduct(v, v));
        if (len == 0) return new double[] {0, 0, 0};
        return new double[] { v[0]/len, v[1]/len, v[2]/len };
    }

    // Метод обчислює скалярний добуток (dot product) двох векторів
    private double dotProduct(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }    

    // Метод візуалізації фігури
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2 = (Graphics2D)g; // Отримуємо посилання на клас для малювання в 2D просторі

        List<Point3D> rotated = new ArrayList<>(); // Список вершин після обертання
        List<Point> projected = new ArrayList<>(); // Список вершин після проекції з 3D в 2D

        // Цикл обертання і 2D проеції  всіх вершин
        for (Point3D p : currentShape.vertices) {
            // Обертання чергової точки
            Point3D r = rotationMatrix.applyTo(p);
            // Додавання точки до списку повернутих
            rotated.add(r);
            // Виконання проекції кординат з 3D в 2D
            projected.add(r.project(getWidth(), getHeight(), scale,
                    projectionType == ProjectionType.PERSPECTIVE));
        }

        // Малювання граней для режимів POLYGONS та ILLUMINATED
        if ((shapeView != ShapeView.WIREFRAME) && (currentShape.faces != null)) {
            g2.setStroke(new BasicStroke(1));
            List<FaceInfo> faceList = new ArrayList<>();

            // Нормалізація вектору освітлення
            Point3D lightVec = new Point3D(lightDir[0], lightDir[1], lightDir[2]);
            double[] light = normalize(lightVec.toArray());

            // Цикл обробки граней
            for (int[] face : currentShape.faces) {
                // Отримання координат вже повернутих трьох вершин грані
                Point3D p0 = rotated.get(face[0]);
                Point3D p1 = rotated.get(face[1]);
                Point3D p2 = rotated.get(face[2]);
                // Обчислення нормалі для поточної грні
                double[] normal = computeNormal(p0.toArray(), p1.toArray(), p2.toArray());
                // Обчислення яскравості освітлення на основі векторів світля і нормалі
                double brightness = dotProduct(normalize(normal), light);

                // Приведення яскравості в межі 0.0 - 1.0
                brightness = Math.max(0, Math.min(1, brightness));

                // Обчислення середньої глибини по Z
                double avgZ = 0;
                for (int idx : face) {
                    avgZ += rotated.get(idx).z;
                }
                avgZ /= face.length;

                // Додавання даних про грань у список
                faceList.add(new FaceInfo(face, avgZ, brightness, faceList.size()));
            }
        
            // Сортування граней від найбільш віддалених до найбільш наближених
            faceList.sort((f1, f2) -> Double.compare(f2.depth, f1.depth));
        
            // Малювання граней на основі відсортованого по глибині списку
            for (FaceInfo f : faceList) {
                if (shapeView == ShapeView.ILLUMINATED) {
                    // Для режиму ILLUMINATED змінюємо колір в залежності від яскравості освітлення
                    int shadeR = (int) (f.brightness * faceColor.getRed());
                    int shadeG = (int) (f.brightness * faceColor.getGreen());
                    int shadeB = (int) (f.brightness * faceColor.getBlue());
                    g2.setColor(new Color(shadeR, shadeG, shadeB));
                }
                else {
                    // Для режиму POLYGONS малюємо грані одним кольором
                    g2.setColor(faceColor);
                }

                // Заповнюємо структуру для малювання заповненого полігону
                // також обчислюємо координати середини грані для виводу її номера при наолагодженні
                Polygon poly = new Polygon();
                Point center = new Point(0, 0);
                for (int idx : f.indices) {
                    Point p = projected.get(idx);
                    poly.addPoint(p.x, p.y);
                    center.x += p.x;
                    center.y += p.y;
                }

                // Малюємо отриманий полігон
                g2.fillPolygon(poly);

                // Виводимо номер грані при налагодженні
                if (showDebug) {
                    center.x /= f.indices.length;
                    center.y /= f.indices.length;
                    g2.setColor(Color.WHITE);
                    g2.drawString(String.valueOf(f.index), center.x, center.y);
                }

                // Для режиму POLYGONS додатково малюємо грані (контури полігона)
                if (shapeView != ShapeView.ILLUMINATED) {
                    g2.setColor(edgeColor);
                    g2.drawPolygon(poly);
                }
            }

            // Для режиму ILLUMINATED малюємо джерело світла
            if (shapeView == ShapeView.ILLUMINATED) {
                // Трохи 'наближаємо' джерело світла до нас
                Point3D lightPos = lightVec.scale(3);
                // Обчислюємо проекцію з 3d в 2D координати
                Point projectedLight = lightPos.project(getWidth(), getHeight(), scale,
                        projectionType == ProjectionType.PERSPECTIVE);
                // Малюємо круг за отриманими коорлинатами
                g2.setColor(Color.YELLOW);
                g2.fillOval(projectedLight.x - 5, projectedLight.y - 5, 10, 10);
            }
        }
        
        // Для режиму WIREFRAME малюємо ребра
        if (shapeView == ShapeView.WIREFRAME) {
            // Втсановлюємо ширину лінії та колір
            g2.setStroke(new BasicStroke(2));
            g2.setColor(edgeColor);

            // Малюємо грані
            for (Edge e : currentShape.edges) {
                Point p1 = projected.get(e.a);
                Point p2 = projected.get(e.b);
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            // Малюємо вершини
            g2.setColor(vertexColor);
            for (Point p : projected) {
                g2.fillOval(p.x - 3, p.y - 3, 6, 6);
            }

            // Виводимо номер вершин при налагодженні
            if (showDebug) {
                g2.setColor(Color.WHITE);
                for (int i = 0; i < projected.size(); i++) {
                    Point p = projected.get(i);
                    g2.drawString(String.valueOf(i), p.x + 5, p.y - 5);
                }            
            }
        }

        // Виводимо текст зпоточним станом програми та підказаками по керуванню
        g2.setColor(Color.WHITE);
        g2.drawString("Фігура: " + currentShape.type.name()
                        + ", тип відображення: " + shapeView.name()
                        + ", проекція:" + projectionType.name(), 10, 20);
        g2.drawString("Змінити фігуру — 'пробіл', тип відображення — V, проекція — P", 10, 40);
        g2.drawString("Автообертання — R, ручне обертання — A/D/W/S/Q/E"
                        + ", керування світлом — стрілки, масштаб — +/-", 10, 60);
    }

    @Override public void keyTyped(KeyEvent e) {}

    @Override public void keyReleased(KeyEvent e) {}

    // Метод обробки реакції на керування
    @Override public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            // Обертання поточної матриці у потрібному напрямку
            case KeyEvent.VK_S -> rotationMatrix = Matrix3x3.rotationX(-rotationStep).multiply(rotationMatrix);
            case KeyEvent.VK_W -> rotationMatrix = Matrix3x3.rotationX(rotationStep).multiply(rotationMatrix);
            case KeyEvent.VK_D -> rotationMatrix = Matrix3x3.rotationY(-rotationStep).multiply(rotationMatrix);
            case KeyEvent.VK_A -> rotationMatrix = Matrix3x3.rotationY(rotationStep).multiply(rotationMatrix);
            case KeyEvent.VK_E -> rotationMatrix = Matrix3x3.rotationZ(-rotationStep).multiply(rotationMatrix);
            case KeyEvent.VK_Q -> rotationMatrix = Matrix3x3.rotationZ(rotationStep).multiply(rotationMatrix);

            // Наближення/віддалення фігури
            case KeyEvent.VK_PLUS, KeyEvent.VK_EQUALS -> scale *= 1.1;
            case KeyEvent.VK_MINUS, KeyEvent.VK_UNDERSCORE -> scale /= 1.1;

            // Переміщення джерела світла по двох координатах
            case KeyEvent.VK_LEFT -> lightDir[0] = Math.max(-1, Math.min(1, lightDir[0] - 0.1));
            case KeyEvent.VK_RIGHT -> lightDir[0] = Math.max(-1, Math.min(1, lightDir[0] + 0.1));
            case KeyEvent.VK_UP -> lightDir[1] = Math.max(-1, Math.min(1, lightDir[1] + 0.1));
            case KeyEvent.VK_DOWN -> lightDir[1] = Math.max(-1, Math.min(1, lightDir[1] - 0.1));

            // Генерація наступної фігури
            case KeyEvent.VK_SPACE -> currentShape = ShapeFactory.generate(null);

            // Вмикання/вимикання автоматичного обертання фігури
            case KeyEvent.VK_R -> autoRotating = !autoRotating;
            // Вмикання/вимикання налагоджувальної інформації
            case KeyEvent.VK_BACK_QUOTE -> showDebug = !showDebug;
            // Зміна режиму відображення фігури
            case KeyEvent.VK_V -> shapeView = ShapeView.values()[(shapeView.ordinal() + 1) % ShapeView.values().length];
            // Зміна режиму проекції
            case KeyEvent.VK_P -> {
                projectionType = ProjectionType.values()[(projectionType.ordinal() + 1) % ProjectionType.values().length];
                scale = projectionType == ProjectionType.PERSPECTIVE ? perspectiveScale : orthographicScale;
            }
        }
        repaint(); // Сигналізуємо про необхідність оновлення вікна
    }

    // Метод що викликається таймером для автообертання фігури
    @Override
    public void actionPerformed(ActionEvent e) {
        // Якщо автообертання дозволено, обертаємо фігуру відносно осі Y
        if (autoRotating) {
            rotationMatrix = Matrix3x3.rotationY(rotationStep).multiply(rotationMatrix);
            repaint(); // Сигналізуємо про необхідність оновлення вікна
        }
    }  
} 
