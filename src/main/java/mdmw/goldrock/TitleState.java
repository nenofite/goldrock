package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

/**
 * Display the title screen and "Click to start"
 */
public class TitleState extends AbstractAppState implements ActionListener
{
    private static final String START_GAME_MAPPING = "Start Game";

    private Main app;
    private Node node;

    public TitleState()
    {
        node = new Node("Title State");
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);
        this.app = (Main) app;

        // Map click or spacebar to start
        this.app.getInputManager().addMapping(START_GAME_MAPPING,
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));

        // Add the title background
        node.attachChild(makeTitleBackground());

        // Register as a listener
        app.getInputManager().addListener(this, START_GAME_MAPPING);

        // Attach our node
        this.app.getGuiNode().attachChild(node);
    }

    @Override
    public void cleanup()
    {
        super.cleanup();

        // Deregister our listener
        app.getInputManager().removeListener(this);

        // Detach our node
        node.removeFromParent();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        if (START_GAME_MAPPING.equals(name) && isPressed)
        {
            // Switch to shoot deer state
            app.getStateManager().detach(this);
            app.getStateManager().attach(new ShootDeerState());
        }
    }


    /**
     * Make the title picture background, which displays the title of the game and the prompt "Click to start"
     *
     * @return A background image
     */
    private Spatial makeTitleBackground()
    {
        Picture bg = new Picture("Title Background");
        bg.setImage(app.getAssetManager(), "Sprites/title.png", true);
        bg.setWidth(app.getCamera().getWidth());
        bg.setHeight(app.getCamera().getHeight());

        return bg;
    }
}
