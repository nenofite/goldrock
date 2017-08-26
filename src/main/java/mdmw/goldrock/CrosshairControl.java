package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

/**
 * Make the crosshair follow the mouse cursor. On click, send a shoot event.
 */
public class CrosshairControl extends AbstractControl implements ActionListener
{
    private static final float WIDTH = 50;
    private static final float HEIGHT = 50;

    private static final float SPEED = 1500;

    private Application app;

    /**
     * Make a crosshair sprite and attach a new control to it.
     *
     * @return A new crosshair with a controller on it
     */
    public static Spatial makeCrosshair(Application app)
    {

        Picture crosshair = new Picture("Arrow");
        crosshair.setImage(app.getAssetManager(), "Sprites/crosshairs.png", true);
        crosshair.setWidth(WIDTH);
        crosshair.setHeight(HEIGHT);
        crosshair.setLocalTranslation(-WIDTH / 2, -HEIGHT / 2, 0);

        Node result = new Node();
        result.attachChild(crosshair);
        result.addControl(new CrosshairControl(app));
        return result;
    }

    private CrosshairControl(Application app)
    {
        this.app = app;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        // Get mouse location
        Vector2f cursor = app.getInputManager().getCursorPosition();
        Vector3f target = new Vector3f(cursor.getX(), cursor.getY(), 0f);

        // Move crosshair towards mouse
        Vector3f delta = target.subtract(getSpatial().getLocalTranslation());
        if (delta.length() <= SPEED * tpf)
        {
            getSpatial().setLocalTranslation(target);
        } else
        {
            getSpatial().move(delta.normalize().mult(SPEED * tpf));
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }

    @Override
    public void setSpatial(Spatial spatial)
    {
        super.setSpatial(spatial);
        if (spatial != null)
        {
            // Listen to the shoot event
            app.getInputManager().addListener(this, ShootDeerState.SHOOT_MAPPING);

        } else
        {
            app.getInputManager().removeListener(this);
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        if (ShootDeerState.SHOOT_MAPPING.equals(name) && isPressed)
        {
            // Shoot the deer under the cursor
            // TODO
            System.out.println("POW!");
        }
    }
}
