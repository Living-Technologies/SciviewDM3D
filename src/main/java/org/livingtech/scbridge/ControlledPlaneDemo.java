package org.livingtech.scbridge;

import deformablemesh.gui.FurrowInput;
import graphics.scenery.*;
import graphics.scenery.backends.Renderer;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import sc.iview.SciView;

import javax.swing.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.List;

public class ControlledPlaneDemo {

    public static void main(String[] args) throws Exception {
        SciView sv = SciView.create();

        Plane p = new Plane(sv, 1.0f);


        FurrowInput fi = new FurrowInput();
        fi.setMarkFromNormal(new double[]{0, 0, 1});
        JFrame frame = new JFrame();
        frame.add(fi);
        frame.pack();
        frame.setVisible(true);

        fi.addPlaneChangeListener(new FurrowInput.PlaneChangeListener() {
            @Override
            public void setNormal(double[] n) {
                p.setNormal(n);
            }

            @Override
            public void updatePosition(double dx, double dy, double dz) {
                p.move(new double[]{dx, dy, dz});
            }
        });
    }
}

