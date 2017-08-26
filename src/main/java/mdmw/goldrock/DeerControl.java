package mdmw.goldrock;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

/**
 * Defines logic for controlling deer as they frolick about the forest. If you are kind to them, they will ignore you.
 */
public class DeerControl extends AbstractControl
{
    public static final int WIDTH = 100;
    public static final int HEIGHT = 75;
    private static final int ACCRUE_THRESHOLD = 1;
    private static final int DEER_MOVEMENT_MAX = 30;
    private static final int DEER_MOVEMENT_MIN = 20;
    private static final float DEER_MOVEMENT_JUMP_MODIFIER = 1.5f;
    private static final int DEER_DYING_SPEED = 5;
    private static final String IMG_WALKING = "Sprites/deer.png";
    private static final String IMG_JUMPING = "Sprites/hopping_deer.png";
    private static final String IMG_EATING = "Sprites/eating_deer.png";
    private static final String IMG_DEAD = "Sprites/dead_deer.png";
    private Picture imgHandle;
    private AssetManager assetManager;
    private float accrue = 0.0f;
    private float deerSpeed = 0.0f;
    private DeerState state;

    private DeerControl(AssetManager assets, Picture imgHandle)
    {
        this.imgHandle = imgHandle;
        this.assetManager = assets;
        deerSpeed = (float) (Math.random() * (DEER_MOVEMENT_MAX - DEER_MOVEMENT_MIN)) + DEER_MOVEMENT_MIN;
        state = DeerState.WALKING;
    }

    /**
     * Creates a Deer Geometry, sets it up with some sweet images, attaches an instance of this controller, and wins
     * the game.
     *
     * @return A deer.
     */
    public static Node createDeer(AssetManager assetManager)
    {
        Picture deer = new Picture("Deer");
        deer.setImage(assetManager, "Sprites/deer.png", true);
        deer.setWidth(WIDTH);
        deer.setHeight(HEIGHT);

        Node commanderNode = new Node("Deer Commander");
        commanderNode.addControl(new DeerControl(assetManager, deer));
        commanderNode.setLocalTranslation(0, 0, ShootDeerState.Z_FOREGROUND);
        commanderNode.attachChild(deer);
        return commanderNode;
    }

    private static DeerState transitionState(DeerState start)
    {
        double randomness = Math.random();
        switch (start)
        {
            case WALKING:
                if (randomness < 0.1)
                {
                    return DeerState.EATING;
                } else if (randomness < 0.3)
                {
                    return DeerState.JUMPING;
                }
                break;
            case JUMPING:
                if (randomness < 0.5)
                {
                    return DeerState.WALKING;
                }
                break;
            case EATING:
                if (randomness < 0.5)
                {
                    return DeerState.WALKING;
                }
                break;
            default:
                return start;
        }
        return start;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        accrue += tpf;
        if (accrue >= ACCRUE_THRESHOLD)
        {
            state = transitionState(state);
            accrue = 0;
        }

        switch (state)
        {
            case DYING:
                imgHandle.setImage(assetManager, "Sprites/dead_deer.png", true);
                getSpatial().move(new Vector3f(0, -DEER_DYING_SPEED * tpf, 0));
                break;
            case WALKING:
                imgHandle.setImage(assetManager, IMG_WALKING, true);
                getSpatial().move(new Vector3f(-deerSpeed * tpf, 0, 0));
                break;
            case JUMPING:
                imgHandle.setImage(assetManager, IMG_JUMPING, true);
                getSpatial().move(new Vector3f(-deerSpeed * tpf * DEER_MOVEMENT_JUMP_MODIFIER, 0, 0));
                break;
            case EATING:
                imgHandle.setImage(assetManager, IMG_EATING, true);
                break;
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }

    /**
     * Shoots the deer. After this is done, the deer will proceed to complete sudoku.
     */
    public void shoot()
    {
        state = DeerState.DYING;
    }

    enum DeerState
    {
        WALKING,
        EATING,
        JUMPING,
        DYING
    }
}
