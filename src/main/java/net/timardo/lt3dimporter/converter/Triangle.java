package net.timardo.lt3dimporter.converter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import de.javagl.obj.FloatTuple;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Triangle {

    public Vec3d a;
    public Vec3d b;
    public Vec3d c;
    public double[] u; // [a, b, c]
    public double[] v; // ...
    //public double[] w; not yet implemented until I find an obj with 3d texture
    public boolean t; // whether the Triangle has texture coords for its points
    
    public Triangle(FloatTuple a, FloatTuple b, FloatTuple c) {
        this.a = new Vec3d(a.getX(), a.getY(), a.getZ());
        this.b = new Vec3d(b.getX(), b.getY(), b.getZ());
        this.c = new Vec3d(c.getX(), c.getY(), c.getZ());
        this.t = false;
    }
    
    public Map<Long, Double[]> calcBlocks(double minPrecision, double scale) {
        return calcBlocks(minPrecision, scale, new BlockPos(0, 0, 0));
    }

    public Map<Long, Double[]> calcBlocks(double minPrecision, double scale, BlockPos relativePos) {
        Vec3d sA = a.scale(scale); // scale BEFORE processing
        Vec3d sB = b.scale(scale);
        Vec3d sC = c.scale(scale);
        Vec3d vectAC = sA.subtractReverse(sC); // make vectors
        Vec3d vectBC = sB.subtractReverse(sC);
        double slices = vectAC.lengthVector() / minPrecision + 2d;
        HashMap<Long, Double[]> blocks = new LinkedHashMap<Long, Double[]>();
        
        for (int i = 0; i <= slices; i++) {
            double t = i / slices; // ratio
            Vec3d p1 = sA.add(vectAC.scale(t));
            double[] uv1 = this.t ? new double[] { u[0] + (u[2] - u[0]) * t, v[0] + (v[2] - v[0]) * t } : null; // uv mapping for p1
            Vec3d p2 = sB.add(vectBC.scale(t));
            double[] uv2 = this.t ? new double[] { u[1] + (u[2] - u[1]) * t, v[1] + (v[2] - v[1]) * t } : null; // uv mapping for p2
            Vec3d v = new Vec3d(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z); // vector
            double subSlices = v.lengthVector() / minPrecision + 2d; // number of slices in new line
            
            for (int j = 0; j <= subSlices; j++) {
                double u = j / subSlices;
                blocks.put(new BlockPos(p1.add(v.scale(u))).toLong(), this.t ? new Double[] { uv1[0] + (uv2[0] - uv1[0]) * u, uv1[1] + (uv2[1] - uv1[1]) * u } : null);
            }
        }
        
        return blocks;
    }
    
    public void addTexCoords(FloatTuple a, FloatTuple b, FloatTuple c) {
        this.u = new double[] { a.getX(), b.getX(), c.getX() };
        this.v = new double[] { a.getY(), b.getY(), c.getY() };
        //this.w = new double[] { a.getZ(), b.getZ(), c.getZ() };
        this.t = true;
    }
}
