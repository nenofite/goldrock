package mdmw.goldrock;

import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

import java.util.ArrayList;
import java.util.List;

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
    private static final long STEP_DELAY = 400;
    private static final String IMG_WALKING = "Sprites/deer.png";
    private static final String IMG_EATING = "Sprites/eating_deer.png";
    private static final String IMG_DEAD = "Sprites/dead_deer.png";
    private Main app;
    private Picture imgHandle;
    private float accrue = 0.0f;
    private float deerSpeed = 0.0f;
    private DeerState state;
    private AnimationStation currentAnimation;
    private boolean goingLeft;
    private float remainingDeathDistance = HEIGHT + 15;
    private float widthScale = 1f;
    private List<AudioNode> deerStepSounds;
    private AudioNode jumpSound;
    private long madeLastStep;

    private DeerControl(Main app, Picture imgHandle, boolean facingLeft, float scale)
    {
        this.widthScale = scale;
        this.imgHandle = imgHandle;
        this.app = app;
        deerSpeed = (float) (Math.random() * (DEER_MOVEMENT_MAX - DEER_MOVEMENT_MIN)) + DEER_MOVEMENT_MIN;
        state = DeerState.RUNNING;
        goingLeft = facingLeft;
        currentAnimation = createRunningAnimation();

        // Load the audio nodes
        initAudio();
        madeLastStep = System.currentTimeMillis();
    }

    /**
     * Creates a Deer Geometry, sets it up with some sweet images, attaches an instance of this controller, and wins
     * the game.
     *
     * @return A deer.
     */
    public static Node createDeer(Main app, boolean facingLeft, float scale)
    {
        Node commanderNode = new Node("Deer Commander");
        Picture deer = new Picture("Regular Deer");
        commanderNode.addControl(new DeerControl(app, deer, facingLeft, scale));
        commanderNode.setLocalTranslation(0, 0, ShootDeerState.Z_DEER);

        deer.setImage(app.getAssetManager(), IMG_WALKING, true);
        deer.getMaterial().getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        deer.setWidth(WIDTH);
        deer.setHeight(HEIGHT);

        if (facingLeft)
        {
            deer.scale(-1, 1, 1);
        }

        deer.scale(scale);

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
        // Remove the deer if it has moved off screen
        float x = getSpatial().getLocalTranslation().getX();
        boolean offLeft = x < 0 && goingLeft;
        boolean offRight = x > app.getCamera().getWidth() && !goingLeft;
        if (offLeft || offRight)
        {
            System.out.println("Goodbye from " + this);
            getSpatial().removeFromParent();
            return;
        }

        if (DeerState.RUNNING.equals(state) && System.currentTimeMillis() - madeLastStep >= STEP_DELAY)
        {
            playStepSound();
            madeLastStep = System.currentTimeMillis();
        }

        DeerState nextState = state;
        if (state != DeerState.DYING)
        {
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
        }

        if (!state.equals(nextState))
        {
            // Play a transition sound, if there is one
            playTransitionSound(state, nextState);

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
        float directionModifier = (goingLeft) ? -1 : 1;
        switch (state)
        {
            case DYING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                float speed = Math.min(DEER_DYING_SPEED * tpf, remainingDeathDistance);
                remainingDeathDistance -= speed;
                getSpatial().move(new Vector3f(0, -speed * widthScale, 0));
                break;
            case WALKING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                getSpatial().move(new Vector3f(directionModifier * deerSpeed * tpf * widthScale, 0, 0));
                break;
            case RUNNING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                getSpatial().move(new Vector3f(
                        directionModifier * deerSpeed * tpf * DEER_MOVEMENT_RUN_MODIFIER * widthScale, 0, 0));
                break;
            case EATING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                break;
            case JUMPING:
                imgHandle.setImage(app.getAssetManager(), currentAnimation.getCurrent(), true);
                getSpatial().move(new Vector3f(
                        directionModifier * deerSpeed * tpf * DEER_MOVEMENT_JUMP_MODIFIER * widthScale, 0, 0));
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
     * Play the sound of the deer taking a step. The sound is 3D-located to match the deer's location, with vertical
     * screen space mapped to 3D depth.
     */
    private void playStepSound()
    {
        // Pick a random deer step sound
        int soundIndex = (int) (Math.random() * deerStepSounds.size());
        playSoundAtLocation(deerStepSounds.get(soundIndex));
    }


    /**
     * Play the sound of the deer jumping. The sound is 3D-located to match the deer's location, with vertical
     * screen space mapped to 3D depth.
     */
    private void playJumpSound()
    {
        playSoundAtLocation(jumpSound);
    }


    /**
     * Play the sound of the deer landing a jump. The sound is 3D-located to match the deer's location, with vertical
     * screen space mapped to 3D depth.
     */
    private void playLandSound()
    {
        // TODO for now we use the same sound as jumping
        playSoundAtLocation(jumpSound);
    }


    /**
     * Play a sound at the deer's location using positional audio
     *
     * @param sound A sound that must be positional
     */
    private void playSoundAtLocation(AudioNode sound)
    {
        // Calculate where the sound should be (map Y to Z)
        Vector3f deerLoc = getSpatial().getLocalTranslation();
        Vector3f soundLoc = new Vector3f((deerLoc.getX() - app.getCamera().getWidth() / 2),
                deerLoc.getY(),
                0);

        // Move the sound node to the location and play the sound
        sound.setLocalTranslation(soundLoc);
        sound.playInstance();
    }


    /**
     * Play a sound effect for transitioning states. For example, this plays a jump sound when transitioning into the
     * jump state, and plays a landing sound when transitioning out of the jump state.
     *
     * @param previousState The state we are leaving
     * @param nextState     The state we are entering
     */
    private void playTransitionSound(DeerState previousState, DeerState nextState)
    {
        if (previousState.equals(nextState))
        {
            return;
        }

        if (DeerState.JUMPING.equals(nextState))
        {
            playJumpSound();
        } else if (DeerState.JUMPING.equals(previousState))
        {
            playLandSound();
        }
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


    private void initAudio()
    {
        deerStepSounds = new ArrayList<>();
        for (int i = 0; i < 5; ++i)
        {
            AudioNode sound = new AudioNode(app.getAssetManager(), "Audio/deer_step_" + i + ".wav", AudioData.DataType
                    .Buffer);
            sound.setPositional(true);
            sound.setVolume(2);
            deerStepSounds.add(sound);
        }

        jumpSound = new AudioNode(app.getAssetManager(), "Audio/deer_step_0.wav", AudioData.DataType.Buffer);
        jumpSound.setPositional(true);
        jumpSound.setVolume(10);
    }

    enum DeerState
    {
        WALKING, EATING, RUNNING, DYING, JUMPING
    }
}
