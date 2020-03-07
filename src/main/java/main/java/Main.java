package main.java;

import org.jcodec.api.JCodecException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static JProgressBar progress;
    public static JFrame mainFrame;
    public static ArrayList<String> paths;
    public static JPanel panel;
    public static int height;
    public static Trimmer trimmer;
    public static ArrayList<String> pathmusic;

    public static File recentPath;

    /**
     * @param args the command line arguments
     */


    public static void main(String[] args) {

        StartGUI gui = new StartGUI();
    }

}
