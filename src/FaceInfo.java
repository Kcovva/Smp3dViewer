/*
 * FaceInfo
 * Клас для зберігання інформації про грань
 * Використовується на етапі сортування граней від більш віддалених до найбілш наближених
 */

public class FaceInfo {
    public int[] indices; // Список індексів вершин грані
    public double depth; // Середня глибина грані по осі Z
    public double brightness; // Яскравість грані з врахуванням освітлення
    public int index; // Індекс грані (для налагодження)

    // Конструктор
    public FaceInfo(int[] indices, double depth, double brightness, int index) {
        this.indices = indices;
        this.depth = depth;
        this.brightness = brightness;
        this.index = index;
    }
}
