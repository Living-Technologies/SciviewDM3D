package org.livingtech.scbridge;

import graphics.scenery.*;
import graphics.scenery.backends.Renderer;
import graphics.scenery.controls.InputHandler;
import org.joml.Vector3f;

public class CreateSceneryWindow {
    public static void main(String[] args){
        SceneryBase base = new SceneryBase("Scenery Window"){
            @Override
            public void init(){
                super.init();
                Hub hub = getHub();
                Scene scene = getScene();
                Renderer renderer = Renderer.createRenderer(
                        hub,
                        getApplicationName(),
                        scene,
                        512, 512
                );
                setRenderer(renderer);
                hub.add( SceneryElement.Renderer, renderer );
                Box box = new Box(new Vector3f(1.0f, 1.0f, 1.0f));
                box.material().setDiffuse(new Vector3f(1.0f) );
                scene.addChild(box);

                PointLight light = new PointLight( 15.0f );
                light.spatial().setPosition(new Vector3f(0.0f, 0.0f, 2.0f));
                light.setIntensity(5.0f);
                light.setEmissionColor(new Vector3f(1.0f, 1.0f, 1.0f) );
                scene.addChild(light);

                DetachedHeadCamera cam = new DetachedHeadCamera();
                cam.spatial().setPosition(new Vector3f(0.0f, 0.0f, 5.0f) );
                cam.perspectiveCamera(50.0f, 512, 512, 0.1f, 1000f);
                scene.addChild(cam);

            }

            @Override
            public void inputSetup() {
                super.inputSetup();
                InputHandler ih = getInputHandler();
                if(ih == null){
                    return;
                }
                for(String s : getInputHandler().getAllBehaviours()){
                    System.out.println(s);
                };

            }
        };

        new Thread(base::main).start();
    }
}
