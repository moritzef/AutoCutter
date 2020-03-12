package main.java;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.api.awt.AWTFrameGrab;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

public class Trimmer {



    public Trimmer() {
    }

    public static class SortedImage implements Comparable<SortedImage> {

        public final double timestamp;
        public final BufferedImage data;

        public SortedImage(PictureWithMetadata p) {
            data = AWTUtil.toBufferedImage(p.getPicture());
            timestamp = p.getTimestamp();
        }

        @Override
        public int compareTo(SortedImage o2) {
            return Double.compare(timestamp, o2.timestamp);
        }
    }

    public static void trim(final String input, final String output, final double start, final int frames, final int fps) throws IOException {
        SeekableByteChannel out = NIOUtils.writableFileChannel(output);
        AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(fps, 1));
        try {
            SeekableByteChannel bytes = NIOUtils.readableChannel(new File(input));
            FrameGrab videoData = FrameGrab.createFrameGrab(bytes);
            ArrayList<SortedImage> sortedImages = new ArrayList();
            videoData.seekToSecondPrecise(start);
            for (int i = 0; i < frames; i++) {
                PictureWithMetadata src = videoData.getNativeFrameWithMetadata();
                sortedImages.add(new SortedImage(src));
            }
            Collections.sort(sortedImages);
            for (int i = 0; i < frames; i++) {
                encoder.encodeImage(sortedImages.get(i).data);
            }
            sortedImages.clear();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        encoder.finish();
        NIOUtils.closeQuietly(out);
    }






    public BufferedImage get_first_frame_scaled(String input) throws IOException, JCodecException {
        File file = new File(input);

        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        grab.seekToSecondPrecise(0);
        Picture picture = grab.getNativeFrame();
        BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);

        BufferedImage resized = new BufferedImage(32, 18, bufferedImage.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(bufferedImage, 0, 0, 32, 18, 0, 0, bufferedImage.getWidth(),
                bufferedImage.getHeight(), null);
        g.dispose();
        bufferedImage.flush();
        System.gc();
        return resized;
    }


  /*  public void trim(final String input, final String output, final double start, final int frames, final int fps) throws IOException, JCodecException {
        /*int frameCount = frames;
        SeekableByteChannel out = NIOUtils.writableFileChannel(output);
        File file = new File(input);
        FrameGrab grab;
        grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        grab.seekToSecondPrecise(start);
        AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(fps, 1));
        Picture picture;


        for (int i = 0; i < frameCount; i++) {
            picture = grab.getNativeFrame();
            System.out.println(AWTUtil.toBufferedImage(picture).getWidth() + "x" + AWTUtil.toBufferedImage(picture).getHeight() + " " + picture.getColor());

            encoder.encodeImage(AWTUtil.toBufferedImage(picture));
        }
        encoder.finish();
        NIOUtils.closeQuietly(out);


    }
*/

    public void create_beginning(String input, String output, int width, int height, int fps, int length) throws IOException, JCodecException {
        SeekableByteChannel out = null;
        try {
            out = NIOUtils.writableFileChannel(output);

            AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(fps, 1));

            BufferedImage image = ImageIO.read(new File(input));

            BufferedImage resized = new BufferedImage(width, height, image.getType());
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(),
                    image.getHeight(), null);
            g.dispose();
            // Encode the image
            for (int t = 0; t < length; t++) {
                encoder.encodeImage(resized);
                System.out.println(t);
            }

            // Finalize the encoding, i.e. clear the buffers, write the header, etc.
            encoder.finish();
        } finally {
            NIOUtils.closeQuietly(out);
        }

    }

    public void create_ending(String input, String output, int width, int height, int fps) throws IOException, JCodecException {
        SeekableByteChannel out = null;
        try {
            out = NIOUtils.writableFileChannel(output);

            AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(fps, 1));

            BufferedImage image = ImageIO.read(new File(input));

            BufferedImage resized = new BufferedImage(width, height, image.getType());
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(),
                    image.getHeight(), null);
            g.dispose();
            // Encode the image
            encoder.encodeImage(resized);
            encoder.encodeImage(resized);
            encoder.encodeImage(resized);
            encoder.encodeImage(resized);
            encoder.encodeImage(resized);


            // Finalize the encoding, i.e. clear the buffers, write the header, etc.
            encoder.finish();
        } finally {
            NIOUtils.closeQuietly(out);
        }

    }

    public BufferedImage get_image(String input) throws IOException, JCodecException {
        File file = new File(input);
        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        grab.seekToSecondPrecise(0);
        Picture picture = grab.getNativeFrame();
        return AWTUtil.toBufferedImage(picture);

    }
}