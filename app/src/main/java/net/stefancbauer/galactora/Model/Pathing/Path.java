package net.stefancbauer.galactora.Model.Pathing;

import net.stefancbauer.galactora.LocalMaths.Vector2f;

import java.util.Vector;

/**
 * Holds a series of Pathnodes forming a Path. Mostly just a vector and some extra methods for ease of use.
 */

public class Path {
    public boolean m_toBeDestroyed = false;
    public Vector<PathNode> nodes = new Vector<PathNode>();

    public Path() {}

    public void addNode(float x, float y) {
        if (nodes.size() == 0) { nodes.addElement(new PathNode(new Vector2f(x, y))); }
        else { nodes.addElement(new PathNode(new Vector2f(x, y), nodes.lastElement().coords)); }
    }

    public void addNode(float x, float y, boolean sync) {
        if (nodes.size() == 0) { nodes.addElement(new PathNode(new Vector2f(x, y), sync)); }
        else { nodes.addElement(new PathNode(new Vector2f(x, y), nodes.lastElement().coords, sync)); }
    }
}
