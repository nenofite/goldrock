package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

import java.util.ArrayList;
import java.util.List;

public class ShootDeerState extends AbstractAppState implements ActionListener
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
    public static final long DELAY_BETWEEN_HUNTS = 5 * 1000 + 300;
    public static final long DELAY_AFTER_WOLF_DIE = 2 * 1000;
    /**
     * The number of deer to kill in order to see the MDMW ending
     */
    public static final int MDMW_KILL_COUNT = 10;
    public static final int DEER_ROUND_ONE = 13;
    public static final int DEER_ROUND_TWO = 22;
    public static final int DEER_ROUND_THREE = 39;
    /**
     * The total deer across all rounds
     */
    public static final int TOTAL_DEER = 13 + 22 + 39;
    private static final String AUDIO_SLOW = "Audio/hunt_slow_short.wav";
    private static final String AUDIO_MED = "Audio/hunt_med.wav";
    private static final String AUDIO_FAST = "Audio/hunt_fast.wav";
    private Main app;
    private Node node;
    private AudioNode gunshot;
    private AudioNode gunReload;
    private AudioNode music;
    private AudioNode intermissionTune;
    private int maxBullets = 3;
    private int bullets;
    private int totalKillCount;
    /**
     * When we finished the previous hunt and started waiting to start the next, or -1 if we're currently hunting
     */
    private long startedWaitForNextHunt = -1;
    /**
     * When the wolf died, or -1 if it hasn't died yet. We use this to know when to switch to the newspaper screen after
     */
    private long startedWaitAfterWolfDie = -1;
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
    private Node titleEarned;
    private boolean doneIn = false;

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
        node.attachChild(KillCountControl.makeKillCount(this.app, KillCountControl.KillCountType.TOTAL));

        // Add the bullets
        node.attachChild(BulletsControl.makeBullets(this.app));

        // Add the text background bar
        node.attachChild(makeTextBar());

        // Load the audio
        initAudio();

        this.app.getInputManager().addListener(this, Main.SKIP_DEER_MAPPING);

        // Attach our node
        this.app.getGuiNode().attachChild(node);

        setupHunt(huntNumber);
    }

    private void setupHunt(int huntNumber)
    {
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
            case 4:
                music = new AudioNode(app.getAssetManager(), "Audio/mdmw.wav", AudioData.DataType.Stream);
                prepareFinalHunt();
                break;
            default:
                throw new IllegalStateException("Too many hunts!");
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

    private void prepareFinalHunt()
    {
        doneIn = false;
        ++activeDeer;
        Spatial s = MdmwControl.createMdmw(app);
        node.attachChild(s);
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

        if (music != null)
        {
            music.stop();
            music = null;
        }

        app.getInputManager().removeListener(this);

        // Detach our node
        Utils.detachAllControls(node);
        node.removeFromParent();
    }

    @Override
    public void update(float tpf)
    {
        super.update(tpf);
        app.getInputManager().setCursorVisible(false);

        if (startedWaitAfterWolfDie != -1)
        {
            if (System.currentTimeMillis() - startedWaitAfterWolfDie >= DELAY_AFTER_WOLF_DIE)
            {
                AppState nextState = new NewspaperState(1);
                app.getStateManager().detach(this);
                app.getStateManager().attach(nextState);
            }
        } else if (startedWaitForNextHunt != -1)
        {
            if (System.currentTimeMillis() - startedWaitForNextHunt >= DELAY_BETWEEN_HUNTS)
            {
                beginNextHunt();
            }
        } else
        {
            if (activeDeer == 0 && lanes.stream().noneMatch(DeerLane::hasDeer))
            {
                finishHunt();
            } else if (huntNumber < 4)
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
            }
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
        // Stop the music
        if (music != null)
        {
            music.stop();
            node.detachChild(music);
            music = null;
        }

        // Play the short intermission tune
        intermissionTune.playInstance();

        startedWaitForNextHunt = System.currentTimeMillis();

        awardTitle();
    }

    /**
     * Begin the next hunt after finishing a delay
     */
    public void beginNextHunt()
    {
        startedWaitForNextHunt = -1;
        removeTitle();

        if (huntNumber == 3 && totalKillCount < MDMW_KILL_COUNT)
        {
            AppState nextState = new NewspaperState((float) totalKillCount / TOTAL_DEER);
            app.getStateManager().detach(this);
            app.getStateManager().attach(nextState);
        } else
        {
            setupHunt(++huntNumber);
        }
    }

    /**
     * Display some "Title earned" text based on the round and the kill count
     */
    private void awardTitle()
    {
        // Pick the title text
        final String titleStr;
        float fracKilled = 0.0f;
        switch (huntNumber)
        {
            case 1:
                fracKilled = (float) totalKillCount / DEER_ROUND_ONE;
                if (fracKilled < 0.3f)
                {
                    titleStr = "Lousy Shot";
                }
                else if (fracKilled < 0.7f)
                {
                    titleStr = "Beginner Hunter";
                }
                else if (fracKilled < 0.999f)
                {
                    titleStr = "Talented Rookie";
                }
                else
                {
                    titleStr = "Rising Star";
                }
                break;
            case 2:
                fracKilled = (float) totalKillCount / (DEER_ROUND_ONE + DEER_ROUND_TWO);
                if (fracKilled < 0.3f)
                {
                    titleStr = "Consistently Lousy Shot";
                }
                else if (fracKilled < 0.7f)
                {
                    titleStr = "Competent Hunter";
                }
                else if (fracKilled < 0.9f)
                {
                    titleStr = "Gifted Hunter";
                }
                else if (fracKilled < 0.999f)
                {
                    titleStr = "Deadeye";
                }
                else
                {
                    titleStr = "Inescapable";
                }
                break;
            case 3:
                fracKilled = (float) totalKillCount / (DEER_ROUND_ONE + DEER_ROUND_TWO + DEER_ROUND_THREE);
                if (fracKilled < 0.001f)
                {
                    titleStr = "Friend of the Deer";
                }
                else if (fracKilled < 0.3f)
                {
                    titleStr = "Nearly Blind";
                }
                else if (fracKilled < 0.7f)
                {
                    titleStr = "Seasoned Veteran";
                }
                else if (fracKilled < 0.999f)
                {
                    titleStr = "Slaughterer of the Masses";
                }
                else
                {
                    titleStr = "Oh God Why";
                }
                break;
            default:
                titleStr = "Missingo";
        }

        // Make some text
        BitmapText titleText = new BitmapText(app.getBigFont());
        titleText.setSize(40);
        titleText.setColor(ColorRGBA.White);
        titleText.setText(titleStr);

        // Make a header
        BitmapText headerText = new BitmapText(app.getBigItalicFont());
        headerText.setSize(25);
        headerText.setColor(ColorRGBA.White);
        headerText.setText("Title earned:");

        // Make a background bar
        Picture bar = new Picture("Title Bar");
        bar.setImage(app.getAssetManager(), "Sprites/text_bar.png", true);
        bar.setWidth(800);
        bar.setHeight(80);

        // Position the text
        titleText.setLocalTranslation(100, app.getCamera().getHeight() / 2, 10);
        headerText.setLocalTranslation(100, app.getCamera().getHeight() / 2 + 30, 10);
        bar.setLocalTranslation(0, app.getCamera().getHeight() / 2 - titleText.getHeight(), 5);

        Node titleNode = new Node("Title Node");
        titleNode.attachChild(titleText);
        titleNode.attachChild(headerText);
        titleNode.attachChild(bar);

        // Show the text
        titleEarned = titleNode;
        node.attachChild(titleEarned);
    }

    /**
     * Remove the "Title earned" text
     */
    private void removeTitle()
    {
        if (titleEarned != null)
        {
            titleEarned.removeFromParent();
            titleEarned = null;
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
        node.attachChild(gunReload);

        intermissionTune = new AudioNode(app.getAssetManager(), "Audio/rag_short.wav", AudioData.DataType.Buffer);
        intermissionTune.setPositional(false);
        intermissionTune.setVolume(1);
        intermissionTune.setLooping(false);
        node.attachChild(intermissionTune);
    }

    /**
     * Make the translucent dark bar that serves as a background for the kill count text
     */
    private Spatial makeTextBar()
    {
        final float width = 800;
        final float height = 50;

        Picture bar = new Picture("Text Bar");
        bar.setImage(app.getAssetManager(), "Sprites/text_bar.png", true);
        bar.setWidth(width);
        bar.setHeight(height);

        bar.setLocalTranslation(0, app.getCamera().getHeight() - height, 5);

        return bar;
    }

    public void notifyWolfKilledYou()
    {
        if (!doneIn)
        {
            node.attachChild(WolfEatsYouControl.createWolfEatingYou(app));
            doneIn = true;
            Spatial chair = node.getChild("Chair");
            if (chair != null)
            {
                Utils.detachAllControls((Node) chair);
                chair.removeFromParent();
            }
        }
    }

    public void notifyWolfDied()
    {
        startedWaitAfterWolfDie = System.currentTimeMillis();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        if (Main.SKIP_DEER_MAPPING.equals(name) && isPressed)
        {
            clearAllDeer();
        }
    }


    /**
     * Clear all the deer on the screen so we can skip to the end of this hunt. Used for debugging.
     */
    private void clearAllDeer()
    {
        System.out.println("Removing all deer");
        lanes.clear();
        new ArrayList<>(node.getChildren()).forEach(it -> {
            if (it.getControl(DeerControl.class) != null)
            {
                it.removeFromParent();
                notifyDeerRemoved();
            }
        });
        activeDeer = 0;
    }
}
