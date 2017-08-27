package mdmw.goldrock;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DeerLane
{
    private float verticalFraction;
    private Orientation direction;
    private List<Float> spawnTimes;
    private float currentTime;
    private float deerScale;

    public DeerLane(float verticalFraction, float deerScale, Orientation direction, Float... spawnTimes)
    {
        this.verticalFraction = verticalFraction;
        this.direction = direction;
        Arrays.sort(spawnTimes);
        this.spawnTimes = new LinkedList<>(Arrays.asList(spawnTimes));
        this.deerScale = deerScale;
    }

    public void update(float tpf)
    {
        currentTime += tpf;
    }

    public boolean shouldSpawn()
    {
        if (!spawnTimes.isEmpty() && spawnTimes.get(0) <= currentTime)
        {
            spawnTimes.remove(0);
            return true;
        }
        return false;
    }

    public boolean hasDeer()
    {
        return !spawnTimes.isEmpty();
    }

    public float getDeerScale()
    {
        return deerScale;
    }

    public float getVerticalOffset(float cameraHeight)
    {
        return verticalFraction * cameraHeight;
    }

    public boolean getFacingLeft()
    {
        return direction == Orientation.LEFT_FACING;
    }

    enum Orientation
    {
        LEFT_FACING, RIGHT_FACING
    }
}
