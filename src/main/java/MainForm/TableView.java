package MainForm;

import Model.Model;
import result.Mistakes;

public class TableView implements IView{
    private MainForm view;
    private Model model;

    public TableView(MainForm view, Model model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void hide() {
        view.setViewTableVisible(false);
    }

    @Override
    public void reveal() {
        view.setViewTableVisible(true);
    }

    @Override
    public void draw() {
        view.createElementsModel(new String[]{"id", "Тип", "Описание", "Следующие", "Предыдущие"});
        view.fillTable();
    }

    @Override
    public void clear() {
        view.createElementsModel(new String[]{"id", "Тип", "Описание", "Следующие", "Предыдущие"});
    }
}
