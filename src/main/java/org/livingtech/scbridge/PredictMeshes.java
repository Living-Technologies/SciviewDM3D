package org.livingtech.scbridge;

import bdv.viewer.SourceAndConverter;
import graphics.scenery.Group;
import graphics.scenery.Node;
import graphics.scenery.volumes.Volume;
import net.imglib2.RandomAccess;
import net.imglib2.View;
import net.imglib2.img.Img;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.alg.MarchingCubesRealType;
import net.imglib2.mesh.alg.MeshConnectedComponents;
import net.imglib2.mesh.alg.RemoveDuplicateVertices;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.joml.Vector3f;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;
import sc.iview.SciView;

import java.util.Arrays;
import java.util.Map;

import static sc.iview.commands.MenuWeights.DEMO;

@Plugin(type = InteractiveCommand.class, label = "Predict Meshes", menuRoot = "SciView",
        menu = { @Menu(label = "Demo", weight = DEMO),
                @Menu(label = "Prediction Demo") })
public class PredictMeshes implements Command {
    @Parameter
    private SciView sciView;
    @Parameter
    private UIService ui;
    @Override
    public void run() {
        Node active = sciView.getActiveNode();
        Volume v;
        if(active instanceof Volume) {
            v = (Volume)active;
            Img<UnsignedByteType> img = (Img<UnsignedByteType>)v.getMetadata().get("RandomAccessibleInterval");

            int tp = v.getCurrentTimepoint();
            System.out.println(Arrays.toString(img.dimensionsAsLongArray()));
            IntervalView<UnsignedByteType> view = Views.hyperSlice(img, 3, tp);
            Mesh meshes = MarchingCubesRealType.calculate(view, 1);
            meshes = RemoveDuplicateVertices.calculate(meshes, 0);
            Group g = new Group(  );
            g.setName("meshes-at:" + tp);
            for(Map.Entry<String, Object> entry : v.getMetadata().entrySet()){
                System.out.println(entry.getKey() + ", " + entry.getValue());
            }
            for(Mesh m : MeshConnectedComponents.iterable(meshes)){
                graphics.scenery.Mesh ready = MarchingCubesCheck.convert(m);
                ready.material().setWireframe(true);
                ready.material().setDiffuse(new Vector3f(1f, 0.7f, 0.5f));
                g.addChild(ready);
            }
            sciView.addNode(g, v);
        } else{
            ui.showDialog("The active node needs to be a volume.", DialogPrompt.MessageType.ERROR_MESSAGE);
            return;
        }
    }
}
