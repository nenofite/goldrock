package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

import java.util.ArrayList;
import java.util.List;

public class ShootDeerState extends AbstractAppState
{
    public static final String SHOOT_MAPPING = "Shoot Deer";
    public final static int Z_BACKGROUND = -10;
    public static final int Z_DEER = -5;
    public static final int Z_FOREGROUND = -2;
    /**
     * The delay it takes to reload, in ms
     */
    public static final long RELOAD_TIME = 1000;
    /**
     * How long before the player has to shoot at deer
     */
    public static final long TIME_LIMIT = 60 * 1000;
    /**
     * The number of deer to kill in order to see the MDMW ending
     */
    public static final int MDMW_KILL_COUNT = 1000;

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
     * The timestamp of when the player started this phase. We use this to know when the time is up and we move on to
     * the score screen or to MDMW.
     */
    private long started;
    /**
     * The timestamp when we started reloading, or -1 if we're not reloading
     */
    private long startedReloading;
    private List<DeerLane> lanes;

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

        //node.attachChild(makeForeground());

        // Add the kill count
        node.attachChild(KillCountControl.makeKillCount(this.app));

        // Add the countdown timer
        node.attachChild(CountdownControl.makeCountdown(this.app));

        // Add the bullets
        node.attachChild(BulletsControl.makeBullets(this.app));

        // Load the audio
        initAudio();

        // Attach our node
        this.app.getGuiNode().attachChild(node);

        until_next_deer = (float) (Math.random() * MAX_DEER_SPAWN_RATE);

        started = System.currentTimeMillis();

        lanes = new ArrayList<>();
        lanes.add(new DeerLane(0.1f, DeerLane.Orientation.LEFT_FACING, 5f, 15f, 30f, 40f, 50f));
        lanes.add(new DeerLane(0.4f, DeerLane.Orientation.RIGHT_FACING, 1f, 7f, 14f, 21f, 28f, 35f, 42f, 49f, 52f));
        lanes.add(new DeerLane(0.6f, DeerLane.Orientation.LEFT_FACING, 3f, 5f, 50f));
        lanes.add(new DeerLane(0.85f, DeerLane.Orientation.RIGHT_FACING, 3f, 5f, 50f));
    }

    private Spatial makeForeground()
    {
        Picture fg = new Picture("Foreground");
        fg.setImage(app.getAssetManager(), "Sprites/ForegroundLayer.png", true);
        fg.setWidth(app.getCamera().getWidth());
        fg.setHeight(app.getCamera().getHeight());
        fg.setLocalTranslation(0, 0, Z_FOREGROUND);
        return fg;
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

        for (DeerLane lane : lanes)
        {
            lane.update(tpf);
            if (lane.shouldSpawn())
            {
                float vertOffset = lane.getVerticalOffset(app.getCamera().getHeight());
                Node deer = DeerControl.createDeer(app, lane.getFacingLeft());
                deer.move(0, vertOffset, 0);
                if (lane.getFacingLeft())
                {
                    deer.move(app.getCamera().getWidth(), 0, 0);
                }
                node.attachChild(deer);
            }
        }

        if (startedReloading != -1 && System.currentTimeMillis() - startedReloading >= RELOAD_TIME)
        {
            finishReload();
        }

        if (System.currentTimeMillis() - started >= TIME_LIMIT)
        {
            gameOver();
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
     * Run when the timer runs out. This will either send the player to the newspaper screen or to the MDMW
     */
    public void gameOver()
    {
        AppState nextState;
        if (killCount < MDMW_KILL_COUNT)
        {
            nextState = new NewspaperState(killCount);
        } else
        {
            nextState = /* TODO MDMW */ null;
        }

        app.getStateManager().detach(this);
        app.getStateManager().attach(nextState);
    }


    /**
     * Get how much time the player has left to shoot, in ms
     */
    public long getTimeRemaining()
    {
        return TIME_LIMIT - (System.currentTimeMillis() - started);
    }

    /**
     * Make the background image
     *
     * @return A spatial for the background
     */
    private Spatial makeBackground()
    {
        Picture bg = new Picture("Background");
        bg.setImage(app.getAssetManager(), "Sprites/DeerPaths.jpg", true);
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
        gunshot.setPositional(false);
        node.attachChild(gunshot);

        gunReload = new AudioNode(app.getAssetManager(), "Audio/gun_reload.wav", AudioData.DataType.Buffer);
        gunReload.setPositional(false);
        gunReload.setVolume(2);
        gunReload.setPositional(false);
        node.attachChild(gunReload);
    }
}
