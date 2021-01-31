package MainForm;

import result.Mistakes;

/**
 * Интерфейс для контроллера отображения диаграммы (таблица или изображение)
 */
public interface IViewController {
    void hide();
    void reveal();
    void draw(String path);
    void clear();
    // метод для реагирования на нажатия на ошибку в таблице у каждого свой
}
