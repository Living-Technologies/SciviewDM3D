package org.livingtech.scbridge;

import graphics.scenery.Mesh;
import graphics.scenery.Sphere;
import graphics.scenery.attribute.geometry.Geometry;
import org.joml.Vector3f;
import sc.iview.SciView;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class AnimateThat {
    static class DynamicMesh{
        int w = 256;
        float length = 5;
        float dx = length/w;
        int[] indexes = new int[w*w*2*3];
        float[] positions = new float[(w+1)*(w+1)*3];
        float[] normals = new float[(w+1)*(w+1)*3];
        Mesh mesh = new Mesh();
        double time = 0.0;
        int modes = 10;
        double velocity = 1;
        double[] xModes;
        double[] yModes;
        double dt = 1e-2;
        public void prepare(){
            Geometry geo = mesh.geometry();
            xModes = new double[modes];
            yModes = new double[modes];
            for(int i = 0; i<modes; i++){
                double m = 2./(i + 1);
                m = m*m;
                xModes[i] = (1 - 2*Math.random())*m;
                yModes[i] = (1 - 2*Math.random())*m;
            }
            for(int i = 0; i<=w; i++){
                for(int j = 0; j<=w; j++){
                    int dex = i * (w + 1) + j;
                    float x = j*dx - length/2;
                    float y = i*dx - length/2;
                    positions[dex*3 + 0] = x;
                    positions[dex*3 + 1] = y;
                    positions[dex*3 + 2] = getZ(x/length, y/length);
                }
            }
            calculateNormals();

            for(int i = 0; i<w; i++){
                for(int j = 0; j<w; j++){
                    int box = w * i + j;
                    indexes[box*6 + 0] = i*(w+1) + j;
                    indexes[box*6 + 1] = (i+1)*(w+1) + j;
                    indexes[box*6 + 2] = i*(w+1) + (j+1);
                    indexes[box*6 + 3] = (i+1)*(w+1) + j;
                    indexes[box*6 + 4] = (i+1)*(w+1) + (j+1);
                    indexes[box*6 + 5] = i*(w+1) + (j+1);
                }
            }

            geo.setVertices(FloatBuffer.wrap(positions));
            geo.setNormals(FloatBuffer.wrap(normals));
            geo.setTexcoords(FloatBuffer.allocate(2*(w+1)*(w+1)));
            geo.setIndices(IntBuffer.wrap(indexes));
            mesh.material().setDiffuse(new Vector3f(0.75f, 0.75f, 0.5f));
            mesh.material().setSpecular(new Vector3f(1f, 1f, 1f));
            //mesh.material().setWireframe(true);
        }

        void calculateNormals(){
            for(int i = 0; i<w; i++){
                for(int j = 0; j<w; j++){
                    int dex = i * (w + 1) + j;
                    Vector3f a = new Vector3f(
                            positions[3*dex], positions[3*dex + 1], positions[3*dex+2]
                    );
                    int dex1 = (i + 1) * (w + 1) + j;
                    Vector3f b = new Vector3f(
                            positions[3*dex1], positions[3*dex1 + 1], positions[3*dex1+2]
                    );
                    int dex2 = i * (w + 1) + j + 1;
                    Vector3f c = new Vector3f(
                            positions[3*dex2], positions[3*dex2 + 1], positions[3*dex2+2]
                    );
                    Vector3f r0 = b.sub(a);
                    Vector3f r1 = c.sub(a);
                    r0.cross(r1);
                    r0.normalize();
                    normals[3*dex + 0 ] = -r0.x;
                    normals[3*dex + 1 ] = -r0.y;
                    normals[3*dex + 2 ] = -r0.z;

                    normals[3*dex1 + 0 ] = -r0.x;
                    normals[3*dex1 + 1 ] = -r0.y;
                    normals[3*dex1 + 2 ] = -r0.z;

                    normals[3*dex2 + 0 ] = -r0.x;
                    normals[3*dex2 + 1 ] = -r0.y;
                    normals[3*dex2 + 2 ] = -r0.z;

                }
            }
        }

        double fourier(double position, int mode){

            double kj = Math.PI *( mode + 1 );
            double wj = kj*velocity;
            return Math.sin(position*kj)*Math.cos( wj*time );
        }

        float getZ(double x, double y){
            double z = 0;
            double fx = 0 ;
            double fy = 0;
            for(int i = 0; i<xModes.length; i++){
                fx += xModes[i] * fourier(x, i);
            }
            for(int j = 0; j<yModes.length; j++){
                fy += yModes[j] * fourier(y, j);
            }

            return (float)(fx*fy);
        }

        public void update(){
            Geometry geo = mesh.geometry();
            time += dt;
            for(int i = 0; i<=w; i++){
                for(int j = 0; j<=w; j++){
                    int dex = i * (w + 1) + j;
                    double x = positions[dex*3];
                    double y = positions[dex*3 + 1];
                    positions[dex*3 + 2] = getZ(x/length + 0.5, y/length + 0.5);
                }
            }

            calculateNormals();

            geo.setVertices(FloatBuffer.wrap(positions));
            geo.setNormals(FloatBuffer.wrap(normals));
            geo.setDirty(true);
            //geo.recalculateNormals();
        }
    }
    static class DynamicSphere{
        Sphere obj;
        Vector3f pos;
        Vector3f vel;
        float radius;
    }
    DynamicMesh mesh;
    List<DynamicSphere> spheres = new ArrayList<>();
    public AnimateThat(){
        mesh = new DynamicMesh();
        mesh.prepare();

    }
    Mesh getSceneryMesh(){
        return mesh.mesh;
    }
    public void update(){
        mesh.update();
    }
    public static void main(String[] args) throws Exception {
        AnimateThat that = new AnimateThat();

        SciView sciView = SciView.create();
        sciView.addNode(that.getSceneryMesh());
        new Thread( () ->{
            while(!Thread.interrupted()){
                that.update();
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

    }
}
