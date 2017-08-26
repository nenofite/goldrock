package mdmw.goldrock;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Quad;

import java.io.IOException;

public class DeerControl extends AbstractControl
{
    /**
     * Creates a Deer Geometry, sets it up with some sweet images, attaches an instance of this controller, and wins
     * the game.
     *
     * @return A deer.
     */
    public static Geometry createDeer()
    {
        Geometry ret = new Geometry();
        ret.setMesh(new Quad());
        ret.addControl(new DeerControl());
        return ret;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        getSpatial().move(new Vector3f(-1 * tpf, 0, 0));
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {

    }
}
