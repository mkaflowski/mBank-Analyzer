package kafl;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatArcDarkContrastIJTheme;
import test.MainKotlinClass;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;

public class MainJavaClass {

    private static KeyListener keyListener;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatArcDarkContrastIJTheme()); //FlatArcDarkContrastIJTheme
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame f = new JFrame();//creating instance of JFrame
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = f.getContentPane();
        contentPane.setLayout(new GridBagLayout());

        contentPane.addKeyListener(keyListener);

        contentPane.setDropTarget(new DropTarget());

        SourcePane dataPene = new SourcePane();
        f.add(dataPene);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

//        JButton button;
//        f.add(button = new JButton("LICZ"), gbc);
//        button.setBackground(Color.green);

        gbc.gridx = 0;
        gbc.gridy = 1;

        JTextArea textArea= new JTextArea(36,105);
        textArea.setFont(new Font(Font.MONOSPACED, Font.CENTER_BASELINE, 12));
        f.add(textArea , gbc);

        f.pack();
        f.setTitle("mBank Analyzer (by Mateusz Kaflowski)");
        f.setSize(1000, 760);//400 width and 500 height
//        f.setLayout(null);//using no layout managers
        f.setVisible(true);//making the frame visible

        initKeyListener(f, dataPene, gbc, textArea);
        dataPene.setKeyListener(keyListener);
    }

    public static void setDnD(JTextField f, JTextField csv) {
        f.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List) evt
                            .getTransferable().getTransferData(
                                    DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        System.out.println(file.getAbsoluteFile());
                        csv.setText(file.getAbsoluteFile().toString());
                        /*
                         * NOTE:
                         *  When I change this to a println,
                         *  it prints the correct path
                         */
                    }

                    keyListener.keyReleased(null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private static void initKeyListener(JFrame f, SourcePane dataPene, GridBagConstraints gbc, JTextArea textArea) {
        keyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                calc(dataPene, textArea);
            }
        };
    }

    private static void calc(SourcePane dataPene, JTextArea textArea) {
        String resString = MainKotlinClass.calc(dataPene.csv.getText().trim(),
                dataPene.gielda.getText(),
                dataPene.rok.getText(), dataPene.prowizja.getText());
        textArea.setText(resString);
    }

    public static class SourcePane extends JPanel {
        private JTextField csv;
        private JTextField gielda;
        private JTextField rok;
        private JTextField prowizja;

        public SourcePane() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;

            add(new JLabel("Plik csv: "), gbc);
            gbc.gridy++;
            add(new JLabel("Giełda [GPW,NASDAQ] (puste = wszystkie): "), gbc);
            gbc.gridy++;
            add(new JLabel("Rok (puste = wszystkie): "), gbc);
            gbc.gridy++;
            add(new JLabel("Prowizja: "), gbc);

            gbc.gridx++;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            add((csv = new JTextField(30)), gbc);
            gbc.gridy++;
            add((gielda = new JTextField(10)), gbc);
            gbc.gridy++;
            add((rok = new JTextField(10)), gbc);
            gbc.gridy++;
            add((prowizja = new JTextField(10)), gbc);
            prowizja.setText("0.39");

            csv.setText("/Users/mateuszkaflowski/Downloads/maklerfile.Csv");

            setDnD(csv,csv);
        }

        public void setKeyListener(KeyListener keyListener){
            csv.addKeyListener(keyListener);
            gielda.addKeyListener(keyListener);
            rok.addKeyListener(keyListener);
            prowizja.addKeyListener(keyListener);
        }


    }
}
