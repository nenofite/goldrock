package mdmw.goldrock;

import com.jme3.asset.AssetManager;
import com.jme3.material.RenderState;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

public class MdmwControl extends AbstractControl
{
    public static final int WIDTH = 100;
    public static final int HEIGHT = 75;
    public static final float BASE_SPEED = 600;
    private static final float BASE_REGEN_DELAY = 3;
    private Main app;
    private Picture imgHandle;
    private AnimationStation animation;
    private int lap;
    private float remainingRegenDelay;

    private MdmwControl(Main app, Picture imgHandle)
    {
        this.app = app;
        this.imgHandle = imgHandle;

        animation = new AnimationStation();
        animation.addImage("Sprites/mdmw.png", 1.0f);
    }

    public static Spatial createMdmw(Main app)
    {
        Node node = new Node("nothing can save you now");
        Picture mdmw = createPicture(app.getAssetManager());

        node.addControl(new MdmwControl(app, mdmw));
        node.setLocalTranslation(0, 0, ShootDeerState.Z_DEER);

        float mod = (getFacingLeftForLap(0)) ? -1f : 1f;
        float scale = getScaleForLap(1);

        mdmw.scale(mod * scale, 1, 1);
        node.attachChild(mdmw);

        node.setLocalTranslation(app.getCamera().getWidth(), app.getCamera().getHeight() * getVerticalFractionForLap(0),
                0);

        return node;
    }

    private static Picture createPicture(AssetManager assetManager)
    {
        Picture mdmw = new Picture("your worst nightmare");
        mdmw.setImage(assetManager, "Sprites/mdmw.png", true);
        mdmw.getMaterial().getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        mdmw.setWidth(WIDTH);
        mdmw.setHeight(HEIGHT);
        return mdmw;
    }

    private static boolean getFacingLeftForLap(int lap)
    {
        return lap % 2 == 0;
    }

    private static float getScaleForLap(int lap)
    {
        switch (lap)
        {
            case 0:
                return 0.6f;
            case 1:
                return 0.75f;
            case 2:
                return 1f;
            case 3:
                return 1.25f;
            default:
                return 1f;
        }
    }

    private static float getVerticalFractionForLap(int lap)
    {
        switch (lap)
        {
            case 0:
                return 0.85f;
            case 1:
                return 0.6f;
            case 2:
                return 0.4f;
            case 3:
                return 0.1f;
            default:
                return 0.5f;
        }
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        float x = getSpatial().getLocalTranslation().getX();
        boolean offLeft = x < 0 && getFacingLeftForLap(lap);
        boolean offRight = x > app.getCamera().getWidth() && !getFacingLeftForLap(lap);
        if (offLeft || offRight && remainingRegenDelay < 0.0001f)
        {
            System.out.println("MDMW completed a lap");
            // TODO howl and shake and stuff
            imgHandle.removeFromParent();
            remainingRegenDelay = BASE_REGEN_DELAY - tpf;

            getSpatial().setLocalTranslation(app.getCamera().getWidth() / 2, app.getCamera().getHeight() / 2, -2);
            imgHandle = null;
        }

        if (remainingRegenDelay > 0.00001f)
        {
            remainingRegenDelay -= tpf;
        } else
        {
            animation.progress(tpf);

            if (imgHandle == null)
            {
                lap += 1;
                imgHandle = createPicture(app.getAssetManager());
                ((Node) getSpatial()).attachChild(imgHandle);
                imgHandle.setImage(app.getAssetManager(), animation.getCurrent(), true);

                boolean facingLeft = getFacingLeftForLap(lap);
                float scale = getScaleForLap(lap);
                float posneg = (facingLeft) ? -1 : 1;

                imgHandle.scale(posneg * scale, 1, 1);

                float xLoc = (facingLeft) ? app.getCamera().getWidth() : 0;
                float yLoc = getVerticalFractionForLap(lap) * app.getCamera().getHeight();

                getSpatial().setLocalTranslation(xLoc, yLoc, ShootDeerState.Z_DEER);
            }

            float posneg = (getFacingLeftForLap(lap)) ? -1 : 1;
            getSpatial().move(posneg * BASE_SPEED * getScaleForLap(lap) * tpf, 0, 0);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }
}
