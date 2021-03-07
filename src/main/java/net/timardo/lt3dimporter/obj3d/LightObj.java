package net.timardo.lt3dimporter.obj3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjGroup;
import net.timardo.lt3dimporter.converter.Triangle;

/**
 * Slightly different implementation of {@link DefaultObj} to fit my needs.
 * Since the default implementation is final...
 * 
 * @author javagl
 *
 */
public class LightObj implements Obj {
     /**
     * The vertices in this Obj
     */
    private final List<FloatTuple> vertices;
    
    /**
     * The texture coordinates in this Obj.
     */
    private final List<FloatTuple> texCoords;

    /**
     * The normals in this Obj
     */
    private final List<FloatTuple> normals;

    /**
     * The faces in this Obj.
     */
    private final List<ObjFace> faces;

    /**
     * Group stuff, gonna use it now.
     */
    private final List<ObjGroup> groups;
    private final List<ObjGroup> materialGroups;
    private final Map<String, LightObjGroup> groupMap;
    private final Map<String, LightObjGroup> materialGroupMap;
    private List<String> mtlFileNames = Collections.emptyList();
    private final Map<ObjFace, Set<String>> startedGroupNames;
    private final Map<ObjFace, String> startedMaterialGroupNames;
    private Set<String> nextActiveGroupNames = null;
    private String nextActiveMaterialGroupName = null;
    private List<LightObjGroup> activeGroups = null;
    private Set<String> activeGroupNames = null;
    private LightObjGroup activeMaterialGroup = null;
    private String activeMaterialGroupName = null;
    
    public double[] boxCoords; // [maxX, maxY, maxZ, minX, minY, minZ]
    private double[] boxSize;
    private boolean isFinal;

    /**
     * Creates a new, empty LightObj.
     */
    public LightObj() {
        this.vertices = new ArrayList<FloatTuple>();
        this.normals = new ArrayList<FloatTuple>();
        this.texCoords = new ArrayList<FloatTuple>();
        this.faces = new ArrayList<ObjFace>();
        this.groups = new ArrayList<ObjGroup>();
        this.materialGroups = new ArrayList<ObjGroup>();
        this.groupMap = new LinkedHashMap<String, LightObjGroup>();
        this.materialGroupMap = new LinkedHashMap<String, LightObjGroup>();
        this.startedGroupNames = new HashMap<ObjFace, Set<String>>();
        this.startedMaterialGroupNames = new HashMap<ObjFace, String>();
        this.boxCoords = new double[] {
                -Double.MAX_VALUE, // maxX
                -Double.MAX_VALUE, // maxY
                -Double.MAX_VALUE, // maxZ
                Double.MAX_VALUE, // minX
                Double.MAX_VALUE, // minY
                Double.MAX_VALUE // minZ
        };
        this.setActiveGroupNames(Arrays.asList("default"));
        this.getGroupInternal("default");
        this.isFinal = false;
    }
    
    public LightObj(boolean isFinal) {
        this();
        this.isFinal = isFinal;
    }

    /**
     * Get length, width and height of this obj. The values in order are distances between max and min X, Y and Z coordinates
     * @return an array of 3 double values each representing one side of box in which this obj is.
     */
    public double[] getSides() {
        if (boxSize != null) return boxSize;
        
        this.boxSize = new double[] { 
            Math.abs(this.boxCoords[0] - this.boxCoords[3]), 
            Math.abs(this.boxCoords[1] - this.boxCoords[4]), 
            Math.abs(this.boxCoords[2] - this.boxCoords[5])
        };
        
        return this.boxSize;
    }

    @Override
    public int getNumVertices() {
        return this.vertices.size();
    }

    @Override
    public FloatTuple getVertex(int index) {
        return this.vertices.get(index);
    }

    @Override
    public int getNumTexCoords() {
        return this.texCoords.size();
    }

    @Override
    public FloatTuple getTexCoord(int index) {
        return this.texCoords.get(index);
    }

    @Override
    public int getNumNormals() {
        return this.normals.size();
    }

    @Override
    public FloatTuple getNormal(int index) {
        return this.normals.get(index);
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
    public Set<String> getActivatedGroupNames(ObjFace face) {
        return this.startedGroupNames.get(face);
    }
    
    @Override
    public String getActivatedMaterialGroupName(ObjFace face) {
        return this.startedMaterialGroupNames.get(face);
    }

    @Override
    public int getNumGroups() {
        return this.groups.size();
    }

    @Override
    public ObjGroup getGroup(int index) {
        return this.groups.get(index);
    }

    @Override
    public ObjGroup getGroup(String name) {
        return this.groupMap.get(name);
    }

    @Override
    public int getNumMaterialGroups() {
        return this.materialGroups.size();
    }

    @Override
    public ObjGroup getMaterialGroup(int index) {
        return this.materialGroups.get(index);
    }

    @Override
    public ObjGroup getMaterialGroup(String name) {
        return this.materialGroupMap.get(name);
    }


    @Override
    public List<String> getMtlFileNames() {
        return this.mtlFileNames;
    }

    @Override
    public void addVertex(FloatTuple vertex) {
        Objects.requireNonNull(vertex, "The vertex is null");
        
        this.vertices.add(vertex);
        
        if (vertex.getX() > this.boxCoords[0]) this.boxCoords[0] = vertex.getX();
        if (vertex.getY() > this.boxCoords[1]) this.boxCoords[1] = vertex.getY();
        if (vertex.getZ() > this.boxCoords[2]) this.boxCoords[2] = vertex.getZ();
        if (vertex.getX() < this.boxCoords[3]) this.boxCoords[3] = vertex.getX();
        if (vertex.getY() < this.boxCoords[4]) this.boxCoords[4] = vertex.getY();
        if (vertex.getZ() < this.boxCoords[5]) this.boxCoords[5] = vertex.getZ();
    }
    
    @Override
    public void addVertex(float x, float y, float z) {
        this.addVertex(new LightFloatTuple(x, y, z));
    }
    
    @Override
    public void addTexCoord(FloatTuple texCoord) {
        Objects.requireNonNull(texCoord, "The texCoord is null");
        this.texCoords.add(texCoord);
    }
    
    @Override
    public void addTexCoord(float x) {
        this.texCoords.add(new LightFloatTuple(x));
    }
    
    @Override
    public void addTexCoord(float x, float y) {
        this.texCoords.add(new LightFloatTuple(x, y));
    }
    
    @Override
    public void addTexCoord(float x, float y, float z) {
        this.texCoords.add(new LightFloatTuple(x, y, z));
    }
    

    @Override
    public void addNormal(FloatTuple normal) {
        Objects.requireNonNull(normal, "The normal is null");
        this.normals.add(normal);
    }

    @Override
    public void addNormal(float x, float y, float z) {
        this.addNormal(new LightFloatTuple(x, y, z));
    }
    
    @Override
    public void setActiveGroupNames(Collection<? extends String> groupNames) {
        if (groupNames == null) return;
        
        if (groupNames.size() == 0) {
            groupNames = Arrays.asList("default");
        } else if (groupNames.contains(null)) {
            throw new NullPointerException("The groupNames contains null");
        }
        
        this.nextActiveGroupNames = Collections.unmodifiableSet(new LinkedHashSet<String>(groupNames));
    }
    
    
    @Override
    public void setActiveMaterialGroupName(String materialGroupName) {
        if (materialGroupName == null) return;
        
        this.nextActiveMaterialGroupName = materialGroupName;
    }
    
    @Override
    public void addFace(ObjFace face) {
        if (face == null) {
            throw new NullPointerException("The face is null");
        }
        
        if (this.nextActiveGroupNames != null) {
            this.activeGroups = this.getGroupsInternal(this.nextActiveGroupNames);
            
            if (!this.nextActiveGroupNames.equals(this.activeGroupNames)) {
                this.startedGroupNames.put(face, this.nextActiveGroupNames);
            }
            
            this.activeGroupNames = this.nextActiveGroupNames;
            this.nextActiveGroupNames = null;
        }
        
        if (this.nextActiveMaterialGroupName != null) {
            this.activeMaterialGroup = this.getMaterialGroupInternal(this.nextActiveMaterialGroupName);
            
            if (!this.nextActiveMaterialGroupName.equals(this.activeMaterialGroupName)) {
                this.startedMaterialGroupNames.put(face, this.nextActiveMaterialGroupName);
            }
            
            this.activeMaterialGroupName = this.nextActiveMaterialGroupName;
            this.nextActiveMaterialGroupName = null;
        }
        
        this.faces.add(face);
        
        for (LightObjGroup group : this.activeGroups) {
            group.addFace(face);
        }
        
        if (this.activeMaterialGroup != null) {
            this.activeMaterialGroup.addFace(face);
            
            if (face.getNumVertices() > 3 && this.isFinal) return; // only work with triangles
            
            Triangle t = new Triangle(this.getVertex(face.getVertexIndex(0)), this.getVertex(face.getVertexIndex(1)), this.getVertex(face.getVertexIndex(2)));
            
            if (face.containsTexCoordIndices())
                t.addTexCoords(this.getTexCoord(face.getTexCoordIndex(0)), this.getTexCoord(face.getTexCoordIndex(1)), this.getTexCoord(face.getTexCoordIndex(2)));
            
            this.activeMaterialGroup.triangles.add(t); // add final triangle product of this face to work with
        }
    }
    

    @Override
    public void addFace(int... v) {
        this.addFace(v, null, null);
    }

    @Override
    public void addFaceWithTexCoords(int... v) {
        this.addFace(v, v, null);
    }

    @Override
    public void addFaceWithNormals(int... v) {
        this.addFace(v, null, v);
    }

    @Override
    public void addFaceWithAll(int... v) {
        this.addFace(v, v, v);
    }
    
    @Override
    public void addFace(int[] v, int[] vt, int[] vn) {
        Objects.requireNonNull(v, "The vertex indices are null");
        checkIndices(v, this.getNumVertices(), "Vertex");
        checkIndices(vt, this.getNumTexCoords(), "TexCoord");
        checkIndices(vn, this.getNumNormals(), "Normal");
        LightObjFace face = new LightObjFace(v, vt, vn);
        this.addFace(face);
    }
    
    @Override
    public void setMtlFileNames(Collection<? extends String> mtlFileNames) {
        this.mtlFileNames = Collections.unmodifiableList(new ArrayList<String>(mtlFileNames));
    }

    @Override
    public String toString() {
        return "Obj[" +
            "#vertices=" + this.vertices.size() + "," +
            "#texCoords=" + this.texCoords.size() + "," +
            "#normals=" + this.normals.size() + "," +
            "#faces=" + this.faces.size() + "," +
            "#groups=" + this.groups.size() + "," +
            "#materialGroups=" + this.materialGroups.size() + "," +
            "mtlFileNames=" + this.mtlFileNames + "]";
    }

    /**
     * Returns a set containing all groups with the given names. If the
     * groups with the given names do not exist, they are created and
     * added to this Obj.
     * 
     * @param groupNames The group names
     * @return The groups
     */
    private List<LightObjGroup> getGroupsInternal(Collection<? extends String> groupNames) {
        List<LightObjGroup> groups = new ArrayList<LightObjGroup>(groupNames.size());
        
        for (String groupName : groupNames) {
            LightObjGroup group = getGroupInternal(groupName);
            groups.add(group);
        }
        
        return groups;
    }
    
    /**
     * Returns the group with the given names. If the group with the given 
     * name does not exist, it is created and added to this Obj.
     * 
     * @param groupName The group name
     * @return The group
     */
    private LightObjGroup getGroupInternal(String groupName) {
        LightObjGroup group = this.groupMap.get(groupName);
        
        if (group == null) {
            group = new LightObjGroup(groupName);
            this.groupMap.put(groupName, group);
            this.groups.add(group);
        }
        
        return group;
    }

    /**
     * Returns the material group with the given names. If the material group 
     * with the given name does not exist, it is created and added to this Obj.
     * 
     * @param materialGroupName The material group name
     * @return The material group
     */
    private LightObjGroup getMaterialGroupInternal(String materialGroupName) {
        LightObjGroup group = this.materialGroupMap.get(materialGroupName);
        
        if (group == null) {
            group = new LightObjGroup(materialGroupName);
            this.materialGroupMap.put(materialGroupName, group);
            this.materialGroups.add(group);
        }
        
        return group;
    }

    /**
     * If the given indices are <code>null</code>, then this method will
     * do nothing. Otherwise, it will check whether the given indices 
     * are valid, and throw an IllegalArgumentException if not. They
     * are valid when they are all not negative, and all smaller than 
     * the given maximum.
     * 
     * @param indices The indices
     * @param max The maximum index, exclusive
     * @param name The name of the index set
     * @throws IllegalArgumentException If the given indices are not valid
     */
    private static void checkIndices(int indices[], int max, String name) {
        if (indices == null) return;
        
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] < 0) {
                throw new IllegalArgumentException(name + " index is negative: " + indices[i]);
            }
            
            if (indices[i] >= max) {
                throw new IllegalArgumentException(name + " index is " + indices[i] + ", but must be smaller than " + max);
            }
        }
    }
}
