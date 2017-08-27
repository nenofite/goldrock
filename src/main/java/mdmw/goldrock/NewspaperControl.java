package mdmw.goldrock;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

/**
 * Spins and grows a newspaper
 */
public class NewspaperControl extends AbstractControl
{
    private static final float WIDTH = 700;
    private static final float HEIGHT = 700;
    private static final long ENTRY_DURATION = 500;
    private Main app;
    private long startedEntry;

    private NewspaperControl(Main app)
    {
        this.app = app;
    }

    public static Spatial makeNewspaper(String path, Main app)
    {
        Picture picture = new Picture("Newspaper");
        picture.setImage(app.getAssetManager(), path, true);
        picture.setWidth(WIDTH);
        picture.setHeight(HEIGHT);
        picture.setLocalTranslation(-WIDTH / 2, -HEIGHT / 2, 0);

        Node node = new Node("Newspaper Parent");
        node.attachChild(picture);
        node.addControl(new NewspaperControl(app));
        node.setLocalTranslation(app.getCamera().getWidth() / 2, app.getCamera().getHeight() / 2, 0);
        return node;
    }

    @Override
    public void setSpatial(Spatial spatial)
    {
        super.setSpatial(spatial);
        if (spatial != null)
        {
            startedEntry = System.currentTimeMillis();
        }
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        float frac = (float) (System.currentTimeMillis() - startedEntry) / ENTRY_DURATION;

        if (frac >= 1)
        {
            // Reset rotation
            getSpatial().setLocalRotation(Quaternion.IDENTITY);
            // Set to full scale
            getSpatial().setLocalScale(1);
        } else
        {
            // Spin
            float angle = FastMath.interpolateLinear(frac, 0, 2 * FastMath.PI);
            getSpatial().setLocalRotation(new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Z));

            // Grow
            float scale = FastMath.interpolateLinear(frac, 0, 1);
            getSpatial().setLocalScale(scale);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }
}
