package org.livingtech.scbridge;

import graphics.scenery.*;
import graphics.scenery.backends.Renderer;
import org.joml.Vector3f;

public class CreateSceneryWindow {
    public static void main(String[] args){
        SceneryBase base = new SceneryBase("Scenery Window"){
            @Override
            public void init(){
                Hub hub = getHub();
                Scene scene = getScene();
                hub.add(
                        SceneryElement.Renderer,
                        Renderer.createRenderer(
                                hub,
                                getApplicationName(),
                                getScene(),
                                512, 512
                        )
                );
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
                cam.perspectiveCamera(50.0f, 512, 512, 0f, 10f);
                scene.addChild(cam);

            }
        };

        new Thread(base::main).start();
    }
}
