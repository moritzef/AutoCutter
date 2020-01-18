package main.java;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
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
import java.io.IOException;

public class Trimmer {


    public Trimmer() {
    }

    public BufferedImage getFirstFrame(String input) throws IOException, JCodecException {
        File file = new File(input);

        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        grab.seekToSecondPrecise(0);
        Picture picture = grab.getNativeFrame();
        BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);

        BufferedImage resized = new BufferedImage(18, 18, bufferedImage.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(bufferedImage, 0, 0, 18, 18, 0, 0, bufferedImage.getWidth(),
                bufferedImage.getHeight(), null);
        g.dispose();
        bufferedImage.flush();
        System.gc();
        return resized;
    }


    public void Trim(final String input, final String output, final double start, final int frames) throws IOException, JCodecException {
        int frameCount = frames;
        SeekableByteChannel out = null;
        File file = new File(input);
        FrameGrab grab = null;
        grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        grab.seekToSecondPrecise(start);
        out = NIOUtils.writableFileChannel(output);
        AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(30, 1));
        Picture picture;
        /*int r1,g1,b1;
        Color colour1;
        BufferedImage image;
         */
        for (int i = 0; i < frameCount; i++) {


            picture = grab.getNativeFrame();
            /*image = AWTUtil.toBufferedImage(picture);
            int width = image.getWidth(), height=image.getHeight();
                for (int y = 1; y < height; y++) {
                    for (int x = 1; x < width; x++) {
                        colour1 = new Color(image.getRGB(x, y));
                        r1 = (colour1.getRed()>=215)?255:colour1.getRed()+40;
                        g1 = (colour1.getGreen()>=215)?255:colour1.getGreen()+40;
                        b1 = (colour1.getBlue()>=215)?255:colour1.getBlue()+40;
                        image.setRGB(x,y,new Color(r1, g1, b1).getRGB());        }
                }


             */
            System.out.println(AWTUtil.toBufferedImage(picture).getWidth() + "x" + AWTUtil.toBufferedImage(picture).getHeight() + " " + picture.getColor());
            //for JDK (jcodec-javase)
            encoder.encodeImage(AWTUtil.toBufferedImage(picture));
        }
        // Finalize the encoding, i.e. clear the buffers, write the header, etc.

        encoder.finish();
        NIOUtils.closeQuietly(out);

    }

    public void createEnding(String input, String output, int width, int height) throws IOException, JCodecException {
        SeekableByteChannel out = null;
        try {
            out = NIOUtils.writableFileChannel(output);

            AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(30, 1));

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

    public BufferedImage getImage(String input) throws IOException, JCodecException {
        File file = new File(input);
        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        grab.seekToSecondPrecise(0);
        Picture picture = grab.getNativeFrame();
        return AWTUtil.toBufferedImage(picture);

    }
}