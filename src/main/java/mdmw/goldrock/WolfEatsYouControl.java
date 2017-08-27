package mdmw.goldrock;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

public class WolfEatsYouControl extends AbstractControl
{
    private static final float WIDTH_PORTION = 0.75f;
    private static final float HEIGHT_PORTION = 0.75f;
    private static final float MOVE_SPEED = 100;
    private float width;
    private float height;
    private Main app;

    public static Spatial createWolfEatingYou(Main app)
    {
        Node base = new Node("you have met");
        Picture p = new Picture("a terrible fate");
        // haven't you

        p.setImage(app.getAssetManager(), "Sprites/git_munched.png", true);
        float width = app.getCamera().getWidth() * WIDTH_PORTION;
        float height = app.getCamera().getHeight() * HEIGHT_PORTION;
        p.setWidth(app.getCamera().getWidth() * WIDTH_PORTION);
        p.setHeight(app.getCamera().getHeight() * HEIGHT_PORTION);

        base.attachChild(p);
        base.addControl(new WolfEatsYouControl(app, width, height));

        base.setLocalTranslation(app.getCamera().getWidth() / 2 - width / 2, -height, 5);

        return base;
    }

    public WolfEatsYouControl(Main app, float width, float height)
    {
        this.app = app;
        this.width = width;
        this.height = height;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        float destY = app.getCamera().getHeight() / 2 - height / 2;
        float currentY = getSpatial().getLocalTranslation().getY();

        float moveY = Math.min(destY - currentY, MOVE_SPEED * tpf);

        getSpatial().move(0, moveY, 0);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }
}
