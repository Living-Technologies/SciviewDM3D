package org.livingtech.scbridge;

import deformablemesh.geometry.DeformableMesh3D;
import deformablemesh.geometry.Node3D;
import deformablemesh.geometry.RayCastMesh;
import graphics.scenery.Mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class TestMeshes {
    public static Mesh convert(DeformableMesh3D mesh){
        Dm3dMeshToScMesh convert = new Dm3dMeshToScMesh(1, 1, 1, 1, 1, 1);
        return convert.convertMesh(mesh);
    }
    public static DeformableMesh3D getTetrahedron(){

        double[] pts = {
                -1, 0, 1,
                -1, 0, -1,
                1, 0, 1,
                1, 0, -1,
                0, 1.5, 0
        };
        int a = 0;
        int b = 1;
        int c = 2;
        int d = 3;
        int e = 4;
        int[] connections = {
                a, b,
                a, c,
                a, e,
                b, c,
                b, d,
                b, e,
                c, e,
                c, d,
                d, e
        };
        int[] triangles = {
                a, c, b,
                b, c, d,
                e, a, b,
                a, e, c,
                e, d, c,
                d, e, b
        };
        return new DeformableMesh3D(pts, connections, triangles);
    }
    public static Mesh scTet(){
        float[][] pts = {
                {-1, 0, 1},
                {-1, 0, -1},
                {1, 0, 1},
                {1, 0, -1},
                {0, 1.5f, 0}
        };
        int a = 0;
        int b = 1;
        int c = 2;
        int d = 3;
        int e = 4;
        int[] triangles = {
                a, c, b,
                b, c, d,
                e, a, b,
                a, e, c,
                e, d, c,
                d, e, b};
        FloatBuffer vtx = FloatBuffer.allocate(triangles.length*3);
        FloatBuffer uv = FloatBuffer.allocate(triangles.length*2);
        for(int t: triangles){
            vtx.put(pts[t]);
        }
        vtx.flip();
        Mesh m = new Mesh();
        m.geometry().setVertices(vtx);
        m.geometry().setTexcoords(uv);
        m.geometry().recalculateNormals();
        m.geometry().setDirty(true);
        return m;
    }
    public static Mesh twoTriangles(){
        float[] pts = {
                -1, -1, 0,
                1, -1, 0,
                1, 1, 0,
                -1, 1, 0
        };
        float[] nm = {
                0, 0, -1,
                0, 0, -1,
                0, 0, -1,
                0, 0, -1
        };

        float[] uv = {
                0, 0,
                0, 0,
                0, 0,
                0, 0


        };

        int[] tas = {
                0, 2, 1,
                2, 0, 3
        };

        Mesh m = new Mesh();
        FloatBuffer loc = FloatBuffer.wrap(pts);
        FloatBuffer norm = FloatBuffer.wrap(nm);
        FloatBuffer uvb = FloatBuffer.wrap(uv);
        IntBuffer indexes = IntBuffer.wrap(tas);


        m.geometry().setVertices(loc);
        m.geometry().setIndices(indexes);
        m.geometry().setNormals(norm);
        m.geometry().setTexcoords(uvb);
        m.geometry().setDirty(true);
        return m;
    }

    public static Mesh generateSphere(int divisions){
        DeformableMesh3D sphere = RayCastMesh.sphereRayCastMesh(divisions);
        Dm3dMeshToScMesh adapter = new Dm3dMeshToScMesh(1, 1,1, 1, 1, 1);
        return adapter.convertMesh(sphere);
    }

    public static Mesh barbell(int divisions){
        DeformableMesh3D sphere = RayCastMesh.sphereRayCastMesh(divisions);
        for(Node3D node: sphere.nodes){
            double[] pos = node.getCoordinates();
            double z = pos[2];
            double theta = Math.PI/2 *( 1 + z);
            double f = 0.8*Math.abs(Math.cos(theta)) + 0.2;
            double dx = f*pos[0];
            double dy = f*pos[1];
            node.setPosition(new double[]{dx, dy, z});
        }
        Dm3dMeshToScMesh adapter = new Dm3dMeshToScMesh(1, 1,1, 1, 1, 1);
        return adapter.convertMesh(sphere);
    }

}
