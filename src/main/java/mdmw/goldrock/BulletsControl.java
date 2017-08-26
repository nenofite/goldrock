package mdmw.goldrock;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

import java.util.ArrayList;
import java.util.List;

/**
 * Display the number of bullets the player has left
 */
public class BulletsControl extends AbstractControl
{
    private static final float WIDTH = 30;
    private static final float HEIGHT = 60;

    private Main app;
    private Node node;
    private List<Picture> bullets;
    private List<Picture> empties;

    /**
     * Make the bullets indicator, which is a node containing many images
     */
    public static Spatial makeBullets(Main app)
    {
        Node node = new Node("Bullets");
        node.addControl(new BulletsControl(node, app));
        return node;
    }

    private BulletsControl(Node node, Main app)
    {
        this.app = app;
        this.node = node;
        bullets = new ArrayList<>();
        empties = new ArrayList<>();
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        ShootDeerState shootDeerState = app.getStateManager().getState(ShootDeerState.class);
        int numBullets = shootDeerState.getBullets();
        int numEmpties = shootDeerState.getMaxBullets() - shootDeerState.getBullets();

        if (bullets.size() != shootDeerState.getBullets() || empties.size() != numEmpties)
        {
            // If the number of bullets/empties has changed, clear the lists and recreate the graphics
            bullets.forEach(Spatial::removeFromParent);
            bullets.clear();
            empties.forEach(Spatial::removeFromParent);
            empties.clear();

            // Create the bullets (non empties) on the right
            int bulletIndex = 0;
            for (int i = 0; i < numBullets; ++i)
            {
                Picture bullet = makeBulletOrEmpty(false, bulletIndex);
                bullets.add(bullet);
                node.attachChild(bullet);
                ++bulletIndex;
            }

            // Then create the empties to the left of those
            for (int i = 0; i < numEmpties; ++i)
            {
                Picture empty = makeBulletOrEmpty(true, bulletIndex);
                empties.add(empty);
                node.attachChild(empty);
                ++bulletIndex;
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }

    private Picture makeBulletOrEmpty(boolean empty, int bulletIndex)
    {
        Picture picture = new Picture("Bullet");
        String path = empty ? "Sprites/empty_bullet.png" : "Sprites/bullet.png";
        picture.setImage(app.getAssetManager(), path, true);
        picture.setWidth(WIDTH);
        picture.setHeight(HEIGHT);
        picture.setLocalTranslation(app.getCamera().getWidth() - (bulletIndex + 1) * WIDTH, 0, 10);
        return picture;
    }
}
