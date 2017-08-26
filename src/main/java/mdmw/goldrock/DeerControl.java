package mdmw.goldrock;

import com.jme3.material.RenderState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;
import javafx.scene.transform.Transform;

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
    private static final float DEER_MOVEMENT_JUMP_MODIFIER = 5f;
    private static final int DEER_DYING_SPEED = 5;
    private static final String IMG_WALKING = "Sprites/deer.png";
    private static final String IMG_EATING = "Sprites/eating_deer.png";
    private static final String IMG_DEAD = "Sprites/dead_deer.png";
    private Main app;
    private Picture imgHandle;
    private float accrue = 0.0f;
    private float deerSpeed = 0.0f;
    private DeerState state;
    private AnimationStation currentAnimation;
    private boolean flipped;

    private DeerControl(Main app, Picture imgHandle, boolean facingLeft)
    {
        this.imgHandle = imgHandle;
        this.app = app;
        deerSpeed = (float) (Math.random() * (DEER_MOVEMENT_MAX - DEER_MOVEMENT_MIN)) + DEER_MOVEMENT_MIN;
        state = DeerState.WALKING;
        flipped = facingLeft;
        currentAnimation = createWalkingAnimation();
    }

    /**
     * Creates a Deer Geometry, sets it up with some sweet images, attaches an instance of this controller, and wins
     * the game.
     *
     * @return A deer.
     */
    public static Node createDeer(Main app, boolean facingLeft)
    {
        Node commanderNode = new Node("Deer Commander");
        Picture deer = new Picture("Regular Deer");
        commanderNode.addControl(new DeerControl(app, deer, facingLeft));
        commanderNode.setLocalTranslation(0, 0, ShootDeerState.Z_FOREGROUND);

        deer.setImage(app.getAssetManager(), IMG_WALKING, true);
        deer.getMaterial().getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        deer.setWidth(WIDTH);
        deer.setHeight(HEIGHT);

        if (facingLeft)
        {
            deer.scale(-1, 1, 1);
        }

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
            DeerState nextState = transitionState(state);
            if (state != nextState)
            {
                switch (nextState)
                {
                    case JUMPING:
                        currentAnimation = createJumpingAnimation();
                        break;
                    case WALKING:
                        currentAnimation = createWalkingAnimation();
                        break;
                    case DYING:
                        currentAnimation = createDyingAnimation();
                        break;
                    case EATING:
                        currentAnimation = createEatingAnimation();
                        break;
                }
            }
            state = nextState;
            accrue = 0;
        }

        currentAnimation.progress(tpf);
        float directionModifier = (flipped) ? -1 : 1;
        switch (state)
        {
            case DYING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                getSpatial().move(new Vector3f(0, -DEER_DYING_SPEED * tpf, 0));
                break;
            case WALKING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                getSpatial().move(new Vector3f(directionModifier * deerSpeed * tpf, 0, 0));
                break;
            case JUMPING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                getSpatial()
                        .move(new Vector3f(directionModifier * deerSpeed * tpf * DEER_MOVEMENT_JUMP_MODIFIER, 0, 0));
                break;
            case EATING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                break;
        }
    }

    private AnimationStation createJumpingAnimation()
    {
        AnimationStation stat = new AnimationStation();
        stat.addImage("Sprites/running_deer_0.png", 0.2f);
        stat.addImage("Sprites/running_deer_1.png", 0.4f);
        return stat;
    }

    private AnimationStation createWalkingAnimation()
    {
        AnimationStation stat = new AnimationStation();
        stat.addImage(IMG_WALKING, 1.0f);
        return stat;
    }

    private AnimationStation createDyingAnimation()
    {
        AnimationStation stat = new AnimationStation();
        stat.addImage(IMG_DEAD, 1.0f);
        return stat;
    }

    private AnimationStation createEatingAnimation()
    {
        AnimationStation stat = new AnimationStation();
        stat.addImage(IMG_EATING, 1.0f);
        return stat;
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
        if (!state.equals(DeerState.DYING))
        {
            state = DeerState.DYING;
            app.getStateManager().getState(ShootDeerState.class).onKillDeer();
            currentAnimation = createDyingAnimation();
        }
    }

    enum DeerState
    {
        WALKING, EATING, JUMPING, DYING
    }
}
