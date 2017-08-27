package mdmw.goldrock;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

import java.util.Stack;

public final class Utils
{
    private Utils()
    {
    }

    /**
     * Check if any parent of this spatial has the given control and return if it does. Otherwise return null.
     * This can be used for extracting DeerControls from deer picture geometries, for example.
     *
     * @param spatial      The spatial to check
     * @param controlClass The type of control to look for
     * @param <T>          The type of control to look for
     * @return The control class, or null
     */
    public static <T extends Control> T extractControl(Spatial spatial, Class<T> controlClass)
    {
        Spatial it = spatial;
        while (it != null)
        {
            T result = it.getControl(controlClass);
            if (result != null)
            {
                return result;
            }

            it = it.getParent();
        }

        return null;
    }


    /**
     * Walk down the spatial's tree and detach all controls. This ensures that controls unsubscribe from event
     * listeners and don't try to execute. It should be called directly before the spatial is removed from the scene
     * graph.
     * @param node A node that is about to be destroyed
     */
    public static void detachAllControls(Node node)
    {
        Stack<Spatial> spatials = new Stack<>();
        spatials.add(node);

        while (!spatials.empty())
        {
            Spatial next = spatials.pop();

            // Remove all controls
            while (next.getNumControls() > 0)
            {
                next.removeControl(next.getControl(0));
            }

            // If the spatial is a node and has children, add them to the stack
            if (next instanceof Node)
            {
                spatials.addAll(((Node) next).getChildren());
            }
        }
    }
}
