package mdmw.goldrock;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;

import java.io.IOException;

/**
 * Defines logic for controlling deer as they frolick about the forest. If you are kind to them, they will ignore you.
 */
public class DeerControl extends AbstractControl
{
    private static final int WIDTH = 100;
    private static final int HEIGHT = 75;
    private static final int ACCRUE_THRESHOLD = 5;
    private static final int DEER_MOVEMENT_MAX = 40;
    private static final int DEER_MOVEMENT_MIN = 25;
    private Picture imgHandle;
    private AssetManager assetManager;
    private float accrue = 0.0f;
    private float deerSpeed = 0.0f;

    public DeerControl(AssetManager assets, Picture imgHandle)
    {
        this.imgHandle = imgHandle;
        this.assetManager = assets;
        deerSpeed = (float) (Math.random() * (DEER_MOVEMENT_MAX - DEER_MOVEMENT_MIN)) + DEER_MOVEMENT_MIN;
    }

    /**
     * Creates a Deer Geometry, sets it up with some sweet images, attaches an instance of this controller, and wins
     * the game.
     *
     * @return A deer.
     */
    public static Node createDeer(AssetManager assetManager)
    {
        Picture deer = new Picture("Deer");
        deer.setImage(assetManager, "Sprites/deer.png", true);
        deer.setWidth(WIDTH);
        deer.setHeight(HEIGHT);

        Node commanderNode = new Node("Deer Commander");
        commanderNode.addControl(new DeerControl(assetManager, deer));
        commanderNode.setLocalTranslation(0, 0, ShootDeerState.Z_FOREGROUND);
        commanderNode.attachChild(deer);
        return commanderNode;
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        getSpatial().move(new Vector3f(-deerSpeed * tpf, 0, 0));
        accrue += tpf;
        if (accrue > ACCRUE_THRESHOLD)
        {
            accrue = 0;
            imgHandle.setImage(assetManager, "Sprites/hopping_deer.png", true);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
    }
}
