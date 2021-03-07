package net.timardo.lt3dimporter.obj3d;

import java.util.ArrayList;
import java.util.List;

import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjGroup;
import net.timardo.lt3dimporter.converter.Triangle;

public class LightObjGroup implements ObjGroup {
    /**
     * The name of this group.
     */
    private String name;
    
    /**
     * The faces in this group
     */
    private List<ObjFace> faces;
    
    /**
     * Triangles in this group
     */
    public List<Triangle> triangles;

    /**
     * Creates a new ObjGroup with the given name
     * 
     * @param name The name of this ObjGroup
     */
    LightObjGroup(String name) {
        this.name = name;
        this.faces = new ArrayList<ObjFace>();
        this.triangles = new ArrayList<Triangle>(); 
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Add the given face to this group
     * 
     * @param face The face to add
     */
    void addFace(ObjFace face) {
        this.faces.add(face);
    }

    @Override
    public int getNumFaces() {
        return this.faces.size();
    }

    @Override
    public ObjFace getFace(int index) {
        return this.faces.get(index);
    }

    @Override
    public String toString() {
        return "ObjGroup[name=" + this.name + ",#faces=" + this.faces.size() + "]";
    }
}
