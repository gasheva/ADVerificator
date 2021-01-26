package MainForm;

import Model.Model;

public class MainController {
    private MainForm view;
    private Model model;

    public MainController(Model model) {
        this.model = model;
        view = new MainForm(this, model);
        view.createView();
        view.createControls();
    }
}
