package kafl;

import com.formdev.flatlaf.intellijthemes.*;
import test.MainKotlinClass;

import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.List;

public class MainJavaClass {

    private static KeyListener keyListener;
    private static Highlighter.HighlightPainter greenPainter;
    private static Highlighter.HighlightPainter redPainter;
    private static JFrame f;
    private static JTextArea headerTextArea;
    private static JTextArea plusTextArea;
    private static JTextArea minusTextArea;
    private static JTextArea summaryTextArea;
    private static JPanel content;
    private static JScrollPane scrollPane;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkFlatIJTheme()); //FlatArcDarkContrastIJTheme //FlatArcDarkContrastIJTheme //light: FlatCyanLightIJTheme FlatGrayIJTheme
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        greenPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.decode("#ffaa00"));
        redPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.red);

        //creating instance of JFrame
        f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = f.getContentPane();
        contentPane.setLayout(new GridBagLayout());

        contentPane.addKeyListener(keyListener);

        contentPane.setDropTarget(new DropTarget());

        SourcePane dataPene = new SourcePane();
        f.add(dataPene);
        dataPene.getCsvButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File startCsvFile = getLastDownloadedCsvFile();
                dataPene.csv.setText(startCsvFile.getAbsolutePath());

                if (startCsvFile.exists())
                    calc(dataPene);
            }
        });


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 1;

        int columns = 115;
        headerTextArea = new JTextArea(0, columns);
        setTextAreaStyle(headerTextArea);
        headerTextArea.setForeground(Color.decode("#3a91cf"));
//        f.add(headerTextArea, gbc);


        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(headerTextArea);

        gbc.gridx = 0;
        gbc.gridy++;

        plusTextArea = new JTextArea(0, columns);
        setTextAreaStyle(plusTextArea);
        plusTextArea.setForeground(Color.decode("#559124"));
//        f.add(plusTextArea, gbc);
        content.add(plusTextArea);


        minusTextArea = new JTextArea(0, columns);
        setTextAreaStyle(minusTextArea);
        minusTextArea.setForeground(Color.decode("#d3524f"));
        content.add(minusTextArea);


        summaryTextArea = new JTextArea(0, columns);
        setTextAreaStyle(summaryTextArea);
        summaryTextArea.setForeground(Color.decode("#ffb700"));

        content.add(summaryTextArea);

        scrollPane = new JScrollPane(content);
        scrollPane.createVerticalScrollBar();
        scrollPane.createHorizontalScrollBar();
        scrollPane.setPreferredSize(new Dimension(850, 550));

        f.add(scrollPane, gbc);

        f.pack();
        f.setTitle("mBank Analyzer (by Mateusz Kaflowski)");
        f.setSize(950, 760);//400 width and 500 height
//        f.setLayout(null);//using no layout managers
        centreWindow(f);
        f.setVisible(true);//making the frame visible

        initKeyListener(f, dataPene);
        dataPene.setKeyListener(keyListener);

    }

    private static File getLastDownloadedCsvFile() {
        String home = System.getProperty("user.home");
        File file = new File(home + "/Downloads/");
        System.out.println(file.exists());


        // This filter will only include files ending with .py
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".Csv");
            }
        };

        String[] pathnames = file.list(filter);

        long max = 0;
        File winner = file;

        // For each pathname in the pathnames array
        for (String pathname : pathnames) {
            // Print the names of files and directories
            System.out.println(pathname);
            File file1 = new File(file + File.separator + pathname);
            if (file1.lastModified() > max) {
                winner = file1;
                max = file1.lastModified();
            }
        }

        return winner;

    }

    private static void setTextAreaStyle(JTextArea headerTextArea) {
        headerTextArea.setFont(new Font(Font.MONOSPACED, Font.CENTER_BASELINE, 12));
        headerTextArea.setBackground(Color.decode("#2b2b2b"));
    }

    public static void centreWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
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

    private static void initKeyListener(JFrame f, SourcePane dataPene) {
        keyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                calc(dataPene);
            }
        };
    }

    private static void calc(SourcePane dataPene) {
        String resString = MainKotlinClass.calc(dataPene.csv.getText().trim(),
                dataPene.gielda.getText(),
                dataPene.rok.getText(), dataPene.prowizja.getText(), dataPene.prowizjaCBox.isSelected());

        String[] res = resString.split("\n" + MainKotlinClass.getSeparator());

        if (res.length == 4) {
            headerTextArea.setText(res[0]);
            plusTextArea.setText(res[1]);
            minusTextArea.setText(res[2]);
            summaryTextArea.setText(res[3]);
        } else {
            headerTextArea.setText(resString);
            plusTextArea.setText("");
            minusTextArea.setText("");
            summaryTextArea.setText("");
        }

    }

    public static class SourcePane extends JPanel {
        private final JTextField csv;
        private final JTextField gielda;
        private final JTextField rok;
        private final JTextField prowizja;
        private final JButton getCsvButton;
        private final JCheckBox prowizjaCBox;

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

            gbc.gridx++;
            add((getCsvButton = new JButton("Znajdź csv")), gbc);
            gbc.gridy++;
            gbc.gridy++;
            gbc.gridy++;
            add((prowizjaCBox = new JCheckBox("Prowizja dla S z pliku")), gbc);
            prowizjaCBox.setSelected(true);
            gbc.gridy--;
            gbc.gridy--;
            gbc.gridy--;

            gbc.gridx--;

            gbc.gridy++;
            add((gielda = new JTextField(10)), gbc);
            gbc.gridy++;
            add((rok = new JTextField(10)), gbc);
            rok.setText(String.valueOf(new Date().getYear() + 1900));
            gbc.gridy++;
            add((prowizja = new JTextField(10)), gbc);
            prowizja.setText("0.39");

            csv.setText("Upuść tutaj plik");

            setDnD(csv, csv);
        }

        public void setKeyListener(KeyListener keyListener) {
            csv.addKeyListener(keyListener);
            gielda.addKeyListener(keyListener);
            rok.addKeyListener(keyListener);
            prowizja.addKeyListener(keyListener);
            prowizjaCBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    keyListener.keyReleased(null);
                }
            });
        }


    }
}
