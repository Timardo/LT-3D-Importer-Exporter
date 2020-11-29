package net.timardo.lt3dimporter.obj3d;

import de.javagl.obj.ObjFace;

/**
 * 
 * @author javagl
 *
 */
public class LightObjFace implements ObjFace {
    /**
     * The vertex indices of this face
     */
    private final int vertexIndices[];
    
    /**
     * The texture coordinate indices of this face
     */
    private final int texCoordIndices[];
    
    /**
     * The normal indices of this face
     */
    private final int normalIndices[];

    /**
     * Creates a face from the given parameters. References to the
     * given objects will be stored.
     * 
     * @param vertexIndices The vertex indices
     * @param texCoordIndices The texture coordinate indices
     * @param normalIndices The normal indices
     */
    public LightObjFace(int vertexIndices[], int texCoordIndices[], int normalIndices[]) {
        this.vertexIndices = vertexIndices;
        this.texCoordIndices = texCoordIndices;
        this.normalIndices = normalIndices;
    }

    @Override
    public boolean containsTexCoordIndices() {
        return texCoordIndices != null;
    }

    @Override
    public boolean containsNormalIndices() {
        return normalIndices != null;
    }

    @Override
    public int getVertexIndex(int number) {
        return this.vertexIndices[number];
    }

    @Override
    public int getTexCoordIndex(int number) {
        return this.texCoordIndices[number];
    }

    @Override
    public int getNormalIndex(int number) {
        return this.normalIndices[number];
    }

    /**
     * Set the specified index to the given value
     * 
     * @param n The index to set
     * @param index The value of the index
     */
    void setVertexIndex(int n, int index) {
        vertexIndices[n] = index;
    }

    /**
     * Set the specified index to the given value
     * 
     * @param n The index to set
     * @param index The value of the index
     */
    void setNormalIndex(int n, int index) {
        normalIndices[n] = index;
    }

    /**
     * Set the specified index to the given value
     * 
     * @param n The index to set
     * @param index The value of the index
     */
    void setTexCoordIndex(int n, int index) {
        texCoordIndices[n] = index;
    }

    @Override
    public int getNumVertices() {
        return this.vertexIndices.length;
    }

    @Override
    public String toString() {
        String result = "ObjFace[";
        
        for(int i = 0; i < getNumVertices(); i++) {
            result += vertexIndices[i];
            
            if(texCoordIndices != null || normalIndices != null) {
                result += "/";
            }
            
            if(texCoordIndices != null) {
                result += texCoordIndices[i];
            }
            
            if(normalIndices != null) {
                result += "/" + normalIndices[i];
            }
            
            if(i < getNumVertices() - 1) {
                result += " ";
            }
        }
        
        return result += "]";
    }
}
