package mdmw.goldrock;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

public class TitleDeerControl extends AbstractControl
{
    private static final float WIDTH = 150;
    private static final float HEIGHT = 150;
    private Main app;
    private AnimationStation animation;
    private Picture imgHandle;

    private TitleDeerControl(Main app, Picture image)
    {
        this.app = app;
        this.imgHandle = image;
        animation = new AnimationStation();
        animation.addImage("Sprites/start_deer_0.png", 1.6f);
        animation.addImage("Sprites/start_deer_1.png", 0.4f);
        animation.addImage("Sprites/start_deer_2.png", 1.2f);
        animation.addImage("Sprites/start_deer_1.png", 0.4f);
    }

    public static Spatial createTitleDeer(Main app)
    {
        Node base = new Node("TitleDeer Node");
        Picture p = new Picture("TitleDeer Pic");
        TitleDeerControl c = new TitleDeerControl(app, p);
        p.setImage(app.getAssetManager(), c.getAnimation().getCurrent(), true);
        p.setWidth(WIDTH);
        p.setHeight(HEIGHT);

        base.attachChild(p);
        base.addControl(c);

        return base;
    }

    public AnimationStation getAnimation()
    {
        return animation;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        animation.progress(tpf);
        imgHandle.setImage(app.getAssetManager(), animation.getCurrent(), true);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }
}
