package MainForm;

import Model.Model;

public class MainController {
    private MainForm view;
    private Model model;
    IViewController curViewControl;
    IViewController tableControl;
    IViewController imageControl;

    public MainController(Model model) {
        this.model = model;
        view = new MainForm(this, model);

        tableControl = new TableViewController(view, model);
        imageControl = new ImageViewController(view, model);
        curViewControl = tableControl;

        view.createView();
        view.createControls();
    }

    public void startVer() {
        String path = view.getFileToOpen(".xmi", "xmi");
        if (path==null) return;
        if (path.isEmpty()) {
            view.showMessage("Выберите файл");
            return;
        }
        // TODO: просим картинку
        String pathImage = "";
        model.startVerification(path);
        curViewControl.clear();
        curViewControl.draw();
    }

    public void changeViewController(int index){
        curViewControl.hide();
        if (index==0) {
            curViewControl=tableControl;
        }
        else {
            curViewControl = imageControl;
        }
        curViewControl.reveal();
    }

    public void writeFile(String path){
        if(path==null) return;
        if(path.isEmpty()) {
            view.showMessage("Введите имя файла");
            return;
        }
        model.writeFile(path);
        view.showMessage("Ошибки сохранены в файле '" +path+"'");
    }
}
