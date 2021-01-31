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
        String pathImage = view.getFileToOpen(".png", "png");

        if (pathImage!=null && pathImage.isEmpty()){
            view.showMessage("Выберите файл");
            return;
        }
        if (!model.readFile(path)){
            view.showMessage("Проблемы с чтением из файла");
            return;
        };

        tableControl.draw(path);
//        tableControl.hide();
        if (pathImage!=null) {
            imageControl.draw(pathImage);
//            imageControl.hide();
        }
//        curViewControl.reveal();
//        model.startVerification(path);
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
