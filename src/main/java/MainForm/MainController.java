package MainForm;

import Model.Model;

public class MainController {
    private MainForm view;
    private Model model;
    IView curViewControl;
    IView tableControl;
    IView imageControl;

    public MainController(Model model) {
        this.model = model;
        view = new MainForm(this, model);

        tableControl = new TableView(view, model);
        // TODO: imageView = new ImageView(view, model);

        view.createView();
        view.createControls();
    }

    public void startVer() {
        String path = view.getFileToOpen();
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
            curViewControl = tableControl;
            //TODO:curViewControl=imageControl;
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
