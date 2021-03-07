package net.timardo.lt3dimporter.converter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;

import de.javagl.obj.Mtl;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MtlTexture implements Texture {
    private float opacity;
    private float[] diffuse;
    private int color;
    private BufferedImage texture;

    public MtlTexture(Mtl mtlFile, String root) throws IOException {
        if (mtlFile.getMapKd() != null)
            this.texture = ImageIO.read(new File(root + mtlFile.getMapKd()));
        else
            this.texture = null;
        
        this.opacity = mtlFile.getD();
        this.diffuse = new float[] { MathHelper.clamp(mtlFile.getKd().getX(), 0, 1), MathHelper.clamp(mtlFile.getKd().getY(), 0, 1), MathHelper.clamp(mtlFile.getKd().getZ(), 0, 1) };
        Color c = ColorUtils.IntToRGBA(ColorUtils.VecToInt(new Vec3d(diffuse[0], diffuse[1], diffuse[2])));
        c.setAlpha(MathHelper.floor(opacity * 255.0D));
        this.color = ColorUtils.RGBAToInt(c);
    }

    @Override
    public int colorTile(double[] uv) {
        if (this.texture == null) {
            return color;
        }
        
        double nU = uv[0] % 1.0F;
        double nV = uv[1] % 1.0F;
        
        if (nU < 0.0F)
            nU += 1.0F;
        
        if (nV < 0.0F)
            nV += 1.0F;
        
        int x = (int)(nU * (this.texture.getWidth() - 1));
        int y = (int)((1.0D - nV) * (this.texture.getHeight() - 1));
        Color c = ColorUtils.IntToRGBA(this.texture.getRGB(x, y));
        c.setRed(MathHelper.floor((c.getRed() * this.diffuse[0])));
        c.setGreen(MathHelper.floor((c.getGreen() * this.diffuse[1])));
        c.setBlue(MathHelper.floor((c.getBlue() * this.diffuse[2])));
        c.setAlpha(MathHelper.floor((c.getAlpha() * this.opacity)));
        return ColorUtils.RGBAToInt(c);
    }
}
