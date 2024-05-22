package org.livingtech.scbridge;

import graphics.scenery.Blending;
import graphics.scenery.Mesh;
import graphics.scenery.attribute.material.DefaultMaterial;
import graphics.scenery.attribute.material.Material;
import org.joml.Vector3f;

public class MeshMaterials {

    public static void makeTransparentMaterial(Mesh mesh){
        Vector3f color = new Vector3f(0.7f, 0.7f, 1);

        Material material = new DefaultMaterial();
        material.setDiffuse(color);
        material.setAmbient(color);
        material.setSpecular(new Vector3f(1, 1, 1));
        Blending b = new Blending();
        b.setOpacity(0.5f);
        b.setOverlayBlending();
        material.setBlending(b);
        mesh.setMaterial(material);
    }
}
