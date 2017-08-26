package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Node;

public class ShootDeerState extends AbstractAppState
{
    public final static int Z_BACKGROUND = 15;
    public static final int Z_FOREGROUND = 10;

    private Main app;
    private Node node;

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
    }

    /**
     * Called on initialization and when set to enabled
     */
    private void setup()
    {
        // Attach our node
        app.getGuiNode().attachChild(node);

        // Make a crosshair
        node.attachChild(CrosshairControl.makeCrosshair(app));
    }


    /**
     * Called when being taken down
     */
    private void teardown()
    {
        // Detach our node
        node.removeFromParent();
    }
}
