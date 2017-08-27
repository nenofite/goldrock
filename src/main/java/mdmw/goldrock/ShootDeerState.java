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
     * How long to delay between hunts
     */
    public static final long DELAY_BETWEEN_HUNTS = 5 * 1000;
    /**
     * The number of deer to kill in order to see the MDMW ending
     */
    public static final int MDMW_KILL_COUNT = 1000;
    private static final String AUDIO_SLOW = "Audio/hunt_slow.wav";
    private static final String AUDIO_MED = "Audio/hunt_med.wav";
    private static final String AUDIO_FAST = "Audio/hunt_fast.wav";
    private Main app;
    private Node node;
    private AudioNode gunshot;
    private AudioNode gunReload;
    private AudioNode music;
    private int maxBullets = 3;
    private int bullets;
    private int killCount;
    private int totalKillCount;
    /**
     * When we finished the previous hunt and started waiting to start the next, or -1 if we're currently hunting
     */
    private long startedWaitForNextHunt = -1;
    /**
     * The timestamp when we started reloading, or -1 if we're not reloading
     */
    private long startedReloading;
    private List<DeerLane> lanes;
    /**
     * The number of hunts we have been on, including this one.
     */
    private int huntNumber;
    private int activeDeer;

    public ShootDeerState(int huntNumber, int prevKillCount)
    {
        node = new Node("ShootDeerState");
        bullets = maxBullets;
        this.huntNumber = huntNumber;
        totalKillCount = prevKillCount;
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

        node.attachChild(makeForeground());

        // Add the kill count
        node.attachChild(KillCountControl.makeKillCount(this.app, KillCountControl.KillCountType.HUNT));

        // Add the kill count
        node.attachChild(KillCountControl.makeKillCount(this.app, KillCountControl.KillCountType.TOTAL));

        // Add the bullets
        node.attachChild(BulletsControl.makeBullets(this.app));

        // Load the audio
        initAudio();

        // Attach our node
        this.app.getGuiNode().attachChild(node);

        setupHunt(huntNumber);
    }

    private void setupHunt(int huntNumber)
    {
        if (music != null)
        {
            music.stop();
            node.detachChild(music);
            music = null;
        }
        switch (huntNumber)
        {
            case 1:
                music = new AudioNode(app.getAssetManager(), AUDIO_SLOW, AudioData.DataType.Stream);
                prepareFirstHunt();
                break;
            case 2:
                music = new AudioNode(app.getAssetManager(), AUDIO_MED, AudioData.DataType.Stream);
                prepareSecondHunt();
                break;
            case 3:
                music = new AudioNode(app.getAssetManager(), AUDIO_FAST, AudioData.DataType.Stream);
                prepareThirdHunt();
                break;
            default:
                throw new IllegalStateException("Only three hunts!");
        }
        music.setLooping(true);
        music.setPositional(false);
        node.attachChild(music);
        music.play();
    }

    private void prepareFirstHunt()
    {
        // total deer: 13
        lanes = new ArrayList<>();
        lanes.add(new DeerLane(0.1f, 1.25f, DeerLane.Orientation.LEFT_FACING, 5f, 15f)); // 2 deer
        lanes.add(new DeerLane(0.4f, 1f, DeerLane.Orientation.RIGHT_FACING, 0.5f, 7f, 14f, 21f)); // 4 deer
        lanes.add(new DeerLane(0.6f, 0.75f, DeerLane.Orientation.LEFT_FACING, 2f, 5f, 20f)); // 3 deer
        lanes.add(new DeerLane(0.85f, 0.6f, DeerLane.Orientation.RIGHT_FACING, 3f, 5f, 15f, 25f)); // 4 deer
    }

    private void prepareSecondHunt()
    {
        // total deer: 22
        lanes = new ArrayList<>();
        lanes.add(new DeerLane(0.1f, 1.25f, DeerLane.Orientation.LEFT_FACING, 1f, 5f, 7f, 13f, 15f, 20f, 24f)); // 7
        // deer
        lanes.add(new DeerLane(0.4f, 1f, DeerLane.Orientation.RIGHT_FACING, 0.5f, 7f, 14f, 21f)); // 4 deer
        lanes.add(new DeerLane(0.6f, 0.75f, DeerLane.Orientation.LEFT_FACING, 2f, 5f, 11f, 18f, 20f, 23f)); // 6 deer
        lanes.add(new DeerLane(0.85f, 0.6f, DeerLane.Orientation.RIGHT_FACING, 3f, 5f, 15f, 20f, 25f)); // 5 deer
    }

    private void prepareThirdHunt()
    {
        // total deer: 39
        lanes = new ArrayList<>();
        lanes.add(new DeerLane(0.1f, 1.25f, DeerLane.Orientation.LEFT_FACING, 1f, 5f, 7f, 12f, 13f, 15f, 20f, 24f));
        // 8 deer
        lanes.add(new DeerLane(0.4f, 1f, DeerLane.Orientation.RIGHT_FACING, 0.5f, 1f, 6f, 7f, 12f, 14f, 21f, 24f));
        // 8 deer
        lanes.add(new DeerLane(0.6f, 0.75f, DeerLane.Orientation.LEFT_FACING, 2f, 5f, 11f, 15f, 18f, 20f, 23f, 25f));
        // 8 deer
        lanes.add(new DeerLane(0.62f, 0.73f, DeerLane.Orientation.RIGHT_FACING, 3f, 2f, 9f, 12f, 16f, 20f, 23f, 25f));
        // 8 deer
        lanes.add(new DeerLane(0.85f, 0.6f, DeerLane.Orientation.RIGHT_FACING, 3f, 5f, 10f, 12f, 15f, 20f, 25f));
        // 7 deer
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

        music.stop();

        // Detach our node
        Utils.detachAllControls(node);
        node.removeFromParent();
    }

    @Override
    public void update(float tpf)
    {
        super.update(tpf);
        app.getInputManager().setCursorVisible(false);

        if (startedWaitForNextHunt != -1)
        {
            if (System.currentTimeMillis() - startedWaitForNextHunt >= DELAY_BETWEEN_HUNTS)
            {
                beginNextHunt();
            }
        } else
        {
            if (activeDeer == 0 && lanes.stream().allMatch(e -> !e.hasDeer()))
            {
                finishHunt();
            } else
            {
                for (DeerLane lane : lanes)
                {
                    lane.update(tpf);
                    if (lane.shouldSpawn())
                    {
                        float vertOffset = lane.getVerticalOffset(app.getCamera().getHeight());
                        Node deer = DeerControl.createDeer(app, lane.getFacingLeft(), lane.getDeerScale());
                        deer.move(0, vertOffset, 0);
                        if (lane.getFacingLeft())
                        {
                            deer.move(app.getCamera().getWidth(), 0, 0);
                        }
                        node.attachChild(deer);
                        ++activeDeer;
                    }
                }

                if (startedReloading != -1 && System.currentTimeMillis() - startedReloading >= RELOAD_TIME)
                {
                    finishReload();
                }
            }
        }
    }

    /**
     * Called by DeerControl when a deer is killed. This increments the score.
     */
    public void onKillDeer()
    {
        ++killCount;
        ++totalKillCount;
        notifyDeerRemoved();
    }

    /**
     * Call only when removing a deer from the play field. This is used to determine when the hunt ends, and relies
     * on trust alone.
     */
    public void notifyDeerRemoved()
    {
        --activeDeer;
    }

    public Node getNode()
    {
        return node;
    }

    public int getKillCount()
    {
        return killCount;
    }

    public int getTotalKillCount()
    {
        return totalKillCount;
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
     * Run when there are no more deer to kill. This will have a delay, and then put the user into the next hunt.
     */
    public void finishHunt()
    {
        killCount = 0;
        startedWaitForNextHunt = System.currentTimeMillis();
    }


    /**
     * Begin the next hunt after finishing a delay
     */
    public void beginNextHunt()
    {
        startedWaitForNextHunt = -1;

        if (huntNumber == 3)
        {
            AppState nextState;
            if (killCount < MDMW_KILL_COUNT)
            {
                nextState = new NewspaperState(totalKillCount, huntNumber);
            } else
            {
                nextState = /* TODO MDMW */ null;
            }

            app.getStateManager().detach(this);
            app.getStateManager().attach(nextState);
        } else
        {
            setupHunt(++huntNumber);
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
        bg.setImage(app.getAssetManager(), "Sprites/BG.png", true);
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
        gunReload.setPositional(false);
        node.attachChild(gunReload);
    }
}
