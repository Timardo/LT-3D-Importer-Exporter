package net.timardo.lt3dimporter.converter;

public class ColoredTexture implements Texture {
    
    public int color;
    
    public ColoredTexture(int colorIn) {
        this.color = colorIn;
    }
    
    @Override
    public int colorTile(double[] uv) {
        return this.color;
    }
}
