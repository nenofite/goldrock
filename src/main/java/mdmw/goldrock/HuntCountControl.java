package mdmw.goldrock;

import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * Display the current hunt number out of the total number of hunts (eg. "Round 1 of 3")
 */
public class HuntCountControl extends AbstractControl
{
    private Main app;
    private BitmapText text;

    public HuntCountControl(BitmapText text, Main app)
    {
        this.text = text;
        this.app = app;
    }

    public static Spatial makeHuntCount(Main app)
    {
        BitmapText text = new BitmapText(app.getGuiFont());
        text.setSize(app.getGuiFont().getCharSet().getRenderedSize());
        text.setColor(ColorRGBA.White);

        Node node = new Node("Hunt Count");
        node.attachChild(text);
        node.addControl(new HuntCountControl(text, app));
        node.setLocalTranslation(app.getCamera().getWidth() * 3 / 4,
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
        int roundNumber = shootDeerState.getRoundNumber();
        int numRounds = ShootDeerState.NUM_ROUNDS;
        if (roundNumber <= numRounds)
        {
            v = String.format("Round %d of %d", roundNumber, numRounds);
        } else
        {
            v = "Round ?!?!?!";
        }
        text.setText(v);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }
}
