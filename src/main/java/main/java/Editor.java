package main.java;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import main.java.decoders.Decoder;
import main.java.decoders.JLayerMp3Decoder;
import main.java.util.Beat;
import org.jcodec.api.JCodecException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Editor {
    String absolutePath = new File("Slices").getAbsolutePath().replace("\\", "\\\\") + "//";
    String endPicture = absolutePath + "Ending.png";
    Trimmer trimmer;
    Main m;
    MotionDetector motion;
    float progressInPercent = 0;
    int part = 0;
    ArrayList<Double> Length;
    ArrayList<String> paths;
    ExecutorService es = Executors.newCachedThreadPool();
    int numberOfClips;
    CountDownLatch countDownLatch;

    public Editor(ArrayList<String> videoUris, String musicMP3, String musicAAC) throws Exception {
        motion = new MotionDetector();
        trimmer = new Trimmer();
        trim_all(videoUris, musicMP3);
        create_end(videoUris, musicAAC);
    }

    public void trim_all(ArrayList<String> videoUris, String musicMP3) throws Exception {
        Length = get_lengths_of_music(musicMP3);
        paths = new ArrayList<>();
        countDownLatch = new CountDownLatch(1);
        for (String str : videoUris) {
            set_Trimmer(str, 0, get_video_duration(str), null);
        }
        countDownLatch.countDown();
        numberOfClips = part;
        System.out.println("number of clips: " + numberOfClips);
        System.gc();
        es.awaitTermination(60, TimeUnit.MINUTES);
    }

    public void create_end(ArrayList<String> videoUris, String musicMP3) throws IOException, JCodecException {
        trimmer.create_ending(endPicture, absolutePath + "endingClip.mp4", trimmer.get_image(videoUris.get(0)).getWidth(), trimmer.get_image(videoUris.get(0)).getHeight());
        this.paths.add(absolutePath + "endingClip.mp4");
        String[] arrayPath = new String[this.paths.size()];
        arrayPath = this.paths.toArray(arrayPath);
        merge_all(arrayPath, musicMP3);
    }

    public void merge_all(String[] pathsmusic, String musicMP3) throws IOException {
        Merge c = new Merge(pathsmusic, musicMP3);
        System.out.println("file ready");
        m.progress.setValue(100);
        m.progress.update(m.progress.getGraphics());
    }

    private double get_video_duration(String filepath) throws IOException {
        Movie m = MovieCreator.build(filepath);
        double Duration = 0;
        for (Track track : m.getTracks()) {
            Duration = (double) track.getDuration() / track.getTrackMetaData().getTimescale();
        }
        return Duration;
    }

    public ArrayList<Double> get_lengths_of_music(String pathMP3) throws IOException {
        ArrayList<Double> analyzedData = analyze_MP3(pathMP3);
        ArrayList<Double> Length = new ArrayList<Double>();
        for (int t = 0; t < analyzedData.size(); t++) {
            try {
                Length.add((analyzedData.get(t + 1) - analyzedData.get(t)) / 1000);
                System.out.println(analyzedData.get(t + 1) - analyzedData.get(t));
            } catch (Exception e) {
                break;
            }
        }
        return Length;
    }

    public ArrayList<Double> analyze_MP3(String pathMP3) throws IOException {
        File audioFile = new File(pathMP3);
        FileInputStream stream = new FileInputStream(audioFile);
        Decoder decoder = new JLayerMp3Decoder(stream);
        Beat[] beats = BeatDetector.detectBeats(decoder, BeatDetector.DetectorSensitivity.LOW);
        for (int i = 0; i < beats.length; i++) {
            System.out.println(beats[i]);
        }
        return get_peaks(beats);
    }

    public ArrayList<Double> get_peaks(Beat[] beat) {
        ArrayList<Double> analyzedData = new ArrayList<Double>();
        analyzedData.add(0.0);
        for (int t = 0; t < beat.length; t++) {
            if (beat[t].energy > 0.16) analyzedData.add((double) beat[t].timeMs);
        }
        return analyzedData;
    }

    public void update_Progress() {
        this.progressInPercent += ((((float) 1 / (float) this.numberOfClips) * 85));
        m.progress.setValue((int) this.progressInPercent);
        m.progress.update(m.progress.getGraphics());
    }

    public void set_Trimmer(String URI, float lengthBefore, double duration, ArrayList<Double> motionFrames) throws IOException, JCodecException {
        if (duration >= this.Length.get(this.part) + lengthBefore && this.Length.get(this.part) != null) {
            if (motionFrames == null) {
                motionFrames = motion.get_motion(URI, duration, 0.0);
            }
            if (motion.get_average(motionFrames, (int) Math.floor(lengthBefore * 30), (int) Math.floor(this.Length.get(this.part) * 30)) > 0.016) {
                int finalPart = this.part;
                es.execute(() -> {
                    try {
                        this.countDownLatch.await();
                        trimmer.trim(URI, this.absolutePath + finalPart + ".mp4", lengthBefore, (int) Math.round((this.Length.get(finalPart)) * 30));
                    } catch (IOException | JCodecException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("now");
                    update_Progress();

                    System.gc();
                    es.shutdown();
                });
                this.paths.add(this.absolutePath + part + ".mp4");
                this.part++;
                set_Trimmer(URI, (float) (1 + lengthBefore + this.Length.get(this.part - 1)), duration, motionFrames);
            } else {
                System.out.println("length before" + lengthBefore + " legth in frames" + lengthBefore * 30 + "motion  " + motion.get_peak(motionFrames, lengthBefore));
                if (motion.get_peak(motionFrames, lengthBefore) != -1 && motion.get_peak(motionFrames, lengthBefore) + (30 * this.Length.get(this.part)) < duration * 30) {
                    System.out.println(motion.get_peak(motionFrames, lengthBefore));
                    set_Trimmer(URI, (motion.get_peak(motionFrames, lengthBefore) / (float) 30), duration, motionFrames);
                }
            }
        }
    }

}