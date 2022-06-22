/*
 * www.javagl.de - Obj
 *
 * Copyright (c) 2008-2015 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package net.timardo.lt3dimporter.obj3d;

import java.util.ArrayList;
import java.util.List;

import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjGroup;
import net.timardo.lt3dimporter.importer.Triangle;

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
