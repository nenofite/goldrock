package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

public class ShootDeerState extends AbstractAppState
{
    public static final String SHOOT_MAPPING = "Shoot Deer";
    public final static int Z_BACKGROUND = -10;
    public static final int Z_FOREGROUND = -5;
    private static final int MAX_DEER_SPAWN_RATE = 10;

    private Main app;
    private Node node;
    private float until_next_deer = 0.0f;

    private int killCount;

    public ShootDeerState()
    {
        node = new Node("ShootDeerState");
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        this.app = (Main) app;

        super.initialize(stateManager, app);
        setup();

        // Make a crosshair
        node.attachChild(CrosshairControl.makeCrosshair(app));

        // Add the background
        node.attachChild(makeBackground());

        // Register the mouse button and the space bar to shoot
        app.getInputManager().addMapping(SHOOT_MAPPING,
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));

        // Add the kill count
        node.attachChild(KillCountControl.makeKillCount(this.app));
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        boolean wasEnabled = isEnabled();
        super.setEnabled(enabled);

        if (wasEnabled == enabled)
        {
            return;
        }

        if (enabled)
        {
            setup();
        } else
        {
            teardown();
        }
    }

    @Override
    public void update(float tpf)
    {
        super.update(tpf);
        app.getInputManager().setCursorVisible(false);

        until_next_deer -= tpf;
        if (until_next_deer <= 0)
        {
            float rand = (float) Math.random();
            Node deerNode = DeerControl.createDeer(app);
            deerNode.move(app.getCamera().getWidth(), (app.getCamera().getHeight() - DeerControl.HEIGHT) * rand, 0);
            node.attachChild(deerNode);
            until_next_deer = (float) (Math.random() * MAX_DEER_SPAWN_RATE);
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

    /**
     * Called on initialization and when set to enabled
     */
    private void setup()
    {
        // Attach our node
        app.getGuiNode().attachChild(node);

        until_next_deer = (float) (Math.random() * MAX_DEER_SPAWN_RATE);
    }

    /**
     * Called when being taken down
     */
    private void teardown()
    {
        // Detach our node
        node.removeFromParent();
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
}
