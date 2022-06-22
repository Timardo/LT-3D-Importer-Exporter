package net.timardo.lt3dimporter.importer;

import de.javagl.obj.FloatTuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.timardo.lt3dimporter.LT3DImporter;

/**
 * Class representing a single triangle of a 3d model. Defined by 3 points. Can contain a uv mappings for texture.
 * 
 * @author Timardo
 *
 */
public class Triangle {
    public Vec3d a;
    public Vec3d b;
    public Vec3d c;
    public double[] u; // [a, b, c]
    public double[] v; // ...
    //public double[] w; not currently implemented until I find an obj with a 3d texture
    public boolean t; // whether the Triangle has texture coords for its points
    
    public Triangle(FloatTuple a, FloatTuple b, FloatTuple c) {
        this.a = new Vec3d(a.getX(), a.getY(), a.getZ());
        this.b = new Vec3d(b.getX(), b.getY(), b.getZ());
        this.c = new Vec3d(c.getX(), c.getY(), c.getZ());
        this.t = false;
    }

    public void calcBlocks(float minPrecision, double scale, ConvertedModel output, String material) throws ImportException {
        Vec3d sA = a.scale(scale); // scale BEFORE processing
        Vec3d sB = b.scale(scale);
        Vec3d sC = c.scale(scale);

        Vec3d vectAC = sA.subtractReverse(sC); // make vectors
        Vec3d vectBC = sB.subtractReverse(sC);
        double slices = vectAC.lengthVector() / minPrecision + 2d;
        
        for (int i = 0; i <= slices; i++) {
            double t = i / slices; // ratio
            Vec3d p1 = sA.add(vectAC.scale(t));
            double[] uv1 = this.t ? new double[] { u[0] + (u[2] - u[0]) * t, v[0] + (v[2] - v[0]) * t } : null; // uv mapping for p1
            Vec3d p2 = sB.add(vectBC.scale(t));
            double[] uv2 = this.t ? new double[] { u[1] + (u[2] - u[1]) * t, v[1] + (v[2] - v[1]) * t } : null; // uv mapping for p2
            
            double r = 0D;
            int a1 = MathHelper.ceil(p1.x);
            int a2 = MathHelper.ceil(p2.x);
            int min = Math.min(a1, a2);
            int max = Math.max(a1, a2);
            
            for (int k = min; k < max; k++) {
                r = Math.abs((double) k - p1.x) / Math.abs(p2.x - p1.x); // param for uv mapping 
                output.addTile(new BlockPos(getIntermediate(p1, p2, (double) k, 0)), this.t ? new double[] { uv1[0] + (uv2[0] - uv1[0]) * r, uv1[1] + (uv2[1] - uv1[1]) * r } : null, material);
            }
            
            a1 = MathHelper.ceil(p1.y);
            a2 = MathHelper.ceil(p2.y);
            min = Math.min(a1, a2);
            max = Math.max(a1, a2);
            
            for (int k = min; k < max; k++) {
                r = Math.abs((double) k - p1.y) / Math.abs(p2.y - p1.y);
                output.addTile(new BlockPos(getIntermediate(p1, p2, (double) k, 1)), this.t ? new double[] { uv1[0] + (uv2[0] - uv1[0]) * r, uv1[1] + (uv2[1] - uv1[1]) * r } : null, material);
            }
            
            a1 = MathHelper.ceil(p1.z);
            a2 = MathHelper.ceil(p2.z);
            min = Math.min(a1, a2);
            max = Math.max(a1, a2);
            
            for (int k = min; k < max; k++) {
                r = Math.abs((double) k - p1.z) / Math.abs(p2.z - p1.z);
                output.addTile(new BlockPos(getIntermediate(p1, p2, (double) k, 2)), this.t ? new double[] { uv1[0] + (uv2[0] - uv1[0]) * r, uv1[1] + (uv2[1] - uv1[1]) * r } : null, material);
            }
        }
    }
    
    public void addTexCoords(FloatTuple a, FloatTuple b, FloatTuple c) {
        this.u = new double[] { a.getX(), b.getX(), c.getX() };
        this.v = new double[] { a.getY(), b.getY(), c.getY() };
        //this.w = new double[] { a.getZ(), b.getZ(), c.getZ() };
        this.t = true;
    }
    
    /**
     * Tweaked version of a method in {@link Vec3d}
     * 
     * @param p1 - first point (start of the vector)
     * @param p2 - second point
     * @param n - value to be scaled the vector to
     * @param p - 0 for scaling X, 1 for Y and 2 for Z, screw Enums
     * 
     * @return new {@link Vec3d} of a point between p1 and p2 scaled with chosen coordinate to n
     * @throws ImportException 
     */
    private static Vec3d getIntermediate(Vec3d p1, Vec3d p2, double n, int p) throws ImportException {
        double dX = p2.x - p1.x;
        double dY = p2.y - p1.y;
        double dZ = p2.z - p1.z;

        if ((p == 0 && dX == 0) || (p == 1 && dY == 0) || (p == 2 && dZ == 0)) {
            return p1; // distance is 0 and there is no need to calculate another point
        } else {
            double t = p == 0 ? (n - p1.x) / dX : p == 1 ? (n - p1.y) / dY : (n - p1.z) / dZ;
            
            if (t >= 0.0D && t <= 1.0D) {// we don't want to go out of the vector
                return new Vec3d(p1.x + dX * t, p1.y + dY * t, p1.z + dZ * t);
            } else {
                LT3DImporter.logger.error("Error getting intermediate value! Expected t from <0,1>, got " + t + ". Base points, 'p' and 'n': " + p1 + ", " + p2 + ", " + p + ", " + n);
                throw new ImportException("An error occured while converting model, check log for more information!"); // this should never happen, but safety first
            }
        }
    }
}
