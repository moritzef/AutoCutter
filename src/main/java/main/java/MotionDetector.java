package main.java;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;


public class MotionDetector {


    public MotionDetector() {

    }

    public ArrayList<Double> get_motion(final String input, int frameCount, Double startSec) throws IOException, JCodecException {
        ArrayList<Double> differences = new ArrayList<Double>();
        double output = 0;
        Color colour1, colour2;
        int r1, g1, b1;
        int r2, g2, b2;
        double rDiff = 0, gDiff = 0, bDiff = 0;
        BufferedImage bufferedImage1, bufferedImage2;
        File file = new File(input);
        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        grab.seekToSecondPrecise(startSec);
        try {
            Picture picture;
            picture = grab.getNativeFrame();
            bufferedImage1 = AWTUtil.toBufferedImage(picture);
            picture = grab.getNativeFrame();
            bufferedImage2 = AWTUtil.toBufferedImage(picture);
            int width = bufferedImage1.getWidth(), height = bufferedImage1.getHeight();
            int numberPixels = width * height;
            for (int i = 0; i < frameCount; i++) {
                for (int y = 1; y < height; y++) {
                    for (int x = 1; x < width; x++) {
                        colour1 = new Color(bufferedImage1.getRGB(x, y));
                        r1 = colour1.getRed();
                        g1 = colour1.getGreen();
                        b1 = colour1.getBlue();
                        colour2 = new Color(bufferedImage2.getRGB(x, y));
                        r2 = colour2.getRed();
                        g2 = colour2.getGreen();
                        b2 = colour2.getBlue();
                        rDiff += (Math.abs(r1 - r2)) / (float) 255;
                        gDiff += (Math.abs(g1 - g2)) / (float) 255;
                        bDiff += (Math.abs(b1 - b2)) / (float) 255;
                    }
                }
                rDiff /= numberPixels;
                gDiff /= numberPixels;
                bDiff /= numberPixels;
                output = (rDiff + gDiff + bDiff) / (float) 3;
                System.out.println("difference in percent: " + output + " of Frame: " + i);
                differences.add(output);
                rDiff = 0;
                gDiff = 0;
                bDiff = 0;

                bufferedImage1 = bufferedImage2;
                picture = grab.getNativeFrame();
                bufferedImage2 = AWTUtil.toBufferedImage(picture);

            }
        } catch (Exception e) {
            return differences;
        }
        return differences;
    }

    public int get_peak(ArrayList<Double> list, double startSec) {
        try {
            return list.stream().filter(a -> list.indexOf(a) > startSec * 30 + 1).filter(a -> a > 0.04).map(a -> list.indexOf(a)).findFirst().get();
        } catch (Exception e) {
            return -1;
        }
    }

    public Double get_average(ArrayList<Double> list, int startSec, int duration) {
        Double sum = list.stream().filter(a -> list.indexOf(a) >= startSec && list.indexOf(a) <= startSec + duration).mapToDouble(a -> a).sum() / list.stream().filter(a -> list.indexOf(a) >= startSec && list.indexOf(a) <= startSec + duration).mapToDouble(a -> 1).sum();
        System.out.println("sum is " + sum);
        return sum;
    }

    public ArrayList<Double> get_single_motion_threaded(String URI, int single_framecount, double single_start_sec) throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<ArrayList<Double>> result = es.submit(new Callable<ArrayList<Double>>() {
            public ArrayList<Double> call() throws Exception {
                return get_motion(URI, single_framecount, single_start_sec);
            }
        });
        return result.get();
    }



/*
    public ArrayList<Double> get_motion_threaded(String URI, double duration) throws InterruptedException, IOException, JCodecException {
        ExecutorService es = Executors.newCachedThreadPool();
        output = new double[(int)Math.floor(duration*30)-1];
        int single_frameCount = 0;
        int single_start_sec=0;
        for(int t = 0; t<Math.ceil(duration);t++) {
            if(Math.floor(duration)==Math.ceil(duration)) single_frameCount = 30;
            if(Math.floor(duration)!=Math.ceil(duration)){ single_frameCount = (t==(int)Math.floor(duration))?(int)Math.floor(duration*30)%29:30;}
            single_start_sec += (t==0)?0:1;
            int finalSingle_frameCount = single_frameCount;
            int finalSingle_start_sec = single_start_sec;
            //es.execute(() -> {
                try {

                        double[] sub_motion = get_single_motion_threaded(URI, finalSingle_frameCount, finalSingle_start_sec);

                            for (int i = 0; i < sub_motion.length; i++) {
                                this.output[i + finalSingle_start_sec * 29] = sub_motion[i];
                            }


                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                //es.shutdown();
            //});
        }
        //es.awaitTermination(50,TimeUnit.MINUTES);





        ArrayList<Double> out = new ArrayList<>();
        for (double v : this.output) out.add(v);
        return out;
    }

    */


}

