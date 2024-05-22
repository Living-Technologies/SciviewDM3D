package org.livingtech.scbridge;

import deformablemesh.gui.SwingJSTerm;
import graphics.scenery.Group;
import graphics.scenery.Node;
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
import java.util.Iterator;
import java.util.TreeMap;

public class MeshVolumeNavigator {
    final SciView sciView;
    final TreeMap<Integer, Group> meshes;
    final Volume v;

    final JSlider slider;
    final JButton startStop;

    public MeshVolumeNavigator(SciView sv, TreeMap<Integer, Group> meshes, Volume v){
        sciView = sv;
        this.meshes = meshes;
        this.v = v;
        slider = new JSlider(meshes.firstKey(), meshes.lastKey());
        startStop = new JButton("start");

    }

    public void sliderChanged(ChangeEvent e){
        if(slider.isEnabled() && !slider.getValueIsAdjusting()){
            int frame = slider.getValue();
            meshes.values().forEach(m -> m.setVisible(false));
            meshes.get(frame).setVisible(true);
            v.goToTimepoint(frame);
        }
    }

    public void start(){
        startStop.setText("stop");
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
                if(startStop.getText().equals("stopping")){
                    break;
                }
            }

            if (stack != null) {
                new ImagePlus("recorded", stack).show();
            }
            EventQueue.invokeLater(()->{
                startStop.setText("start");
                slider.setEnabled(true);
            });
        }).start();
    }
    public void stop(){
        startStop.setText("stopping");
    }
    public void hideMeshes(){
        meshes.values().stream().forEach(g->g.setVisible(false));
    }
    public void buildUI(){

        JDialog controlFrame = new JDialog((Frame)null, "Mesh/Volume navigator");
        JPanel panel = new JPanel(new BorderLayout());
        JButton hide = new JButton("hide meshes");
        hide.addActionListener(evt->{
            hideMeshes();
        });

        panel.add(startStop, BorderLayout.EAST);
        panel.add(hide, BorderLayout.WEST);
        panel.add(slider, BorderLayout.NORTH);
        JButton remove = new JButton("remove");
        panel.add(remove, BorderLayout.SOUTH);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                sliderChanged(e);
            }
        });

        startStop.addActionListener(evt->{
            if(startStop.getText().equals("stop")){
                stop();
            }
            if(startStop.getText().equals("start")){
                start();
            }
        });

        controlFrame.setContentPane(panel);
        controlFrame.pack();
        controlFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        controlFrame.setVisible(true);

        remove.addActionListener(evt->{
            Iterator<Group> iter = meshes.values().iterator();
            while(iter.hasNext()){
                Node node = iter.next();
                boolean publish = ! iter.hasNext();
                sciView.deleteNode(node, publish);
            }

            //meshes.values().forEach(g -> { sciView.deleteNode(g, false);});
            controlFrame.setVisible(false);
        });
    }
}
