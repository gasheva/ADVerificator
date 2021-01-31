package MainForm;

import Model.Model;

public class ImageViewController implements IViewController{
    private MainForm view;
    private Model model;


    public ImageViewController(MainForm view, Model model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void hide() {
        view.setViewDiagramVisible(false);
    }

    @Override
    public void reveal() {
        view.setViewDiagramVisible(true);
    }

    @Override
    public void draw(String path) {
        view.drawImage(path);
    }

    @Override
    public void clear() {
        //TODO
    }
}
