package org.livingtech.scbridge;

import graphics.scenery.Group;
import graphics.scenery.backends.RenderedImage;
import graphics.scenery.backends.Renderer;
import graphics.scenery.volumes.Volume;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import sc.iview.SciView;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.TreeMap;

public class MeshVolumeNavigator {
    SciView sciView;
    public MeshVolumeNavigator(SciView sv){
        sciView = sv;
    }
    public void buildUI(TreeMap<Integer, Group> meshes, Volume v){

        JDialog controlFrame = new JDialog((Frame)null, "Mesh/Volume navigator");
        JPanel panel = new JPanel(new BorderLayout());

        JButton but = new JButton("start");
        panel.add(but, BorderLayout.SOUTH);
        JSlider slider = new JSlider(meshes.firstKey(), meshes.lastKey());
        panel.add(slider, BorderLayout.NORTH);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(slider.isEnabled() && !slider.getValueIsAdjusting()){
                    int frame = slider.getValue();
                    meshes.values().forEach(m -> m.setVisible(false));
                    meshes.get(frame).setVisible(true);
                    v.goToTimepoint(frame);
                };
            }
        });

        but.addActionListener(evt->{
            if(but.getText().equals("stop")){
                but.setText("stopping");
                return;
            }
            if(!but.getText().equals("start")){
                return;
            }
            but.setText("stop");
            slider.setEnabled(false);
            new Thread( () -> {
                Renderer r = sciView.getSceneryRenderer();
                if (r == null) return;
                ImageStack stack = null;
                for (Integer frame : meshes.keySet()) {

                    meshes.values().forEach(m -> m.setVisible(false));
                    meshes.get(frame).setVisible(true);
                    v.goToTimepoint(frame);
                    slider.setValue(frame);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    r.screenshot("test-" + frame + ".png", true);
                    RenderedImage img = r.requestScreenshot();
                    byte[] data = img.getData();
                    if(data == null) continue;
                    IntBuffer buf = ByteBuffer.wrap(data).asIntBuffer();
                    int[] pxls = new int[img.getWidth() * img.getHeight()];
                    buf.get(pxls);
                    ImageProcessor proc = new ColorProcessor(img.getWidth(), img.getHeight(), pxls);
                    if (stack == null) {
                        stack = new ImageStack(img.getWidth(), img.getHeight());

                    }
                    stack.addSlice(proc);
                    if(but.getText().equals("stopping")){
                        break;
                    }
                }
                if (stack != null) {
                    new ImagePlus("recorded", stack).show();
                }
                but.setText("start");
                slider.setEnabled(true);
            }).start();

        });

        controlFrame.setContentPane(panel);
        controlFrame.pack();
        controlFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        controlFrame.setVisible(true);
    }
}
