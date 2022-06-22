package net.timardo.lt3dimporter.importer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class NormalTexture implements ITexture {
    private BufferedImage texture;
    
    public NormalTexture(String texFile) throws IOException {
        this.texture = ImageIO.read(new File(texFile));
    }

    @Override
    public int colorTile(double[] uv) {
        if (uv == null) return -1; // can happen in cases when some faces don't have texture coords
        
        double nU = uv[0] % 1.0D;
        double nV = uv[1] % 1.0D;
        
        if (nU < 0.0F) nU += 1.0D;
        if (nV < 0.0F) nV += 1.0D;
        
        int x = (int)(nU * (this.texture.getWidth() - 1));
        int y = (int)((1.0D - nV) * (this.texture.getHeight() - 1));
        return this.texture.getRGB(x, y);
    }
}
