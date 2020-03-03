package main.java;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import main.java.decoders.Decoder;
import main.java.decoders.JLayerMp3Decoder;
import main.java.util.Beat;
import org.jcodec.api.JCodecException;
import org.jcodec.common.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Editor {
    Main m;
    String absolutePath = new File("Slices").getAbsolutePath().replace("\\", "\\\\") + "//";
    String endPicture = absolutePath + "Ending.png";
    String startPicture = absolutePath + "beginning.png";
    Trimmer trimmer;
    MotionDetector motion;
    float progressInPercent = 0;
    CountDownLatch thread_counter;
    int part = 0;
    int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    ArrayList<Double> Length;
    ArrayList<String> paths;
    ExecutorService es = Executors.newCachedThreadPool();
    int numberOfClips;
    int fps;
    CountDownLatch cl;

    public Editor(ArrayList<String> videoUris, String musicMP3, String musicAAC) throws Exception {
        motion = new MotionDetector();
        trimmer = new Trimmer();
        this.Length = get_lengths_of_music(musicMP3);
        this.paths = new ArrayList<>();
        this.fps = get_fps(videoUris.get(0));
        start(videoUris);
        trim_all(videoUris);
        create_end(videoUris, musicAAC);
    }

    public void trim_all(ArrayList<String> videoUris) throws Exception {
        cl = new CountDownLatch(1);
        for (String str : videoUris) {
            System.out.println("fps: " + get_fps(str));
            set_Trimmer(str, 0, get_video_duration(str), motion.get_single_motion_threaded(str, (int) Math.floor(get_video_duration(str) * this.fps), 0));
        }
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals("Finalizer")) {
                System.out.println(t);
                t.setPriority(Thread.MAX_PRIORITY);
                System.out.println(t);
            } else {
                t.setPriority(Thread.MIN_PRIORITY);
            }
        }
        this.numberOfClips = part;
        thread_counter = new CountDownLatch(this.numberOfClips);
        cl.countDown();
        System.out.println("number of clips: " + this.numberOfClips);
        System.gc();
        es.awaitTermination(24, TimeUnit.HOURS);
    }

    public void start(ArrayList<String> videoUris) throws IOException, JCodecException {
        trimmer.create_beginning(startPicture, absolutePath + "firstClip.mp4", trimmer.get_image(videoUris.get(0)).getWidth(), trimmer.get_image(videoUris.get(0)).getHeight(), this.fps, (int) Math.round(this.Length.get(this.part) * this.fps));
        this.paths.add(absolutePath + "firstClip.mp4");
        this.part++;
    }

    public void create_end(ArrayList<String> videoUris, String musicMP3) throws IOException, JCodecException {
        trimmer.create_ending(endPicture, absolutePath + "endingClip.mp4", trimmer.get_image(videoUris.get(0)).getWidth(), trimmer.get_image(videoUris.get(0)).getHeight(), this.fps);
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

    public double get_video_duration(String filepath) throws IOException {
        Movie m = MovieCreator.build(filepath);
        double Duration = 0;
        for (Track track : m.getTracks()) {
            Duration = (double) track.getDuration() / track.getTrackMetaData().getTimescale();
        }
        return Duration;
    }

    public int get_fps(String filepath) throws IOException {
        File file = new File(filepath);
        Format f = JCodecUtil.detectFormat(file);
        Demuxer d = JCodecUtil.createDemuxer(f, file);
        DemuxerTrack vt = d.getVideoTracks().get(0);
        DemuxerTrackMeta dtm = vt.getMeta();
        return (int) Math.round(dtm.getTotalFrames() / dtm.getTotalDuration());
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
        System.out.println(pathMP3);
        File audioFile = new File(pathMP3);
        FileInputStream stream = new FileInputStream(audioFile);
        Decoder decoder = new JLayerMp3Decoder(stream);
        Beat[] beats = BeatDetector.detectBeats(decoder, BeatDetector.DetectorSensitivity.MIDDLING);
        for (Beat beat : beats) {
            System.out.println(beat);
        }
        return get_peaks(beats);
    }

    public ArrayList<Double> get_peaks(Beat[] beat) {
        ArrayList<Double> analyzedData = new ArrayList<Double>();
        analyzedData.add(0.0);
        for (int t = 0; t < beat.length; t++) {
            if (beat[t].energy > 0.2) analyzedData.add((double) beat[t].timeMs);
        }
        return analyzedData;
    }

    public void update_Progress() {
        this.progressInPercent += ((((float) 1 / (float) this.numberOfClips) * 85));
        System.out.println(this.numberOfClips + "  " + this.progressInPercent);
        m.progress.setValue((int) this.progressInPercent);
        m.progress.update(m.progress.getGraphics());
    }

    public void set_Trimmer(String URI, float lengthBefore, double duration, ArrayList<Double> motionFrames) {
        if (duration >= this.Length.get(this.part) + lengthBefore && this.Length.get(this.part) != null) {
            if (motion.get_average(motionFrames, (int) Math.floor(lengthBefore * this.fps), (int) Math.floor(this.Length.get(this.part) * this.fps)) > 0.017) {
                int finalPart = this.part;
                int final_thread_number = this.part;
                es.execute(() -> {
                    try {
                        cl.await();
                        while (true) {
                            if ((int) thread_counter.getCount() <= final_thread_number + MAX_THREADS) break;
                            Thread.sleep(500);
                        }
                        trimmer.trim(URI, this.absolutePath + finalPart + ".mp4", lengthBefore, (int) Math.round((this.Length.get(finalPart)) * this.fps), this.fps);
                        update_Progress();
                    } catch (IOException | JCodecException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("now");

                    System.gc();
                    thread_counter.countDown();
                    es.shutdown();
                });
                this.paths.add(this.absolutePath + part + ".mp4");
                this.part++;
                set_Trimmer(URI, (float) (1 + lengthBefore + this.Length.get(this.part - 1)), duration, motionFrames);
            } else {
                if (motion.get_peak(motionFrames, lengthBefore) != -1 && motion.get_peak(motionFrames, lengthBefore) + (this.fps * this.Length.get(this.part)) < duration * this.fps) {
                    System.out.println(motion.get_peak(motionFrames, lengthBefore));
                    set_Trimmer(URI, (motion.get_peak(motionFrames, lengthBefore) / (float) this.fps), duration, motionFrames);
                }
            }
        }
        if (motionFrames != null) motionFrames.clear();
    }

}