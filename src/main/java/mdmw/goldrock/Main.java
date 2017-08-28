package mdmw.goldrock;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.system.AppSettings;

public class Main extends SimpleApplication
{
    public static final boolean IS_DEBUG = true;
    public static final String NEXT_SCREEN_MAPPING = "Next Screen";
    public static final String SKIP_DEER_MAPPING = "(Debug) Skip Deer";

    private BitmapFont bigFont;
    private BitmapFont bigItalicFont;

    public Main()
    {
        AppSettings settings = new AppSettings(true);
        settings.setSettingsDialogImage("Sprites/title.png");
        settings.setVSync(true);
        settings.setFrameRate(100);
        settings.setResolution(800, 800);
        setSettings(settings);
        setShowSettings(false);
        setPauseOnLostFocus(false);
        setDisplayFps(false);
    }

    public static void main(String[] args)
    {
        new Main().start();
    }

    @Override
    public void simpleInitApp()
    {
        setDisplayStatView(false);
        flyCam.setEnabled(false);

        // Load fonts
        guiFont = assetManager.loadFont("Fonts/gui.fnt");
        bigFont = assetManager.loadFont("Fonts/big.fnt");
        bigItalicFont = assetManager.loadFont("Fonts/big_italic.fnt");

        // Setup mappings

        // Map click or spacebar to start or go to next screen (eg. after newspaper)
        getInputManager().addMapping(NEXT_SCREEN_MAPPING, new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));

        // Register the mouse button and the space bar to shoot
        getInputManager().addMapping(ShootDeerState.SHOOT_MAPPING, new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));

        if (IS_DEBUG)
        {
            getInputManager().addMapping(SKIP_DEER_MAPPING, new KeyTrigger(KeyInput.KEY_F7));
        }

        stateManager.attach(new TitleState());
    }

    @Override
    public void simpleUpdate(float tpf)
    {
        super.simpleUpdate(tpf);
    }

    public BitmapFont getGuiFont()
    {
        return guiFont;
    }

    public BitmapFont getBigFont()
    {
        return bigFont;
    }

    public BitmapFont getBigItalicFont()
    {
        return bigItalicFont;
    }
}
