package net.timardo.lt3dimporter.obj3d;

import java.util.Arrays;

import de.javagl.obj.FloatTuple;

/**
 * 
 * @author javagl
 *
 */
public class LightFloatTuple implements FloatTuple {
    /**
     * The values of this tuple
     */
    private final float values[];
    
    /**
     * Creates a new DefaultFloatTuple with the given values
     *
     * @param values The values
     */
    LightFloatTuple(float values[]) {
        this.values = values;
    }
    
    /**
     * Creates a new DefaultFloatTuple with the given values
     * 
     * @param x The x value
     * @param y The y value
     * @param z The z value
     * @param w The w value
     */
    LightFloatTuple(float x, float y, float z, float w) {
        this(new float[]{x,y,z,w});
    }

    /**
     * Creates a new DefaultFloatTuple with the given values
     * 
     * @param x The x value
     * @param y The y value
     * @param z The z value
     */
    LightFloatTuple(float x, float y, float z) {
        this(new float[]{x,y,z});
    }

    /**
     * Creates a new DefaultFloatTuple with the given values
     * 
     * @param x The x value
     * @param y The y value
     */
    LightFloatTuple(float x, float y) {
        this(new float[]{x,y});
    }

    /**
     * Creates a new DefaultFloatTuple with the given value
     * 
     * @param x The x value
     */
    LightFloatTuple(float x) {
        this(new float[]{x});
    }
    
    
    /**
     * Copy constructor.
     * 
     * @param other The other FloatTuple
     */
    LightFloatTuple(FloatTuple other) {
        this(getValues(other));
    }
    
    /**
     * Returns the values of the given {@link FloatTuple} as an array
     * 
     * @param f The {@link FloatTuple}
     * @return The values
     */
    private static float[] getValues(FloatTuple f) {
        if (f instanceof LightFloatTuple) {
            LightFloatTuple other = (LightFloatTuple)f;
            return other.values.clone();
        }
        
        float values[] = new float[f.getDimensions()];
        
        for (int i=0; i<values.length; i++) {
            values[i] = f.get(i);
        }
        
        return values;
    }

    @Override
    public float get(int index) {
        return values[index];
    }
    
    @Override
    public float getX() {
        return values[0];
    }

    /**
     * Set the given component of this tuple
     * 
     * @param x The component to set
     * @throws IndexOutOfBoundsException If this tuple has less than 1 
     * dimensions
     */
    void setX(float x) {
        values[0] = x;
    }

    @Override
    public float getY() {
        return values[1];
    }

    /**
     * Set the given component of this tuple
     * 
     * @param y The component to set
     * @throws IndexOutOfBoundsException If this tuple has less than 2 
     * dimensions
     */
    void setY(float y) {
        values[1] = y;
    }

    @Override
    public float getZ() {
        return values[2];
    }

    /**
     * Set the given component of this tuple
     * 
     * @param z The component to set
     * @throws IndexOutOfBoundsException If this tuple has less than 3 
     * dimensions
     */
    void setZ(float z) {
        values[2] = z;
    }

    @Override
    public float getW() {
        return values[3];
    }
    
    /**
     * Set the given component of this tuple
     * 
     * @param w The component to set
     * @throws IndexOutOfBoundsException If this tuple has less than 4 
     * dimensions
     */
    void setW(float w) {
        values[3] = w;
    }

    @Override
    public int getDimensions() {
        return values.length;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        
        for (int i=0; i<getDimensions(); i++) {
            sb.append(get(i));
            
            if (i < getDimensions()-1) {
                sb.append(",");
            }
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        
        if (object instanceof LightFloatTuple) {
            LightFloatTuple other = (LightFloatTuple)object;
            return Arrays.equals(values, other.values);
        }
        
        if (object instanceof FloatTuple) {
            FloatTuple other = (FloatTuple)object;
            
            if (other.getDimensions() != getDimensions()) return false;
            
            for (int i=0; i<getDimensions(); i++) {
                if (get(i) != other.get(i)) return false;
            }
            
            return true;
        }
        
        return false;
    }
}
