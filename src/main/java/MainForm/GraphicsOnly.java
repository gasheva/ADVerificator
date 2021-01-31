package MainForm;

import debugging.Debug;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

public class GraphicsOnly extends JComponent implements ChangeListener {

    private JPanel gui;
    /**
     * Displays the image.
     */
    private JLabel imageCanvas;
    private Dimension size;
    private double scale = 1.0;
    public  BufferedImage image;
    private String path = "src\\main\\resources\\noImage256x256.png";
    private Graphics2D g2;
    private int x = -1 , y= -1, id = -1;

    public GraphicsOnly(String path) {
        this.path = path;
        createGraphics();
    }

    public GraphicsOnly() {
        createGraphics();
    }
    private void createGraphics(){
        size = new Dimension(10, 10);
        setBackground(Color.black);
        try {
            image = ImageIO.read(new File(path));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setImage(Image image) {
        imageCanvas.setIcon(new ImageIcon(image));

    }

    public void initComponents() {
        if (gui == null) {
            gui = new JPanel(new BorderLayout());
            gui.setBorder(new EmptyBorder(5, 5, 5, 5));
            imageCanvas = new JLabel();
            JPanel imageCenter = new JPanel(new GridBagLayout());
            imageCenter.add(imageCanvas);
            JScrollPane imageScroll = new JScrollPane(imageCenter);
            imageScroll.setPreferredSize(new Dimension(300, 100));
            gui.add(imageScroll, BorderLayout.CENTER);

        }
    }

    public Container getGui() {
        initComponents();
        return gui;
    }

    public void stateChanged(ChangeEvent e) {
        int value = ((JSlider) e.getSource()).getValue();
        scale = value / 100.0;
        paintImage();
    }

    public void paintImage() {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        BufferedImage bi = new BufferedImage(
                (int)(imageWidth*scale),
                (int)(imageHeight*scale),
                image.getType());
        g2 = bi.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform at = AffineTransform.getTranslateInstance(0, 0);
        at.scale(scale, scale);
        g2.drawRenderedImage(image, at);
        setImage(bi);
        if (id!=-1) drawMistake(id, x, y);
    }

    public Dimension getPreferredSize() {
        int w = (int) (scale * size.width);
        int h = (int) (scale * size.height);
        return new Dimension(w, h);
    }

    public JSlider getControl() {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 50, 150, 50);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(this);
        return slider;
    }
    public void drawMistake(int id, int x, int y){
        this.x = x;
        this.y = y;
        this.id = id;
        Debug.println("[x] x="+x+" y="+y);
        g2.setColor(Color.RED);
        g2.fillOval(((int)(x*scale)), ((int)(y*scale)), (int)(10*scale), (int)(10*scale));
    }
}
