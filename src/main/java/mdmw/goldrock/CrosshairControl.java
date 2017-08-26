package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

/**
 * Make the crosshair follow the mouse cursor. On click, send a shoot event.
 */
public class CrosshairControl extends AbstractControl {

    /**
     * Make a crosshair sprite and attach a new control to it.
     *
     * @return A new crosshair with a controller on it
     */
    public static Spatial makeCrosshair(Application app) {

        Picture crosshair = new Picture("Arrow");
        crosshair.setImage(app.getAssetManager(), "Sprites/crosshairs.png", true);
        crosshair.setWidth(50);
        crosshair.setHeight(50);

        Node result = new Node();
        result.attachChild(crosshair);
        result.addControl(new CrosshairControl());
        return result;
    }

    @Override
    protected void controlUpdate(float tpf) {

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }
}
