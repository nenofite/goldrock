package mdmw.goldrock;

import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * Display how much time the player has left to shoot
 */
public class CountdownControl extends AbstractControl
{
    private Main app;
    private BitmapText text;

    public CountdownControl(BitmapText text, Main app)
    {
        this.text = text;
        this.app = app;
    }

    public static Spatial makeCountdown(Main app)
    {
        BitmapText text = new BitmapText(app.getGuiFont());
        text.setSize(app.getGuiFont().getCharSet().getRenderedSize());
        text.setColor(ColorRGBA.Black);

        Node node = new Node("Countdown");
        node.attachChild(text);
        node.addControl(new CountdownControl(text, app));
        node.setLocalTranslation(0, app.getCamera().getHeight() - text.getHeight() - 10, 10);
        return node;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        // Get the time remaining
        ShootDeerState shootDeerState = app.getStateManager().getState(ShootDeerState.class);
        if (shootDeerState == null)
        {
            return;
        }
        long remaining = shootDeerState.getTimeRemaining();

        // Get number of minutes
        int minutes = (int) (remaining / 1000 / 60);

        // Get number of seconds
        int seconds = (int) (remaining / 1000 % 60);

        // Update the text
        text.setText(String.format("%01d:%02d", minutes, seconds));
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }
}
