package main.java;

import org.jcodec.api.JCodecException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static JProgressBar progress;
    public static JFrame mainFrame;
    public static ArrayList<String> paths;
    public static JPanel panel;
    public static int height;
    public static Trimmer trimmer;
    public static ArrayList<String> pathmusic;

    /**
     * @param args the command line arguments
     */


    public static void main(String[] args) {
        pathmusic = new ArrayList<>();
        paths = new ArrayList<>();
        height = 0;
        trimmer = new Trimmer();
        mainFrame = new JFrame();
        mainFrame.setSize(700, 1080);
        mainFrame.setTitle("AutoCutter");
        panel = new JPanel();

        mainFrame.setResizable(false);

        JButton button1 = new JButton("add Clip");
        JButton button2 = new JButton("add Music");
        JButton button3 = new JButton("cut");

        JLabel label = new JLabel("Your Files");

        panel.add(label);
        panel.add(button1);
        button1.addActionListener(e -> {
            try {
                addClip(true);
            } catch (IOException | JCodecException ex) {
                ex.printStackTrace();
            }
        });
        panel.add(button2);
        button2.addActionListener(e -> {
            try {
                addClip(false);
            } catch (IOException | JCodecException ex) {
                ex.printStackTrace();
            }
        });
        panel.add(button3);
        button3.addActionListener(e -> {
            progress.setValue(0);

            try {
                progress.update(progress.getGraphics());
                Editor editor = new Editor(paths, pathmusic.get(1), pathmusic.get(0));
                System.gc();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        mainFrame.add(panel);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.setBackground(Color.gray);

        progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        panel.add(progress);
        progress.setVisible(true);


        mainFrame.setVisible(true);

    }


    public static void addClip(boolean isPic) throws IOException, JCodecException {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.showOpenDialog(mainFrame);
        File[] files = chooser.getSelectedFiles();
        for (int i = 0; i < files.length; i++) {
            JTextField clipfile = new JTextField(files[i].toString(), 400);
            panel.add(clipfile);
            clipfile.setVisible(true);
            clipfile.setBounds(60, 34 + height, 550, 18);
            if (isPic) {
                JLabel picLabel = new JLabel(new ImageIcon(trimmer.getFirstFrame(files[i].toString().replace("\\", "\\\\"))));
                panel.add(picLabel);
                picLabel.setBounds(10, 34 + height, 18, 18);
                paths.add(files[i].toString().replace("\\", "\\\\"));
            } else {
                pathmusic.add(files[i].toString().replace("\\", "\\\\"));
            }
            height = height + 20;
        }
    }

}
