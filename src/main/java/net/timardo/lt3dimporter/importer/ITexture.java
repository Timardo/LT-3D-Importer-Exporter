package net.timardo.lt3dimporter.importer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public interface ITexture {
    static final int MIN_TEXTURE_SIZE = 512; // TODO: this number is a placeholder, make it configurable or find the lower bound that still works
    
    int colorTile(double[] uv);
    
    /**
     * Resizes small pictures to prevent issues with number precision on texture coords with a lot of decimal places
     * TODO: refactor the Texture hierarchy
     */
    default BufferedImage resizeTextureIfNeeded(BufferedImage input) {
        if (input == null) return null;
        int height = input.getHeight();
        int width = input.getWidth();
        
        if (width <= MIN_TEXTURE_SIZE ||  height <= MIN_TEXTURE_SIZE) {
            int multiplier = 1;
            
            if (width < height) {
                multiplier = (int)Math.ceil(((float)MIN_TEXTURE_SIZE) / width);
            } else {
                multiplier = (int)Math.ceil(((float)MIN_TEXTURE_SIZE) / height);
            }
            
            BufferedImage resized = new BufferedImage(width * multiplier, height * multiplier, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(input, 0, 0, width * multiplier, height * multiplier, null);
            g.dispose();
            input = resized;
        }
        
        return input;
    }
}
