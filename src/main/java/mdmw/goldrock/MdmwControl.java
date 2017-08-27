package mdmw.goldrock;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

public class MdmwControl extends AbstractControl implements Shootable
{
    public static final int WIDTH = 100;
    public static final int HEIGHT = 75;
    public static final float BASE_SPEED = 600;
    private static final float BASE_REGEN_DELAY = 3;
    private static final int BASE_HEALTH = 8;
    private Main app;
    private Picture imgHandle;
    private AnimationStation animation;
    private AudioNode growlSound;
    private AudioNode snarlSound;
    private AudioNode howlSound;
    private int lap;
    private float remainingRegenDelay;
    private int health;

    private MdmwControl(Main app, Picture imgHandle)
    {
        this.app = app;
        this.imgHandle = imgHandle;

        animation = createRunAnimation();

        health = BASE_HEALTH;
    }

    private static AnimationStation createRunAnimation()
    {
        AnimationStation ret = new AnimationStation();
        ret.addImage("Sprites/wolf_running_0.png", 0.3f);
        ret.addImage("Sprites/wolf_running_1.png", 0.7f);
        return ret;
    }

    private static AnimationStation createDeathAnimation()
    {
        AnimationStation ret = new AnimationStation();
        ret.addImage("Sprites/mdmw_death.png", 1.0f);
        return ret;
    }

    public static Spatial createMdmw(Main app)
    {
        Node node = new Node("nothing can save you now");
        Picture mdmw = createPicture(app.getAssetManager());

        node.addControl(new MdmwControl(app, mdmw));
        node.setLocalTranslation(0, 0, ShootDeerState.Z_DEER);

        float mod = (getFacingLeftForLap(0)) ? -1f : 1f;
        float scale = getScaleForLap(1);

        mdmw.scale(mod * scale, 1, 1);
        node.attachChild(mdmw);

        node.setLocalTranslation(app.getCamera().getWidth(), app.getCamera().getHeight() * getVerticalFractionForLap(0),
                0);

        return node;
    }

    @Override
    public void setSpatial(Spatial spatial)
    {
        super.setSpatial(spatial);
        if (spatial != null)
        {
            // Make the sound effect nodes
            initAudio();

            // Play the starting howl
            howlSound.playInstance();
        } else
        {

        }
    }

    private static Picture createPicture(AssetManager assetManager)
    {
        Picture mdmw = new Picture("your worst nightmare");
        mdmw.setImage(assetManager, "Sprites/mdmw.png", true);
        mdmw.getMaterial().getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        mdmw.setWidth(WIDTH);
        mdmw.setHeight(HEIGHT);
        return mdmw;
    }

    private static boolean getFacingLeftForLap(int lap)
    {
        return lap % 2 == 0;
    }

    private static float getScaleForLap(int lap)
    {
        switch (lap)
        {
            case 0:
                return 0.6f;
            case 1:
                return 0.75f;
            case 2:
                return 1f;
            case 3:
                return 1.25f;
            default:
                return 1f;
        }
    }

    private static float getVerticalFractionForLap(int lap)
    {
        switch (lap)
        {
            case 0:
                return 0.85f;
            case 1:
                return 0.6f;
            case 2:
                return 0.4f;
            case 3:
                return 0.1f;
            default:
                return 0.5f;
        }
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        float x = getSpatial().getLocalTranslation().getX();
        boolean offLeft = x < 0 && getFacingLeftForLap(lap);
        boolean offRight = x > app.getCamera().getWidth() && !getFacingLeftForLap(lap);
        if (offLeft || offRight && remainingRegenDelay < 0.0001f)
        {
            System.out.println("MDMW completed a lap");
            playSoundAtLocation(growlSound);
            imgHandle.removeFromParent();
            remainingRegenDelay = BASE_REGEN_DELAY - tpf;

            getSpatial().setLocalTranslation(app.getCamera().getWidth() / 2, app.getCamera().getHeight() / 2, -2);
            imgHandle = null;
        }

        if (remainingRegenDelay > 0.00001f)
        {
            remainingRegenDelay -= tpf;
        } else
        {
            if (imgHandle == null)
            {
                if (lap >= 3)
                {
                    ShootDeerState state = app.getStateManager().getState(ShootDeerState.class);
                    if (state != null)
                    {
                        state.notifyWolfKilledYou();
                    }
                    return;
                } else
                {
                    lap += 1;

                    imgHandle = createPicture(app.getAssetManager());
                    ((Node) getSpatial()).attachChild(imgHandle);
                    imgHandle.setImage(app.getAssetManager(), animation.getCurrent(), true);

                    boolean facingLeft = getFacingLeftForLap(lap);
                    float scale = getScaleForLap(lap);
                    float posneg = (facingLeft) ? -1 : 1;

                    imgHandle.scale(posneg * scale, 1, 1);

                    float xLoc = (facingLeft) ? app.getCamera().getWidth() : 0;
                    float yLoc = getVerticalFractionForLap(lap) * app.getCamera().getHeight();

                    getSpatial().setLocalTranslation(xLoc, yLoc, ShootDeerState.Z_DEER);
                    playSoundAtLocation(snarlSound);
                }
            }

            animation.progress(tpf);
            imgHandle.setImage(app.getAssetManager(), animation.getCurrent(), true);

            if (health > 0)
            {
                float posneg = (getFacingLeftForLap(lap)) ? -1 : 1;
                getSpatial().move(posneg * BASE_SPEED * getScaleForLap(lap) * tpf, 0, 0);
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }

    @Override
    public void shoot()
    {
        System.out.println("MDMW hit");

        --health;
        if (health <= 0)
        {
            System.out.println("MDMW should die.");
            animation = createDeathAnimation();
            ShootDeerState state = app.getStateManager().getState(ShootDeerState.class);
            if (state != null)
            {
                state.notifyWolfDied();
            }
        }
    }

    /**
     * Make the sound effect nodes
     */
    private void initAudio()
    {
        snarlSound = new AudioNode(app.getAssetManager(), "Audio/mdmw_snarl.wav", AudioData.DataType.Buffer);
        snarlSound.setPositional(true);
        snarlSound.setVolume(16);

        growlSound = new AudioNode(app.getAssetManager(), "Audio/mdmw_growl.wav", AudioData.DataType.Buffer);
        growlSound.setPositional(true);
        growlSound.setVolume(16);

        howlSound = new AudioNode(app.getAssetManager(), "Audio/mdmw_howl.wav", AudioData.DataType.Buffer);
        howlSound.setPositional(false);
        howlSound.setVolume(32);
    }

    /**
     * Play a sound at the wolf's location using positional audio
     *
     * @param sound A sound that must be positional
     */
    private void playSoundAtLocation(AudioNode sound)
    {
        // Calculate where the sound should be (map Y to Z)
        Vector3f deerLoc = getSpatial().getLocalTranslation();
        Vector3f soundLoc = new Vector3f((deerLoc.getX() - app.getCamera().getWidth() / 2), deerLoc.getY(), 0);

        // Move the sound node to the location and play the sound
        sound.setLocalTranslation(soundLoc);
        sound.playInstance();
    }
}
