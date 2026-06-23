package dev.hermes.api.ecs;

/**
 * Single drawable piece on an entity: mesh, sprite, or procedural primitive.
 */
public final class DrawablePart {

    private final DrawableKind kind;
    private final String id;
    private String model;
    private String texture;
    private String primitive;
    private float[] size;
    private DrawableRig rig;
    private final LocalTransform local = new LocalTransform();
    private SpriteSheet sheet;
    private PartMaterial partMaterial;

    private DrawablePart(DrawableKind kind, String id) {
        this.kind = kind;
        this.id = id;
    }

    public static DrawablePart mesh(String id, String model) {
        DrawablePart part = new DrawablePart(DrawableKind.MESH, id);
        part.model = model;
        return part;
    }

    public static DrawablePart sprite(String id, String texture) {
        DrawablePart part = new DrawablePart(DrawableKind.SPRITE, id);
        part.texture = texture;
        return part;
    }

    public DrawableKind kind() {
        return kind;
    }

    public String id() {
        return id;
    }

    public String model() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String texture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public String primitive() {
        return primitive;
    }

    public void setPrimitive(String primitive) {
        this.primitive = primitive;
    }

    public float[] size() {
        return size == null ? null : size.clone();
    }

    public void setSize(float[] size) {
        this.size = size == null ? null : size.clone();
    }

    public DrawableRig rig() {
        return rig;
    }

    public void setRig(DrawableRig rig) {
        this.rig = rig;
    }

    public LocalTransform local() {
        return local;
    }

    public SpriteSheet sheet() {
        return sheet;
    }

    public void setSheet(SpriteSheet sheet) {
        this.sheet = sheet;
    }

    public PartMaterial partMaterial() {
        return partMaterial;
    }

    public void setPartMaterial(PartMaterial partMaterial) {
        this.partMaterial = partMaterial;
    }
}
