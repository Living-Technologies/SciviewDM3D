package org.livingtech.scbridge;

import graphics.scenery.Mesh;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.List;

public class Plane {
    static final Vector3fc up = new Vector3f(0, 0, 1);
    private Vector3f normal = new Vector3f(0, 0, 1);
    private Vector3f pos = new Vector3f(0f, 0f, 0f);
    record Points(Vector3fc a, Vector3fc b, Vector3fc c, Vector3fc d) implements Iterable<Vector3fc> {
        @Override
        public Iterator<Vector3fc> iterator() {
            return List.of(a, b, c, d).iterator();
        }
    }
    private final Points points;
    private final Mesh mesh;

    /**
     * Creates a new plane, and adds it to the sciview context.
     *
     * The plane is a set of 4 triangle that make a square (forward and
     * backward facing).
     *
     * @param context This is needed for updating the mesh.
     * @param length the size of the square to represent this plane.
     */
    public Plane(float length) {
        points = new Points(
                new Vector3f(-length, -length, 0),
                new Vector3f(length, -length, 0),
                new Vector3f(length, length, 0),
                new Vector3f(-length, length, 0)
        );

        mesh = newMesh();

        update();
    }

    /**
     * Creates a duplicate mesh with the appropriate style information.
     * This will override any changes made from the UI. Eg. color
     *
     * @return
     */
    Mesh newMesh() {
        Mesh nextMesh = new Mesh("furrow plane");
        nextMesh.material().setDiffuse(new Vector3f(0, 0, 1f));

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
            position.put(pt.x());
            position.put(pt.y());
            position.put(pt.z());

            normals.put(normal.x);
            normals.put(normal.y);
            normals.put(normal.z);

        }
        position.flip();
        normals.flip();


        nextMesh.geometry().setVertices(position);
        nextMesh.geometry().setNormals(normals);
        nextMesh.geometry().setIndices(indexes);
        nextMesh.geometry().setTexcoords(uv);

        return nextMesh;
    }

    Mesh getMesh(){
        return mesh;
    }

    /**
     * Sets the normal value and updates the geometry in the sciview context.
     * @param n xyz direction. Assumed to be normalized.
     */
    public void setNormal(double[] n) {
        normal = new Vector3f((float) n[0], (float) n[1], (float) n[2]);
        update();
    }

    /**
     * Displaces the center of the mesh by the provided values.
     *
     * @param delta
     */
    public void move(double[] delta) {
        pos = new Vector3f(
                pos.x + (float) delta[0],
                pos.y + (float) delta[1],
                pos.z + (float) delta[2]);
        update();
    }

    /**
     * Calculates the axis/angle required to rotate the normal (front facing)
     * of the plane along the desired normal.
     *
     * @return a quaternion derived from the axis-angle rotation.
     */
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

    /**
     * Updates the position and orientation of the mesh by
     * applying a rotation.
     */
    public void update() {
        mesh.spatial().setRotation(calculateRotationTransform());
        mesh.spatial().setPosition(pos);
        mesh.geometry().setDirty(true);

    }
}
