package net.stefancbauer.galactora.Model.Pathing;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Enemy;

/**
 * A simple data container used for pathing.
 */

public class PathNode {
    public Vector2f coords; //!< Coordinates of the node, in GU.
    public float length; //!< Distance between the last node and this one. If -1.0f, it means that this is the first node in its path.
    public boolean sync = true; //!< Whether different paths in a group should sync to this node.

    private float travelTime; //!< How long the enemy ship should take to reach the point from the last.
    private float travelSpeed; //!< How fast the enemy ship should move to reach the point in order to match the travelTime.

    public PathNode(Vector2f inp_coords) {
        coords = new Vector2f(inp_coords);
        length = -1.0f;
        setTravelTime(length / Enemy.moveSpeed);
    }

    public PathNode(Vector2f inp_coords, boolean inp_sync) {
        coords = new Vector2f(inp_coords);
        length = -1.0f;
        setTravelTime(length / Enemy.moveSpeed);
        sync = inp_sync;
    }

    public PathNode(Vector2f inp_coords, float inp_length) {
        coords = new Vector2f(inp_coords);
        length = inp_length;
        setTravelTime(length / Enemy.moveSpeed);
    }

    public PathNode(Vector2f inp_coords, Vector2f inp_lastCoords) {
        coords = new Vector2f(inp_coords);
        length = Vector2f.subtract(inp_coords, inp_lastCoords).getMagnitude();
        setTravelTime(length / Enemy.moveSpeed);
    }

    public PathNode(Vector2f inp_coords, Vector2f inp_lastCoords, boolean inp_sync) {
        coords = new Vector2f(inp_coords);
        length = Vector2f.subtract(inp_coords, inp_lastCoords).getMagnitude();
        setTravelTime(length / Enemy.moveSpeed);
        sync = inp_sync;
    }

    public float getTravelTime() { return travelTime; }
    public float getTravelSpeed() { return travelSpeed; }
    public void setTravelTime(float inp_newTravelTime) {
        travelTime = inp_newTravelTime;
        travelSpeed = length / travelTime;
    }
}
