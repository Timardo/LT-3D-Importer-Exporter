package net.timardo.lt3dimporter.converter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class NormalTexture implements Texture {
    private BufferedImage texture;
    
    public NormalTexture(String texFile) throws IOException {
        this.texture = ImageIO.read(new File(texFile));
    }

    @Override
    public int colorTile(double[] uv) {
        if (uv == null) return -1; // can happen in cases when some faces don't have normals
        
        double nU = uv[0] % 1.0F;
        double nV = uv[1] % 1.0F;
        
        if (nU < 0.0F) nU += 1.0F;
        if (nV < 0.0F) nV += 1.0F;
        
        int x = (int)(nU * (this.texture.getWidth() - 1));
        int y = (int)((1.0D - nV) * (this.texture.getHeight() - 1));
        return this.texture.getRGB(x, y);
    }
}
