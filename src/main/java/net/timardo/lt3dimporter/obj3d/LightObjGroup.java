package net.timardo.lt3dimporter.obj3d;

import java.util.ArrayList;
import java.util.List;

import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjGroup;

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
     * Creates a new ObjGroup with the given name
     * 
     * @param name The name of this ObjGroup
     */
    LightObjGroup(String name) {
        this.name = name;
        faces = new ArrayList<ObjFace>();
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Add the given face to this group
     * 
     * @param face The face to add
     */
    void addFace(ObjFace face) {
        faces.add(face);
    }

    @Override
    public int getNumFaces() {
        return faces.size();
    }

    @Override
    public ObjFace getFace(int index) {
        return faces.get(index);
    }

    @Override
    public String toString() {
        return "ObjGroup[name=" + name + ",#faces=" + faces.size() + "]";
    }
}
