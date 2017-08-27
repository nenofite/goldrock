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
    private KillCountType type;

    public KillCountControl(BitmapText text, Main app, KillCountType type)
    {
        this.text = text;
        this.app = app;
        this.type = type;
    }

    public static Spatial makeKillCount(Main app, KillCountType type)
    {
        BitmapText text = new BitmapText(app.getGuiFont());
        text.setSize(app.getGuiFont().getCharSet().getRenderedSize());
        text.setColor(ColorRGBA.White);

        Node node = new Node("Kill Count");
        node.attachChild(text);
        node.addControl(new KillCountControl(text, app, type));
        float xLocationMod = (type == KillCountType.TOTAL) ? 2f : 1f;
        node.setLocalTranslation(app.getCamera().getWidth() * xLocationMod / 3f,
                app.getCamera().getHeight() - text.getHeight() + 5, 10);
        return node;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        ShootDeerState shootDeerState = app.getStateManager().getState(ShootDeerState.class);
        if (shootDeerState == null)
        {
            return;
        }
        String v;
        switch (type)
        {
            case HUNT:
                v = "Score: " + shootDeerState.getKillCount();
                break;
            case TOTAL:
                v = "Total Score: " + shootDeerState.getTotalKillCount();
                break;
            default:
                v = "";
                break;
        }
        text.setText(v);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }

    enum KillCountType
    {
        HUNT, TOTAL
    }
}
