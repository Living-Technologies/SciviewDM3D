package org.livingtech.scbridge;

import deformablemesh.MeshImageStack;
import deformablemesh.geometry.BinaryMeshGenerator;
import deformablemesh.geometry.CurvatureCalculator;
import deformablemesh.geometry.DeformableMesh3D;
import deformablemesh.geometry.Triangle3D;
import graphics.scenery.Mesh;
import graphics.scenery.Node;
import ij.ImagePlus;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.mesh.Triangle;
import net.imglib2.mesh.Vertex;
import net.imglib2.mesh.alg.MarchingCubesRealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import sc.iview.SciView;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class MarchingCubesCheck {

    public static Img<UnsignedByteType> image(){
        final ImgFactory< UnsignedByteType > factory = new ArrayImgFactory<>(new UnsignedByteType(0));
        ArrayImg<UnsignedByteType, ByteArray> img = (ArrayImg<UnsignedByteType, ByteArray>)factory.create(new int[]{9, 9, 9});
        RandomAccess<UnsignedByteType> r = img.randomAccess();

        for(int i = 3; i<6; i++){
            for(int j = 3; j<6; j++) {
                for (int k = 3; k < 6; k++) {
                    UnsignedByteType bt = r.setPositionAndGet(i, j, k);
                    bt.set(1);
                }
            }
        }
        return img;
    }
    public static Mesh convert(net.imglib2.mesh.Mesh il2mesh ){
        Mesh remesh = new Mesh();

        final int n = il2mesh.triangles().size();
        final int nodes = il2mesh.vertices().size();

        FloatBuffer pos = FloatBuffer.allocate(nodes*3);
        FloatBuffer normals = FloatBuffer.allocate(nodes*3);
        IntBuffer indexes = IntBuffer.allocate(n*3);

        int i = 0;
        for(Vertex v : il2mesh.vertices()){
            double[] pt = v.positionAsDoubleArray();

            pos.put((float)pt[0]);
            pos.put((float)pt[1]);
            pos.put((float)pt[2]);
            double[] norm = {0, 0, 0};
            normals.put((float)norm[0]);
            normals.put((float)norm[1]);
            normals.put((float)norm[2]);
        }

        for(Triangle t : il2mesh.triangles()){
            indexes.put((int)t.vertex0());
            indexes.put((int)t.vertex1());
            indexes.put((int)t.vertex2());
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

    static Mesh getVoxelMesh(Img<UnsignedByteType> img){
        List<int[]> points = new ArrayList<>();
        for(int i = 3; i<6; i++){
            for(int j = 3; j<6; j++){
                for(int k = 3; k<6; k++){
                    points.add(new int[]{i,j,k});
                }
            }
        }
        ImagePlus plus = ImageJFunctions.wrap(img, "sample volume");
        plus.setDimensions(1, 9, 1);
        MeshImageStack mis = new MeshImageStack(plus);
        DeformableMesh3D dm3d = BinaryMeshGenerator.voxelMesh(points, mis, 1 );
        Dm3dMeshToScMesh dm3dToSv = new Dm3dMeshToScMesh(plus);
        return dm3dToSv.convertMesh(dm3d);

    }

    public static void main(String[] args) throws Exception {
        SciView sv = SciView.create();
        Img<UnsignedByteType> img = image();
        Node volume = sv.addVolume(img);
        net.imglib2.mesh.Mesh mesh = MarchingCubesRealType.calculate(img, 1);
        Mesh m = convert(mesh);
        m.material().setWireframe(true);
        sv.addNode(m, volume);
        Mesh voxels = getVoxelMesh(img);
        voxels.material().setWireframe(true);
        sv.addNode(voxels, volume);
    }

}
