package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

public class ShootDeerState extends AbstractAppState
{
    public static final String SHOOT_MAPPING = "Shoot Deer";
    public final static int Z_BACKGROUND = -10;
    public static final int Z_FOREGROUND = -5;
    /**
     * The delay it takes to reload, in ms
     */
    public static final long RELOAD_TIME = 500;
    private static final int MAX_DEER_SPAWN_RATE = 3;
    private Main app;
    private Node node;

    private AudioNode gunshot;
    private AudioNode gunReload;

    private float until_next_deer = 0.0f;
    private int maxBullets = 3;
    private int bullets;
    private int killCount;
    /**
     * The timestamp when we started reloading, or -1 if we're not reloading
     */
    private long startedReloading;

    public ShootDeerState()
    {
        node = new Node("ShootDeerState");
        bullets = maxBullets;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);

        this.app = (Main) app;

        // Make a crosshair
        node.attachChild(CrosshairControl.makeCrosshair(this.app));

        // Add the background
        node.attachChild(makeBackground());

        // Add the kill count
        node.attachChild(KillCountControl.makeKillCount(this.app));

        // Add the bullets
        node.attachChild(BulletsControl.makeBullets(this.app));

        // Load the audio
        initAudio();

        // Attach our node
        this.app.getGuiNode().attachChild(node);

        until_next_deer = (float) (Math.random() * MAX_DEER_SPAWN_RATE);
    }

    @Override
    public void cleanup()
    {
        super.cleanup();

        // Detach our node
        node.removeFromParent();
    }

    @Override
    public void update(float tpf)
    {
        super.update(tpf);
        app.getInputManager().setCursorVisible(false);

        until_next_deer -= tpf;
        if (until_next_deer <= 0)
        {
            boolean onRight = Math.random() < 0.5;
            float rand = (float) Math.random();
            Node deerNode = DeerControl.createDeer(app, onRight);
            if (onRight)
            {
                deerNode.move(app.getCamera().getWidth(), (app.getCamera().getHeight() - DeerControl.HEIGHT) * rand, 0);
            } else
            {
                deerNode.move(0, (app.getCamera().getHeight() - DeerControl.HEIGHT) * rand, 0);
            }
            node.attachChild(deerNode);
            until_next_deer = (float) (Math.random() * MAX_DEER_SPAWN_RATE);
        }

        if (startedReloading != -1 && System.currentTimeMillis() - startedReloading >= RELOAD_TIME)
        {
            finishReload();
        }
    }

    /**
     * Called by DeerControl when a deer is killed. This increments the score.
     */
    public void onKillDeer()
    {
        ++killCount;
    }

    public Node getNode()
    {
        return node;
    }

    public int getKillCount()
    {
        return killCount;
    }

    public int getMaxBullets()
    {
        return maxBullets;
    }

    public int getBullets()
    {
        return bullets;
    }

    /**
     * Whether the player can currently shoot. This means they have a positive number of bullets and are not currently
     * reloading. This is called by CrosshairControl before firing a bullet
     *
     * @return Whether the player is capable of shooting right now
     */
    public boolean canShoot()
    {
        return bullets > 0 && startedReloading == -1;
    }

    /**
     * Decrement the amount of bullets and play a shot sound
     */
    public void fireBullet()
    {
        if (bullets > 0)
        {
            --bullets;

            gunshot.playInstance();
            System.out.println("POW!");
        }

        if (bullets == 0)
        {
            reload();
        }
    }

    /**
     * Start reloading. This blocks the player from shooting, plays sound, and after a delay restores bullets to max.
     */
    public void reload()
    {
        if (startedReloading == -1)
        {
            startedReloading = System.currentTimeMillis();
        }
    }

    /**
     * Finish the reloading delay, restoring the bullets to max and enabling the player to shoot again
     */
    public void finishReload()
    {
        if (startedReloading != -1)
        {
            startedReloading = -1;
            bullets = maxBullets;
            gunReload.playInstance();
        }
    }

    /**
     * Make the background image
     *
     * @return A spatial for the background
     */
    private Spatial makeBackground()
    {
        Picture bg = new Picture("Background");
        bg.setImage(app.getAssetManager(), "Sprites/background.png", true);
        bg.setWidth(app.getCamera().getWidth());
        bg.setHeight(app.getCamera().getHeight());
        bg.setLocalTranslation(0, 0, Z_BACKGROUND);
        return bg;
    }


    /**
     * Set up all the audio nodes
     */
    private void initAudio()
    {
        gunshot = new AudioNode(app.getAssetManager(), "Audio/gunshot.wav", AudioData.DataType.Buffer);
        gunshot.setPositional(false);
        gunshot.setVolume(2);
        node.attachChild(gunshot);

        gunReload = new AudioNode(app.getAssetManager(), "Audio/gun_reload.wav", AudioData.DataType.Buffer);
        gunReload.setPositional(false);
        gunReload.setVolume(2);
        node.attachChild(gunReload);
    }
}
