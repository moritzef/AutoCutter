package main.java;

import org.jcodec.api.JCodecException;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartGUI {

    public JProgressBar progress;
    public JFrame mainFrame;
    public ArrayList<String> paths;
    public JPanel panel;
    public int height;
    public Trimmer trimmer;
    public ArrayList<String> pathmusic;

    public File recentPath;
    public JPanel optionsPanel;

    public int skipFrames;

    public StartGUI() {

        pathmusic = new ArrayList<>();
        paths = new ArrayList<>();
        height = 0;
        trimmer = new Trimmer();
        mainFrame = new JFrame();
        mainFrame.setSize(700, 720);
        mainFrame.setTitle("AutoCutter");
        panel = new JPanel();
        ExecutorService thread = Executors.newSingleThreadExecutor();

        optionsPanel = new JPanel();
        //mainFrame.add(panel);
        mainFrame.add(optionsPanel, BorderLayout.SOUTH);

        optionsPanel.setMaximumSize(new Dimension(700, 160));
        optionsPanel.setBackground(Color.darkGray);

        JPanel option1 = new JPanel();
        optionsPanel.add(option1);

        JTextField option1Text = new JTextField("Skip Frames");
        option1Text.setEditable(false);
        option1.add(option1Text, BorderLayout.NORTH);

        JTextField option1TextField = new JTextField("1");
        option1TextField.setPreferredSize(new Dimension(40, 20));
        option1.add(option1TextField, BorderLayout.SOUTH);

        //mainFrame.setResizable(false);

        JButton button1 = new JButton("add clip");
        JButton button2 = new JButton("add music");
        JButton button3 = new JButton("cut");

        JLabel label = new JLabel("Your files");

        panel.add(label);
        panel.add(button1);
        button1.addActionListener(e -> {
            try {
                add_clip(true);
            } catch (IOException | JCodecException ex) {
                ex.printStackTrace();
            }
        });
        panel.add(button2);
        button2.addActionListener(e -> {
            try {
                add_clip(false);
            } catch (IOException | JCodecException ex) {
                ex.printStackTrace();
            }
        });
        panel.add(button3);
        button3.addActionListener(e -> {
            progress.setValue(0);

            try {
                progress.update(progress.getGraphics());
                thread.execute(() -> {
                    if(!(paths.size() == 0 || pathmusic.size() == 0)) {

                        try {
                            skipFrames = Integer.parseInt(option1TextField.getText());
                        } catch (NumberFormatException nfex) {
                            skipFrames = 0;
                        }

                        try {
                            new Editor(paths, pathmusic.get(1), pathmusic.get(0), this);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        System.out.println("Please add at least one Video Clip and the right Audio Clips");
                    }
                });
                System.gc();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        mainFrame.add(panel, BorderLayout.NORTH);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.setBackground(Color.gray);

        progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        panel.add(progress);
        progress.setVisible(true);
        mainFrame.setVisible(true);


        recentPath = new File(System.getProperty("user.dir"));
        System.out.println(recentPath);
    }

    public void add_clip(boolean isPic) throws IOException, JCodecException {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        if(isPic) chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Video", "mp4"));
        else      chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Audio", "aac", "mp3"));
        chooser.setCurrentDirectory(recentPath);
        chooser.showOpenDialog(mainFrame);
        File[] files = chooser.getSelectedFiles();
        for (int i = 0; i < files.length; i++) {
            JTextField clipFile = new JTextField(files[i].toString(), 400);
            panel.add(clipFile);
            clipFile.setVisible(true);
            clipFile.setBounds(60, 34 + height, 550, 18);
            if (isPic) {
                JLabel picLabel = new JLabel(new ImageIcon(trimmer.get_first_frame_scaled(files[i].toString().replace("\\", "\\\\"))));
                panel.add(picLabel);
                picLabel.setBounds(5, 34 + height, 32, 18);
                paths.add(files[i].toString().replace("\\", "\\\\"));
            } else {
                pathmusic.add(files[i].toString().replace("\\", "\\\\"));
            }
            height = height + 20;
        }
    }

}
