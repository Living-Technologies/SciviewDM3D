/*-
 * #%L
 * Scenery-backed 3D visualization package for ImageJ.
 * %%
 * Copyright (C) 2016 - 2018 SciView developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.livingtech.scbridge;

import deformablemesh.io.MeshReader;
import graphics.scenery.*;
import graphics.scenery.attribute.material.Material;
import graphics.scenery.volumes.Volume;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.FileInfoVirtualStack;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;
import sc.iview.SciView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import deformablemesh.track.Track;

import static sc.iview.commands.MenuWeights.DEMO;

/**
 * Make a sphere and say hello.
 *
 * The annotations in the next line define where this plugin will appear in sciview's menu, and under which label.
 *
 * @author MB Smith
 * @author Kyle Harrington
 */
@Plugin(type = InteractiveCommand.class, label = "DM3D Importer", menuRoot = "SciView",
        menu = { @Menu(label = "Demo", weight = DEMO),
                 @Menu(label = "Mesh Demo") })
public class MeshVolumeDemo implements Command {
    // This parameter is necessary to be able to access sciview from this plugin. It is automatically assigned
    // by the scijava plugin infrastructure, so just declaring it is sufficient.
    @Parameter
    private SciView sciView;
    @Parameter
    private UIService ui;

    static void shadeMesh(Mesh mesh, Color color){
        Vector3f cf = new Vector3f(color.getRGBColorComponents(new float[4]));
        Material mat = mesh.createMaterial();
        mat.setDiffuse(cf);
        mat.setAmbient(cf);
        mat.setRoughness(0.7f);
        mesh.setMaterial(mat);
    }




    @Override
    public void run() {
        TreeMap<Integer, Group> meshes = new TreeMap<>();

        try {
            System.out.println(sciView.getActiveNode());
            Node active = sciView.getActiveNode();
            Volume v;
            if(active instanceof Volume) {
                v = (Volume)active;
            } else{
                ui.showDialog("The active node needs to be a volume that" +
                        "the meshes will be scaled to.", DialogPrompt.MessageType.ERROR_MESSAGE);
                return;
            }
            File f = ui.chooseFile(null, "Select DM3D mesh file (.bmf");
            List<Track> tracks = MeshReader.loadMeshes(f);
            int maxFrame = tracks.stream().mapToInt(Track::getLastFrame).max().orElse(-1);
            Vector3i dims = v.getDimensions();
            Vector3f scales = v.spatial().getScale();
            //TODO the scale factor is squared in the z-dimension will be fixed soon
            Dm3dMeshToScMesh scaler = new Dm3dMeshToScMesh(dims.x, dims.y, dims.z, scales.x, scales.y, Math.sqrt(scales.z));
            for(int frame = 0; frame<=maxFrame; frame++){
                Group g = new Group();
                for(Track trk : tracks){
                    Color color = trk.getColor();

                    if(trk.containsKey(frame)){
                        Mesh mesh = scaler.convertMesh(trk.getMesh(frame));
                        shadeMesh(mesh, color);
                        g.addChild(mesh);
                    }
                }
                sciView.addNode(g, v);
                meshes.put(frame, g);
                g.setVisible(false);
            }
            MeshVolumeNavigator navigator = new MeshVolumeNavigator(sciView, meshes, v);
            navigator.buildUI();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }
    public static void main(final String... args) throws Exception {
        ImageJ ij = new ImageJ();
        ij.ui().getDefaultUI().show();

        try {
            ImagePlus plus = FileInfoVirtualStack.openVirtual(
                    Paths.get("sample.tif").toAbsolutePath().toString()
            );
            Calibration c = plus.getCalibration();
            float sx = (float) c.pixelWidth;
            float sy = (float) c.pixelHeight;
            float sz = (float) c.pixelDepth;
            SciView sv = SciView.create();
            Img<UnsignedShortType> img = ImageJFunctions.wrap(plus);


            Volume v = sv.addVolume(img);
            //TODO remove that square when scenery gets updated.
            v.spatial().setScale(new Vector3f(sx, sy, sz * sz));
            v.setMaxDisplayRange(2000);
            v.setMinDisplayRange(400);
        } catch(Exception e){
            //if this finishes with an unhandled exception the gui
            //never starts ?!
            e.printStackTrace();
        }

	}
}
