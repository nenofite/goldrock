package mdmw.goldrock;


import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * Display the kill count
 */
public class KillCountControl extends AbstractControl
{
    private Main app;
    private BitmapText text;

    public static Spatial makeKillCount(Main app)
    {
        BitmapText text = new BitmapText(app.getGuiFont());
        text.setSize(app.getGuiFont().getCharSet().getRenderedSize());
        text.setColor(ColorRGBA.Black);

        Node node = new Node("Kill Count");
        node.attachChild(text);
        node.addControl(new KillCountControl(text, app));
        node.setLocalTranslation(app.getCamera().getWidth() / 2f, app.getCamera().getHeight() - text.getHeight() - 10,
                10);
        return node;
    }

    public KillCountControl(BitmapText text, Main app)
    {
        this.text = text;
        this.app = app;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        text.setText("" + app.getStateManager().getState(ShootDeerState.class).getKillCount());
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {

    }
}
