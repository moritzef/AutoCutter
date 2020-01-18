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


public class MotionDetect {
    ArrayList<Double> differences;

    public MotionDetect() {

    }

    public ArrayList<Double> getMotion(final String input, Double duration, Double startSec) throws IOException, JCodecException {
        differences = new ArrayList<Double>();
        double output = 0;
        int frameCount = (int) Math.floor(duration * 30);
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

    public int getPeak(ArrayList<Double> list, double startSec) {
        try {
            return list.stream().filter(a -> list.indexOf(a) > startSec * 30 + 1).filter(a -> a > 0.04).map(a -> list.indexOf(a)).findFirst().get();
        } catch (Exception e) {
            return -1;
        }
    }

    public Double getAverage(ArrayList<Double> list, int startSec, int duration) {
        Double sum = list.stream().filter(a -> list.indexOf(a) >= startSec && list.indexOf(a) <= startSec + duration).mapToDouble(a -> a).sum() / list.stream().filter(a -> list.indexOf(a) >= startSec && list.indexOf(a) <= startSec + duration).mapToDouble(a -> 1).sum();
        System.out.println("sum is " + sum);
        return sum;
    }
}

