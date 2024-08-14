package org.livingtech.scbridge;

import deformablemesh.gui.FurrowInput;
import graphics.scenery.*;
import graphics.scenery.backends.Renderer;
import graphics.scenery.controls.InputHandler;
import graphics.scenery.controls.SwingMouseAndKeyHandler;
import graphics.scenery.controls.behaviours.SelectCommand;
import kotlin.jvm.functions.Function0;
import org.jogamp.java3d.utils.picking.PickResult;
import org.joml.Vector3f;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.InputTrigger;
import sc.iview.SciView;
import sc.iview.ui.MainWindow;

import javax.swing.JFrame;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static sc.iview.commands.MenuWeights.DEMO;

@Plugin(type = InteractiveCommand.class, label = "Controlled Plane", menuRoot = "SciView",
        menu = { @Menu(label = "Demo", weight = DEMO),
                @Menu(label = "Plane Demo") })
public class ControlledPlaneDemo implements Command {
    @Parameter
    private SciView sciView;
    @Parameter
    private UIService ui;
    public static void main(String[] args) throws Exception {
        SciView sv = SciView.create();

        /*
        Plane p = new Plane(1.0f);
        sv.addMesh(p.getMesh());

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
        MainWindow window = sv.getMainWindow();

        InputHandler ih = sv.getSceneryInputHandler();
        if(ih!= null){
            Renderer render = sv.getSceneryRenderer();
            Scene scene = sv.getCurrentScene();
            Camera cam = sv.getCamera();
            ClickBehaviour b = new ClickBehaviour() {
                @Override
                public void click(int i, int i1) {
                    for(Node node: sv.getAllSceneNodes()){
                        System.out.println(node);
                    }
                    Camera cam = sv.getCamera();
                    System.out.println("clicked!");
                    Scene.RaycastResult rcr = cam.getNodesForScreenSpacePosition(i, i1);
                    System.out.println(rcr);
                    System.out.println(cam.getLinkedNodes().size());
                    for(Scene.RaycastMatch match : rcr.getMatches()){
                        System.out.println(match);
                    }
                }
            };
            DragBehaviour db = new DragBehaviour() {
                @Override
                public void init(int i, int i1) {
                    System.out.println("down!");
                }

                @Override
                public void drag(int i, int i1) {
                    System.out.println("move!");
                }

                @Override
                public void end(int i, int i1) {
                    System.out.println("up!");
                }
            };
            ih.addBehaviour("movit", db);
            ih.addKeyBinding("movit", "button1");
            ih.addBehaviour("selecty", b);
            ih.addKeyBinding("selecty", "button1");

            for(String s: ih.getAllBehaviours()){
                System.out.println(s);
            }
        }

         */
    }

    @Override
    public void run() {


        Plane p = new Plane(1.0f);

        FurrowInput fi = new FurrowInput();
        fi.setMarkFromNormal(new double[]{0, 0, 1});
        JFrame frame = new JFrame();
        frame.add(fi);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        Sphere cursor = new Sphere(0.1f, 128, false);

        InputHandler ih = sciView.getSceneryInputHandler();
        if (ih != null) {

            Map<String, Behaviour> behaviours = new HashMap<>();
            InputTrigger button1 = InputTrigger.getFromString("button1");

            for(String behave : ih.getAllBehaviours()){
                if(ih.getBehaviour(behave) instanceof DragBehaviour){
                    if(ih.getKeyBindings(behave).contains(button1)){
                        System.out.println("disabling: " + behave + " bound " + ih.getKeyBindings(behave));
                        behaviours.put(behave, ih.getBehaviour(behave));
                    }
                }
            }

            AtomicBoolean disabled = new AtomicBoolean(false);
            ClickBehaviour b = new ClickBehaviour() {
                @Override
                public void click(int i, int i1) {
                    if(!disabled.get()) {

                        for (String b : behaviours.keySet()) {
                            ih.removeBehaviour(b);
                        }
                        disabled.set(true);
                    } else{
                        for(String key : behaviours.keySet()){
                            ih.addBehaviour(key, behaviours.get(key));
                        }
                        disabled.set(false);
                    }
                }
            };
            DragBehaviour db = new DragBehaviour() {
                @Override
                public void init(int i, int i1) {
                    Camera cam = sciView.getCamera();
                    Scene.RaycastResult rcr = cam.getNodesForScreenSpacePosition(i, i1);
                    for (Scene.RaycastMatch match : rcr.getMatches()) {
                        if(match.getNode() == p.getMesh()){


                            Vector3f origin = rcr.getInitialPosition();
                            Vector3f dir = rcr.getInitialDirection();
                            float d = match.getDistance();
                            cursor.spatial().setPosition( dir.normalize(d).add(origin) );
                            Node n = sciView.getActiveNode();
                            sciView.addNode(cursor);
                            sciView.setActiveNode(n);
                            break;
                        }
                    }

                }

                @Override
                public void drag(int i, int i1) {
                    Camera cam = sciView.getCamera();
                    Scene.RaycastResult rcr = cam.getNodesForScreenSpacePosition(i, i1);
                    for (Scene.RaycastMatch match : rcr.getMatches()) {
                        if(match.getNode() == p.getMesh()){
                            Vector3f origin = rcr.getInitialPosition();
                            Vector3f dir = rcr.getInitialDirection();
                            float d = match.getDistance();
                            cursor.spatial().setPosition( dir.normalize(d).add(origin) );
                            break;
                        }
                    }
                }

                @Override
                public void end(int i, int i1) {
                    sciView.deleteNode(cursor);
                }
            };

            ih.addBehaviour("movit", db);
            ih.addKeyBinding("movit", "button1");
            ih.addBehaviour("selecty", b);
            ih.addKeyBinding("selecty", "button1");


        }
        Mesh mesh = p.getMesh();
        mesh.setBoundingBox(mesh.generateBoundingBox());
        mesh.geometry().setDirty(true);
        sciView.addNode(mesh);
    }

}

