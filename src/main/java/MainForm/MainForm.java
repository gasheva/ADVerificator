package MainForm;

import Model.Model;
import view.TableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainForm extends JFrame {
    private Model model;
    private MainController controller;
    private JPanel mainPanel;
    private JTable tblElements;
    private JTable tblMistakes;
    private JScrollPane scrollElements;
    private JScrollPane scrollMistakes;
    private JSplitPane splitPane;
    private JMenuBar mbMain;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenu startVerMenu;
    private JMenuItem miNew;
    private JMenuItem miWriteInFile;
    private JMenuItem miExit;
    private JMenuItem miStartVer;
    private JMenuItem miHelp;
    private TableModel mistakesModel;
    private TableModel elementsModel;

    public MainForm(MainController controller, Model model) {
        this.model = model;
    }

    public void createControls() {
        createJMenuBar();
        splitPane.setDividerLocation(0.5);

        //background color for scroll panes
        scrollElements.getViewport().setBackground(Color.white);
        scrollMistakes.getViewport().setBackground(Color.white);

        initTable(tblElements, elementsModel, new String[]{"id", "Тип", "Описание", "Следующие", "Предыдущие"});
        initTable(tblMistakes, mistakesModel, new String[]{"Серьезность", "Ошибка", "id элемента"});


        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        miWriteInFile.addActionListener(e->miWriteInFileClicked());
        miNew.addActionListener(e->miNewClicked());
        startVerMenu.addMouseListener(new MyMouseAdapter(startVerMenu));
        helpMenu.addMouseListener(new MyMouseAdapter(helpMenu));
        fileMenu.addMouseListener(new MyMouseAdapter(fileMenu));

        miHelp.addActionListener(e-> miHelpClicked());
        miStartVer.addActionListener(e->miStartVerClicked());
        miExit.addActionListener(e -> System.exit(0));
    }

    private void miStartVerClicked() {
    }

    private void miHelpClicked() {

    }

    private void miNewClicked() {

    }

    private void miWriteInFileClicked() {

    }


    public void createView(){
        setContentPane(mainPanel);
        setSize(800, 600);
        setLocationRelativeTo(null);
        createJMenuBar();
        setVisible(true);
    }

    private void createJMenuBar(){
        mbMain = new JMenuBar();

        fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        miNew = new JMenuItem("Загрузить диаграмму");
        miWriteInFile = new JMenuItem("Сохранить в файл");
        miExit = new JMenuItem("Выход");

        fileMenu.add(miNew);
        fileMenu.add(miWriteInFile);
        fileMenu.add(miExit);

        startVerMenu = new JMenu("Проверка");
        miStartVer = new JMenuItem("Начать проверку");
        miStartVer.setMnemonic(KeyEvent.VK_F5);


        helpMenu = new JMenu("Справка");
        miHelp = new JMenuItem("Получить справку");
        miHelp.setMnemonic(KeyEvent.VK_F1);

        mbMain.add(fileMenu);
        mbMain.add(startVerMenu);
        mbMain.add(helpMenu);
        helpMenu.add(miHelp);
        startVerMenu.add(miStartVer);

        this.setJMenuBar(mbMain);
    }

    private void setTableModel(TableModel model, JTable table){
        table.setModel(model);
    }
    private void initTable(JTable table, TableModel model, String[] headers) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //model
        model = new TableModel(headers);
        setTableModel(model, table);

        //fonts
        table.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 11));
        table.setForeground(Color.DARK_GRAY);
        table.setFont(new Font("Verdana", Font.PLAIN, 11));

        //sorter
//        table.setAutoCreateRowSorter(true);
    }

    private static class MyMouseAdapter extends MouseAdapter{
        private final JMenu menuItem;
        public MyMouseAdapter(JMenu menuItem) {
            super();
            this.menuItem = menuItem;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            super.mouseEntered(e);
            menuItem.setForeground(Color.BLUE);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            menuItem.setForeground(Color.black);
        }

    }
}
