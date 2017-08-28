package mdmw.goldrock;

import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

public class WolfEatsYouControl extends AbstractControl
{
    private static final float WIDTH_PORTION = 0.9f;
    private static final float HEIGHT_PORTION = 0.9f;
    private static final long ENTRY_DURATION = 500;
    private long startedEntry;
    private Main app;
    private AudioNode biteSound;

    public static Spatial createWolfEatingYou(Main app)
    {
        Node base = new Node("you have met");
        Picture picture = new Picture("a terrible fate");
        // haven't you

        picture.setImage(app.getAssetManager(), "Sprites/git_munched.png", true);
        float width = app.getCamera().getWidth() * WIDTH_PORTION;
        float height = app.getCamera().getHeight() * HEIGHT_PORTION;
        picture.setWidth(width);
        picture.setHeight(height);
        picture.setLocalTranslation(-width / 2, -height / 2, 0);

        base.attachChild(picture);
        base.addControl(new WolfEatsYouControl(app));
        base.setLocalTranslation(app.getCamera().getWidth() / 2, app.getCamera().getHeight() / 2, 5);

        return base;
    }

    public WolfEatsYouControl(Main app)
    {
        this.app = app;

        biteSound = new AudioNode(app.getAssetManager(), "Audio/mdmw_bite.wav", AudioData.DataType.Buffer);
        biteSound.setPositional(false);
        biteSound.setVolume(16);
    }

    @Override
    public void setSpatial(Spatial spatial)
    {
        super.setSpatial(spatial);
        if (spatial != null)
        {
            biteSound.playInstance();
            startedEntry = System.currentTimeMillis();
        } else
        {
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
