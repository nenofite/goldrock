package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

/**
 * Ask the player if they would like to play again. When they click, go back to ShootDeerState and start fresh.
 */
public class PlayAgainState extends AbstractAppState implements ActionListener
{
    private Main app;
    private Node node;

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);
        this.app = (Main) app;
        node = new Node("Play Again State");

        // Add the "Play again" message
        node.attachChild(makeMessage());

        // Listen to clicks to play again
        app.getInputManager().addListener(this, Main.NEXT_SCREEN_MAPPING);

        // Add the node to the world
        this.app.getGuiNode().attachChild(node);
    }

    @Override
    public void cleanup()
    {
        super.cleanup();

        // Unlisten
        app.getInputManager().removeListener(this);

        // Remove our node
        Utils.detachAllControls(node);
        node.removeFromParent();
    }


    /**
     * Make the image prompting the player to play again
     */
    private Spatial makeMessage()
    {
        Picture msg = new Picture("Play Again Message");
        msg.setImage(app.getAssetManager(), "Sprites/play_again.png", true);
        msg.setWidth(app.getCamera().getWidth());
        msg.setHeight(app.getCamera().getHeight());

        return msg;
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        if (Main.NEXT_SCREEN_MAPPING.equals(name) && isPressed)
        {
            // Continue to a new ShootDeerState
            app.getStateManager().detach(this);
            app.getStateManager().attach(new ShootDeerState(1, 0));
        }
    }
}
