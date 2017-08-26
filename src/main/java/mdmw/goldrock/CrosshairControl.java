package mdmw.goldrock;

import com.jme3.app.Application;
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
public class CrosshairControl extends AbstractControl
{
    private static final float WIDTH = 50;
    private static final float HEIGHT = 50;

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
        crosshair.setWidth(50);
        crosshair.setHeight(50);

        Node result = new Node();
        result.attachChild(crosshair);
        result.addControl(new CrosshairControl(app));
        return result;
    }

    private CrosshairControl(Application app) {
        this.app = app;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        // Get mouse location
        Vector2f cursor = app.getInputManager().getCursorPosition();
        Vector3f target = new Vector3f(cursor.getX() - WIDTH / 2, cursor.getY() - HEIGHT / 2, 0f);

        System.err.println("Cursor: " + cursor);
        System.err.println("Me: " + getSpatial().getLocalTranslation());

        // Move crosshair towards mouse
//        Vector3f delta = getSpatial().getLocalTranslation().subtract(target).mult(0.05f * tpf);
        Vector3f delta = target.subtract(getSpatial().getLocalTranslation()).mult(2f * tpf);
//        new Vector3f(getSpatial().getLocalTranslation()).interpolateLocal(target, 0.005f * tpf);
//        getSpatial().setLocalTranslation(delta);
        getSpatial().move(delta);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }
}
