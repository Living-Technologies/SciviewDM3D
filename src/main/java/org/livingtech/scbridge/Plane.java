package org.livingtech.scbridge;

import graphics.scenery.Mesh;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import sc.iview.SciView;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.List;

public class Plane {
    final SciView context;
    static final Vector3fc up = new Vector3f(0, 0, 1);
    Vector3f normal = new Vector3f(0, 0, 1);
    Vector3f pos = new Vector3f(0f, 0f, 0f);
    record Points(Vector3fc a, Vector3fc b, Vector3fc c, Vector3fc d) implements Iterable<Vector3fc> {
        @Override
        public Iterator<Vector3fc> iterator() {
            return List.of(a, b, c, d).iterator();
        }
    }
    final Points points;
    Mesh mesh;

    public Plane(SciView context, float length) {
        points = new Points(
                new Vector3f(-length, -length, 0),
                new Vector3f(length, -length, 0),
                new Vector3f(length, length, 0),
                new Vector3f(-length, length, 0)
        );
        this.context = context;
        update();

    }

    Mesh newMesh() {
        Mesh m = new Mesh("furrow plane");
        m.material().setDiffuse(new Vector3f(0, 0, 1f));
        return m;
    }

    public void setNormal(double[] n) {
        normal = new Vector3f((float) n[0], (float) n[1], (float) n[2]);
        update();
    }

    public void move(double[] delta) {
        pos = new Vector3f(
                pos.x + (float) delta[0],
                pos.y + (float) delta[1],
                pos.z + (float) delta[2]);
        update();
    }

    Quaternionf calculateRotationTransform() {
        Vector3f x = up.cross(normal, new Vector3f());

        float l = x.length();
        if (l != 0) {
            x.normalize();
        } else {
            x = new Vector3f(0, 1f, 0);
        }
        float angle = (float) Math.asin(l);

        if (up.dot(normal) < 0) {
            angle = (float) (Math.PI - angle);
        }

        return new Quaternionf(new AxisAngle4f(angle, x));
    }

    public void update() {
        int vertexes = 4;
        int triangles = 4;

        IntBuffer indexes = IntBuffer.wrap(new int[]{
                0, 2, 3,
                0, 1, 2,
                0, 3, 2,
                0, 2, 1
        });

        FloatBuffer uv = FloatBuffer.wrap(new float[2 * vertexes]);
        FloatBuffer position = FloatBuffer.allocate(12);
        FloatBuffer normals = FloatBuffer.allocate(12);

        Quaternionf rot = calculateRotationTransform();

        for (Vector3fc pt : points) {
            Vector3f transformed = rot.transform(new Vector3f(pt)).add(pos);
            position.put(transformed.x);
            position.put(transformed.y);
            position.put(transformed.z);

            normals.put(normal.x);
            normals.put(normal.y);
            normals.put(normal.z);

        }
        position.flip();
        normals.flip();

        Mesh nextMesh = newMesh();
        nextMesh.geometry().setVertices(position);
        nextMesh.geometry().setNormals(normals);
        nextMesh.geometry().setIndices(indexes);
        nextMesh.geometry().setTexcoords(uv);

        if (mesh != null) {
            context.deleteNode(mesh);
        }
        mesh = nextMesh;
        context.addNode(mesh);
    }
}
