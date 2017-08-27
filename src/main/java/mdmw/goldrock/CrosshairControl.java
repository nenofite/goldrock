package mdmw.goldrock;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.texture.Texture;
import com.jme3.texture.image.ImageRaster;
import com.jme3.ui.Picture;

/**
 * Make the crosshair follow the mouse cursor. On click, send a shoot event.
 */
public class CrosshairControl extends AbstractControl implements ActionListener
{
    private static final float WIDTH = 50;
    private static final float HEIGHT = 50;
    private static final float SPEED = 4500;
    private Main app;

    private CrosshairControl(Main app)
    {
        this.app = app;
    }

    /**
     * Make a crosshair sprite and attach a new control to it.
     *
     * @return A new crosshair with a controller on it
     */
    public static Spatial makeCrosshair(Main app)
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
        ShootDeerState shootDeerState = app.getStateManager().getState(ShootDeerState.class);
        if (shootDeerState == null)
        {
            return;
        }

        if (ShootDeerState.SHOOT_MAPPING.equals(name) && isPressed)
        {
            if (shootDeerState.canShoot())
            {
                shootDeerState.fireBullet();

                // Shoot the deer under the cursor
                DeerControl deer = pickDeer();
                if (deer != null)
                {
                    deer.shoot();
                }
            }
        }
    }

    /**
     * Get the deer under the crosshair
     *
     * @return The deer under the crosshair, or null if there is none
     */
    private DeerControl pickDeer()
    {
        ShootDeerState shootDeerState = app.getStateManager().getState(ShootDeerState.class);
        if (shootDeerState == null)
        {
            return null;
        }

        CollisionResults collisionResults = new CollisionResults();

        Vector3f click3d = getSpatial().getWorldTranslation();
        Vector3f dir = new Vector3f(0, 0, -1);

        Texture t = app.getAssetManager().loadTexture("Sprites/ForegroundLayer.png");
        int y = (int) (click3d.getY() * t.getImage().getHeight() / app.getCamera().getHeight());
        int x = (int) (click3d.getX() * t.getImage().getWidth() / app.getCamera().getWidth());
        ImageRaster ir = ImageRaster.create(t.getImage());
        ColorRGBA p;
        try
        {
            p = ir.getPixel(x, y);
        }
        catch (IllegalArgumentException e)
        {
            // The x and y were outside the image; this means nothing was hit
            p = new ColorRGBA(0, 0, 0, 0);
        }

        if (p.a < 0.01f)
        {
            // Aim the ray from the clicked spot forwards.
            Ray ray = new Ray(click3d, dir);

            shootDeerState.getNode().collideWith(ray, collisionResults);

            for (CollisionResult result : collisionResults)
            {
                DeerControl deer = Utils.extractControl(result.getGeometry(), DeerControl.class);
                if (deer != null)
                {
                    return deer;
                }
            }
        }

        return null;
    }
}
