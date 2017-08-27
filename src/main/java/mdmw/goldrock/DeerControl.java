package mdmw.goldrock;

import com.jme3.material.RenderState;
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
    private static final float DEER_MOVEMENT_RUN_MODIFIER = 7f;
    private static final float DEER_MOVEMENT_JUMP_MODIFIER = 14f;
    private static final int DEER_DYING_SPEED = 60;
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
    private float remainingDeathDistance = HEIGHT + 15;
    private float widthScale = 1f;

    private DeerControl(Main app, Picture imgHandle, boolean facingLeft)
    {
        this.imgHandle = imgHandle;
        this.app = app;
        deerSpeed = (float) (Math.random() * (DEER_MOVEMENT_MAX - DEER_MOVEMENT_MIN)) + DEER_MOVEMENT_MIN;
        state = DeerState.RUNNING;
        flipped = facingLeft;
        currentAnimation = createRunningAnimation();
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
        commanderNode.setLocalTranslation(0, 0, ShootDeerState.Z_DEER);

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

    private DeerState transitionState(DeerState start, float x, float y, float deerWidth)
    {
        if (start == DeerState.DYING)
        {
            // Frankenstein is not present
            return DeerState.DYING;
        }

        double randomness = Math.random();
        switch (start)
        {
            case JUMPING:
                return DeerState.WALKING;
            case WALKING:
                if (randomness < 0.1)
                {
                    return DeerState.EATING;
                } else if (randomness < 0.3)
                {
                    return DeerState.RUNNING;
                }
                break;
            case RUNNING:
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
        }
        return start;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        DeerState nextState = state;
        if (shouldJump())
        {
            nextState = DeerState.JUMPING;
        } else
        {
            accrue += tpf;
            if (state.equals(DeerState.JUMPING) || accrue >= ACCRUE_THRESHOLD)
            {
                nextState = transitionState(state, getSpatial().getLocalTranslation().getX(),
                        getSpatial().getLocalTranslation().getY(), getDeerWidth());
                accrue = 0;
            }
        }

        if (!state.equals(nextState))
        {
            switch (nextState)
            {
                case RUNNING:
                    currentAnimation = createRunningAnimation();
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
                case JUMPING:
                    currentAnimation = createJumpingAnimation();
                    break;
            }
            state = nextState;
        }

        currentAnimation.progress(tpf);
        float directionModifier = (flipped) ? -1 : 1;
        switch (state)
        {
            case DYING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                float speed = Math.min(DEER_DYING_SPEED * tpf, remainingDeathDistance);
                remainingDeathDistance -= speed;
                getSpatial().move(new Vector3f(0, -speed, 0));
                break;
            case WALKING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                getSpatial().move(new Vector3f(directionModifier * deerSpeed * tpf, 0, 0));
                break;
            case RUNNING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                getSpatial().move(new Vector3f(directionModifier * deerSpeed * tpf * DEER_MOVEMENT_RUN_MODIFIER, 0, 0));
                break;
            case EATING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                break;
            case JUMPING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                getSpatial()
                        .move(new Vector3f(directionModifier * deerSpeed * tpf * DEER_MOVEMENT_JUMP_MODIFIER, 0, 0));
                break;
        }
    }

    private AnimationStation createJumpingAnimation()
    {
        AnimationStation stat = new AnimationStation();
        stat.addImage("Sprites/running_deer_1.png", 1f);
        return stat;
    }

    private AnimationStation createRunningAnimation()
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

            ShootDeerState shootDeerState = app.getStateManager().getState(ShootDeerState.class);
            if (shootDeerState != null)
            {
                shootDeerState.onKillDeer();
            }

            currentAnimation = createDyingAnimation();
        }
    }


    private float getDeerWidth()
    {
        return WIDTH * widthScale;
    }


    /**
     * Check whether the deer is currently in a position where it must be jumping (eg. over a creek)
     *
     * @return True if the deer should be jumping
     */
    private boolean shouldJump()
    {
        float fX = getSpatial().getLocalTranslation().getX() / app.getCamera().getWidth();
        float fY = getSpatial().getLocalTranslation().getY() / app.getCamera().getHeight();
        float fW = getDeerWidth() / app.getCamera().getHeight();
        if (0.05 < fY && fY < 0.2)
        {
            // bottom lane, right to left
            if (0.4 < fX - fW && fX - fW < 0.7)
            {
                return true;
            }
        } else if (0.3 < fY && fY < 0.5)
        {
            // 2nd from bottom, left to right
            if (0.3 < fX + fW && fX + fW < 0.57)
            {
                return true;
            }
        } else if (0.55 < fY && fY < 0.65)
        {
            // 3rd from bottom, right to left
            if (0.6 < fX && fX < 0.7)
            {
                return true;
            }
        }

        return false;
    }

    enum DeerState
    {
        WALKING, EATING, RUNNING, DYING, JUMPING
    }
}
