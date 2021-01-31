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

    public void startVer() {
        String path = view.getFileToOpen(".xmi", "xmi");
        if (path==null) return;
        if (path.isEmpty()) {
            view.showMessage("Выберите файл");
            return;
        }
        // TODO: если картинки нет
        String pathImage = view.getFileToOpen(".png", "png");

        if (pathImage!=null && pathImage.isEmpty()){
            view.showMessage("Выберите файл");
            return;
        }
        if (!model.readFile(path)){
            view.showMessage("Проблемы с чтением из файла");
            return;
        };

        if (pathImage!=null) {
            view.drawImage(pathImage);
        }
        model.startVerification(path);
        view.fillMistakesTable();

        view.fillElementsTable();

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

    public void changeViewController(int selectedIndex) {

    }
}
