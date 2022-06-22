package net.timardo.lt3dimporter.importer;

public class ColoredTexture implements ITexture {
    public int color;
    
    public ColoredTexture(int colorIn) {
        this.color = colorIn;
    }
    
    @Override
    public int colorTile(double[] uv) {
        return this.color;
    }
}
