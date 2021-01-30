package MainForm;

import java.awt.*;

public class MyCanvas extends Canvas {
    private String path="";

    public MyCanvas(String path) {
        this.path = path;
    }

    public MyCanvas() {}

    @Override
    public void paint(Graphics g) {
//        super.paint(g);
        Toolkit t=Toolkit.getDefaultToolkit();
        //Image i=t.getImage("C:\\Users\\DocGashe\\Documents\\Лекции\\ДиПломная\\Тестирование\\Одна дорожка но файнал ноде.png"); //TODO: path
        //g.drawImage(i, 120,100,this);
        g.drawOval(0, 0, 20, 20);
        g.setColor(Color.red);

    }
}
