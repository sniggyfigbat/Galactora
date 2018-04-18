package net.stefancbauer.galactora.Model.Level;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Pathing.PathFactory;

/**
 * Used to sequence flyby pathing.
 */

public class FlybyTemplate {
    public float weight;
    public PathFactory.flybyType type;
    public Vector2f range = new Vector2f();

    public FlybyTemplate(PathFactory.flybyType inp_type, float inp_weight, float inp_x, float inp_y) {
        weight = inp_weight;
        type = inp_type;
        range.set(inp_x, inp_y);
    }
}
