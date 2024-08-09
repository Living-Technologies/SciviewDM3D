package org.livingtech;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

public class GeometryChecks {

    public static void main(String[] args){

        Vector3f v = new Vector3f(0, 0, 1);
        FloatBuffer b = FloatBuffer.wrap(new float[3]);
        v.get(b);
        System.out.println(b.position());
        AxisAngle4f aa = new AxisAngle4f((float)Math.PI/4, new Vector3f(1, 0, 0));
        Quaternionf quat = new Quaternionf(aa);
        System.out.println(quat.transform(v));

    }

}
