package net.timardo.lt3dimporter.converter;

public class ColoredTexture implements Texture {
    
    public int color;
    
    public ColoredTexture(int colorIn) {
        this.color = colorIn;
    }
    
    @Override
    public int colorTile(double u, double v) {
        return this.color;
    }
    
}
