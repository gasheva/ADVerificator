package MainForm;

import Model.Model;
import debugging.Debug;
import entities.DiagramElement;
import result.ElementMistake;
import result.Mistake;
import result.Mistakes;
import view.TableModel;
import Model.ADNodesList.ADNode;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainForm extends JFrame {
    private WrapTableCellRenderer tableCellRenderer = new WrapTableCellRenderer();
    private Model model;
    private MainController controller;
    private JPanel mainPanel;
    private JTable tblElements;
    private JTable tblMistakes;
    private JScrollPane scrollElements;
    private JScrollPane scrollMistakes;
    private JSplitPane splitPane;
    private JTabbedPane tpViewer;
    private JScrollPane scrollImage;
    private JPanel imagePanel;
    private JMenuBar mbMain;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenu startVerMenu;
    private JMenuItem miWriteInFile;
    private JMenuItem miExit;
    private JMenuItem miStartVer;
    private JMenuItem miHelp;
    private TableModel mistakesModel;
    private TableModel elementsModel;
    private GraphicsOnly app;
    private Box boxes[];
    private Set<Integer> coloredRowsPrev = new HashSet();
    private Set<Integer> coloredRowsNext = new HashSet();
    private int coloredRowTarget;

    public MainForm(MainController controller, Model model) {
        this.controller = controller;
        this.model = model;
    }

    public void createControls() {
        createJMenuBar();
        scrollImage.setVisible(true);

        splitPane.setDividerLocation(0.5);

        //background color for scroll panes
        scrollElements.getViewport().setBackground(Color.white);
        scrollMistakes.getViewport().setBackground(Color.white);
        scrollImage.getViewport().setBackground(Color.blue);

        initTable(tblElements, elementsModel, new String[]{"id", "Тип", "Описание", "Следующие", "Предыдущие"});
        initTable(tblMistakes, mistakesModel, new String[]{"id", "Серьезность", "Ошибка", "id элемента"});

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        miWriteInFile.addActionListener(e->miWriteInFileClicked());

        startVerMenu.addMouseListener(new MyMouseAdapter(startVerMenu));
        helpMenu.addMouseListener(new MyMouseAdapter(helpMenu));
        fileMenu.addMouseListener(new MyMouseAdapter(fileMenu));

        miHelp.addActionListener(e-> miHelpClicked());
        miStartVer.addActionListener(e->miStartVerClicked());
        miExit.addActionListener(e -> System.exit(0));
        
        tpViewer.addChangeListener(e -> tpViewerChanged());

        tblElements.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2) {
                    tblElements.clearSelection();
                    tblElements.updateUI();
                }

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });


        tblMistakes.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                if (tblMistakes.getSelectedRow()!=-1) {
                    int mistakeId = Integer.parseInt(tblMistakes.getValueAt(tblMistakes.getSelectedRow(), 0).toString());
                    Mistake selectedMistake = Mistakes.mistakes.get(mistakeId);
                    coloredRowsPrev.clear();
                    coloredRowsNext.clear();
                    coloredRowTarget = -1;
                    if (selectedMistake instanceof ElementMistake){
                        int petriId = ((ElementMistake)selectedMistake).getElementPetriId();
                        List<Integer> prev;
                        List<Integer> next;
                        prev = ((ElementMistake)selectedMistake).getPrevPetriIds();
                        next = ((ElementMistake)selectedMistake).getNextPetriIds();

                        for (Integer integer : prev) {
                            coloredRowsPrev.add(integer);
                        }
                        for (Integer integer : next) {
                            coloredRowsNext.add(integer);
                        }
                        coloredRowTarget = petriId;
                    }
                    tblElements.updateUI();
//                    app.drawMistake((int)tblMistakes.getValueAt(tblMistakes.getSelectedRow(), 0)); //TODO
                }
            }
        });
    }


    private void tpViewerChanged() {
        controller.changeViewController(tpViewer.getSelectedIndex());
    }

    private void miStartVerClicked() {
        controller.startVer();
    }

    private void miHelpClicked() {

    }

    private void miWriteInFileClicked() {
        String path = getFileToWrite();
        controller.writeFile(path);
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
        miWriteInFile = new JMenuItem("Сохранить в файл");
        miExit = new JMenuItem("Выход");

        fileMenu.add(miWriteInFile);
        fileMenu.add(miExit);

        startVerMenu = new JMenu("Проверка");
        miStartVer = new JMenuItem("Загрузить файл и начать проверку");
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
        table.getTableHeader().setReorderingAllowed(false);     // запрет изменения порядка столбцов
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(tableCellRenderer);     // перенос строк в ячейках
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(40);  // настройка ширины столбца
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
    public void drawImage(String path){
        app = new GraphicsOnly(path);
        boxes[0].remove(0);
        boxes[0].add(app.getGui());
        app.setImage(app.image);
        boxes[1].remove(0);
        boxes[1].add(app.getControl(), "Last");
        app.paintImage();
    }
    public void setViewTableVisible(boolean isVisible) {
        scrollElements.setVisible(isVisible);
    }
    public void setViewDiagramVisible(boolean isVisible) {
        scrollImage.setVisible(isVisible);
    }
    public String getFileToOpen(String descr, String extension) {
        JFileChooser c = new JFileChooser("C:\\Users\\DocGashe\\Documents\\Лекции\\ДиПломная\\Тестирование");
        FileFilter filter = new FileNameExtensionFilter(
                descr, extension);
        c.removeChoosableFileFilter(c.getFileFilter());
        c.addChoosableFileFilter(filter);
        int rVal = c.showOpenDialog(this);
        if (rVal!=JFileChooser.APPROVE_OPTION) return null;

        return c.getSelectedFile().getAbsolutePath();
    }
    public String getFileToWrite() {
        JFileChooser c = new JFileChooser("C:\\Users\\DocGashe\\IdeaProjects\\Shell-master");
        FileFilter filter = new FileNameExtensionFilter(
                ".txt", "txt");
        c.removeChoosableFileFilter(c.getFileFilter());
        c.addChoosableFileFilter(filter);
        if (c.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
            return c.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
    public void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
    public void fillMistakesTable(){
        mistakesModel = new TableModel(new String[]{"id", "Серьезность", "Ошибка", "id элемента"});
        //"id", "Серьезность", "Ошибка", "id элемента"
        for(int i = 0; i< Mistakes.mistakes.size(); i++){
            mistakesModel.addRow(new String[]{String.valueOf(Mistakes.mistakes.get(i).getId()),
                    String.valueOf(Mistakes.mistakes.get(i).getLevel()),
                    String.valueOf(Mistakes.mistakes.get(i).getMistake()),
                    Mistakes.mistakes.get(i) instanceof ElementMistake? String.valueOf(((ElementMistake)Mistakes.mistakes.get(i)).getElementPetriId()):""
            });
        }
        setTableModel(mistakesModel, tblMistakes);
    }
    public void fillElementsTable(){
        // "id", "Тип", "Описание", "Следующие", "Предыдущие"
        elementsModel = new TableModel(new String[]{"id", "Тип", "Описание", "Следующие", "Предыдущие"});
        for(int i=0; i<model.getAdNodesList().size(); i++){
            if(model.getAdNodesList().get(i) instanceof DiagramElement) {
                ADNode node = model.getAdNodesList().getNode(i);
                final String[] idsNext = {""};
                node.getNextPetriIds().forEach(x-> idsNext[0] +=x+"; ");
                final String[] idsPrev = {""};
                node.getPrevPetriIds().forEach(x-> idsPrev[0] +=x+"; ");

                elementsModel.addRow(new String[]{String.valueOf(((DiagramElement)node.getValue()).petriId), node.getValue().getType().toString(),
                        ((DiagramElement) node.getValue()).getDescription(),
                        idsNext[0], idsPrev[0]});
            }
        }
        setTableModel(elementsModel, tblElements);

    }

    private void createUIComponents() {
        // подсветка строк при выборе
        tblElements = new JTable(elementsModel)
        {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
            {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    if (!coloredRowsPrev.isEmpty()) {
                        c.setBackground(coloredRowsPrev.contains(row) ? Color.GRAY : getBackground());
                        if (row == coloredRowTarget) {
                            c.setBackground(Color.yellow);
                        }
                        if (!coloredRowsNext.isEmpty()) {
                            if (coloredRowsNext.contains(row))
                                c.setBackground(Color.lightGray);
                        }
                    }

                }
                return c;
            }
        };
        imagePanel = new JPanel();

        LayoutManager layout = new BoxLayout(imagePanel, BoxLayout.Y_AXIS);
        boxes = new Box[2];
        boxes[0] = Box.createHorizontalBox();
        boxes[1] = Box.createHorizontalBox();

        boxes[0].createGlue();
        boxes[1].createGlue();
        imagePanel.add(boxes[0]);
        imagePanel.add(boxes[1]);
        imagePanel.setLayout(layout);

        app = new GraphicsOnly();
        boxes[0].add(app.getGui());
        app.setImage(app.image);
        boxes[1].add(app.getControl(), "Last");
        app.paintImage();

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
