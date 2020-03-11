package main.java;

import org.jcodec.api.JCodecException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartGUI {

    public JProgressBar progress;
    public JFrame mainFrame;
    public ArrayList<String> paths;
    public JPanel panel;
    public JPanel panel2;
    public Trimmer trimmer;
    public ArrayList<String> pathmusic;
    public GridBagConstraints GBC;

    public File recentPath;
    public JPanel optionsPanel;

    public int skipFrames;

    public StartGUI() {
        pathmusic = new ArrayList<>();
        paths = new ArrayList<>();
        trimmer = new Trimmer();
        mainFrame = new JFrame();
        mainFrame.setSize(700, 400);
        mainFrame.setTitle("AutoCutter");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new JPanel();
        panel2 = new JPanel();
        panel2.setLayout(new javax.swing.BoxLayout(
                panel2, javax.swing.BoxLayout.Y_AXIS));

        ExecutorService thread = Executors.newSingleThreadExecutor();

        /*optionsPanel = new JPanel();
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

         */


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
                    if (!(paths.size() == 0 || pathmusic.size() == 0)) {

                        try {
                            //skipFrames = Integer.parseInt(option1TextField.getText());
                        } catch (NumberFormatException nfex) {
                            //skipFrames = 0;
                        }

                        try {
                            new Editor(paths, pathmusic.get(1), pathmusic.get(0), this);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        System.out.println("Please add at least one Video Clips and the right Audio Clips");
                    }
                });
                System.gc();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JScrollPane scrollPane = new JScrollPane(panel2);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        mainFrame.add(scrollPane);

        mainFrame.add(panel, BorderLayout.NORTH);

        panel.setBackground(Color.darkGray);
        panel2.setBackground(Color.gray);

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
        if (isPic) chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Video", "mp4"));
        else chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Audio", "aac", "mp3", "ac3"));
        chooser.setCurrentDirectory(recentPath);
        chooser.showOpenDialog(mainFrame);
        File[] files = chooser.getSelectedFiles();
        for (int i = 0; i < files.length; i++) {
            JPanel rowPanel = new JPanel();
            rowPanel.setLayout(new javax.swing.BoxLayout(
                    rowPanel, javax.swing.BoxLayout.X_AXIS));
            if (isPic) {
                JLabel picLabel = new JLabel(new ImageIcon(trimmer.get_first_frame_scaled(files[i].toString())));
                picLabel.setSize(32, 18);
                rowPanel.add(picLabel);

                paths.add(files[i].toString());
            } else {
                pathmusic.add(files[i].toString());
            }
            JTextField clipFile = new JTextField(files[i].toString(), 200);
            clipFile.setMaximumSize(clipFile.getPreferredSize());
            rowPanel.add(clipFile);
            panel2.add(rowPanel);
        }
        mainFrame.setVisible(true);
    }

}