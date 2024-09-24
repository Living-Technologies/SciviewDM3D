import graphics.scenery.volumes.Colormap;
import graphics.scenery.utils.Image;
import graphics.scenery.volumes.ColormapPanel;
import graphics.scenery.volumes.DummyVolume;
import graphics.scenery.volumes.Volume;
import org.joml.Vector4f;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class ColorMapPurveyor {

    public static void main(String[] args){
        Colormap map = Colormap.fromColor(new Vector4f(1, 0, 0, 1));
        for(String name: Colormap.list()){
            System.out.println(name);
        }
        int w = 256;
        int h = 16;
        byte[] array = new byte[2*2*4];

        for(int i = 0; i<2; i++){

            for(int j = 0; j<2; j++){
                int o = (j*2 + i)*4;
                array[o] = (byte)(i * 0xff);
                array[o+3] = (byte)0xff;
            }
        }
        Colormap arr = Colormap.fromArray( array, 2, 2 );
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setPaint(
                new LinearGradientPaint(
                    new Point2D.Float(0, h / 2 ),
                    new Point2D.Float(w-10, h / 2),
                    new float[]{0f, 1f},
                    new Color[]{Color.BLACK, Color.RED}
                )
            );
        g2d.fillRect(0, 0, w, h);
        g2d.dispose();
        JLabel label = new JLabel();
        label.setIcon( new ImageIcon(img));
        JFrame frame = new JFrame();

        ColormapPanel panel = new ColormapPanel(new Volume());
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(label, BorderLayout.NORTH);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Colormap map2 = Colormap.fromBuffer(Image.Companion.bufferedImageToRGBABuffer(img), w, h );
        float delta = 1f/256f;
        for(float i = 0f; i<=1f; i+=delta){

            Vector4f a = arr.sample(i);
            Vector4f b = map2.sample(i);
            System.out.printf("%f :: %s // %s \n",i, a, b);
        }
        System.out.println( (img.getRGB(w-1, h/2)>>16)&0xff);
    }
}
