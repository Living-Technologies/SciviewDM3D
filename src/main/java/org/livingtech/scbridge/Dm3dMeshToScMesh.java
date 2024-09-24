package org.livingtech.scbridge;

import deformablemesh.geometry.CurvatureCalculator;
import deformablemesh.geometry.DeformableMesh3D;
import deformablemesh.geometry.Triangle3D;
import graphics.scenery.Mesh;
import ij.ImagePlus;
import ij.measure.Calibration;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Dm3dMeshToScMesh {
        final double SCALE;
        final double ox, oy, oz;
        final double sx, sy, sz;
        public Dm3dMeshToScMesh(ImagePlus plus){
            Calibration c = plus.getCalibration();
            double dx = c.pixelWidth;
            double dy = c.pixelHeight;
            double dz = c.pixelDepth;
            double lx = dx*plus.getWidth();
            double ly = dy*plus.getHeight();
            double lz = dz*plus.getNSlices();
            SCALE = lx > ly ?
               lx > lz ? lx : lz :
               ly > lz ? ly : lz;

            ox = lx/SCALE*0.5;
            oy = ly/SCALE*0.5;
            oz = lz/SCALE*0.5;

            sx = SCALE/dx;
            sy = SCALE/dy;
            sz = SCALE/dz;
        }

    /**
     * No arguments... no scaling.
     */
    public Dm3dMeshToScMesh(){
            this(1, 1, 1, 1, 1, 1);
        }

        public Dm3dMeshToScMesh(int px, int py, int pz, double dx, double dy, double dz){
            double lx = dx*px;
            double ly = dy*py;
            double lz = dz*pz;
            SCALE = lx > ly ?
                    lx > lz ? lx : lz :
                    ly > lz ? ly : lz;

            ox = lx/SCALE*0.5;
            oy = ly/SCALE*0.5;
            oz = lz/SCALE*0.5;

            sx = SCALE/dx;
            sy = SCALE/dy;
            sz = SCALE/dz;
        }
    public double[] getImageCoordinates(double[] r) {
        return new double[] {
                (r[0] + ox)*sx,
                (r[1] + oy)*sy,
                (r[2] + oz)*sz
        };

    }
    public Mesh convertMesh(DeformableMesh3D mesh){

        Mesh remesh = new Mesh();

        final int n = mesh.triangles.size();
        final int nodes = mesh.nodes.size();

        FloatBuffer pos = FloatBuffer.allocate(nodes*3);
        FloatBuffer normals = FloatBuffer.allocate(nodes*3);
        IntBuffer indexes = IntBuffer.allocate(n*3);

        CurvatureCalculator cc = new CurvatureCalculator(mesh);

        for(int i = 0; i<nodes; i++){
            double[] cn = {mesh.positions[3*i], mesh.positions[3*i+1], mesh.positions[3*i+2]};

            double[] pt = getImageCoordinates(cn);
            pos.put((float)pt[0] - 0.5f);
            pos.put((float)pt[1] - 0.5f);
            pos.put((float)pt[2] - 0.5f);
            double[] norm = cc.getNormal(i);
            normals.put((float)norm[0]);
            normals.put((float)norm[1]);
            normals.put((float)norm[2]);
        }

        for(int i = 0; i<n; i++){
            Triangle3D t = mesh.triangles.get(i);
            indexes.put(t.getIndices());
        }
        FloatBuffer uv = FloatBuffer.allocate(nodes*2);
        pos.flip();
        normals.flip();
        indexes.flip();
        remesh.geometry().setVertices(pos);
        remesh.geometry().setNormals(normals);
        remesh.geometry().setTexcoords(uv);
        remesh.geometry().setIndices(indexes);

        remesh.setBoundingBox(remesh.generateBoundingBox());
        remesh.geometry().setDirty(true);
        return remesh;
    }
}
