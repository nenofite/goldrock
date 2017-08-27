package mdmw.goldrock;

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
    public static final float BASE_SPEED = 300;
    boolean goingLeft;
    private float widthScale = 0.6f;
    private Main app;
    private Picture imgHandle;
    private AnimationStation animation;
    private int lap;

    private MdmwControl(Main app, Picture imgHandle, boolean startLeft)
    {
        this.app = app;
        this.imgHandle = imgHandle;
        this.goingLeft = startLeft;

        animation = new AnimationStation();
        animation.addImage("Sprites/mdmw.png", 1.0f);
    }

    public static Spatial createMdmw(Main app, boolean startLeft)
    {
        Node node = new Node("nothing can save you now");
        Picture mdmw = new Picture("your worst nightmare");

        node.addControl(new MdmwControl(app, mdmw, startLeft));
        node.setLocalTranslation(0, 0, ShootDeerState.Z_DEER);

        mdmw.setImage(app.getAssetManager(), "Sprites/mdmw.png", true);
        mdmw.getMaterial().getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        mdmw.setWidth(WIDTH);
        mdmw.setHeight(HEIGHT);

        float mod = (startLeft) ? -1f : 1f;
        mdmw.scale(mod * 0.6f, 1, 1);

        node.setLocalTranslation(app.getCamera().getWidth(), app.getCamera().getHeight() * getVerticalFractionForLap(0),
                0);

        return node;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        float x = getSpatial().getLocalTranslation().getX();
        boolean offLeft = x < 0 && goingLeft;
        boolean offRight = x > app.getCamera().getWidth() && !goingLeft;
        if (offLeft || offRight)
        {
            System.out.println("MDMW completed a lap");
            // TODO howl and shake and stuff
            imgHandle.removeFromParent();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
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
}
