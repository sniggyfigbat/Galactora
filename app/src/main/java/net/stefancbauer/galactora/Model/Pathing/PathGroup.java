package net.stefancbauer.galactora.Model.Pathing;

import net.stefancbauer.galactora.Model.Enemy;

import java.util.Vector;

/**
 * Contains a bunch of parallel m_paths, each tied to an enemy. Set up so that multiple enemies may move in formation.
 */

public class PathGroup {
    public boolean m_toBeDestroyed = false;
    public Vector<Path> m_paths = new Vector<Path>();
    public Vector<Enemy> m_enemies = new Vector<Enemy>();
    public boolean m_hasBegun = false;

    public PathGroup() {}

    public void add(Path inp_path, Enemy inp_enemy) {
        boolean valid = false;
        if (m_paths.size() == 0) { valid = true; }
        else if (inp_path.nodes.size() == m_paths.get(0).nodes.size()) { valid = true; }

        if (valid) {
            m_paths.addElement(inp_path);
            m_enemies.addElement(inp_enemy);
        }
    }

    public void update() {
        // Clean up any deleted enemies.
        boolean hasChanged = false;

        for (int i = m_enemies.size() - 1; i >= 0; i--) {
            if (m_enemies.get(i).isToBeDestroyed() ||
                    m_paths.get(i).m_toBeDestroyed) {
                m_enemies.remove(i);
                m_paths.remove(i);
                hasChanged = true;
            }
        }

        if (hasChanged) { balancePaths(); }
        if (m_enemies.size() == 0) { m_toBeDestroyed = true; }

        // Start enemies along the path if they're all ready.
        if (!m_hasBegun) {
            boolean allReady = true;
            for (int i = 0; i < m_enemies.size(); i++) {
                if (!m_enemies.get(i).m_atStartPoint) {
                    allReady = false;
                }
            }
            if (allReady) {
                m_hasBegun = true;
                for (Enemy en : m_enemies) {
                    en.m_mayFollowPath = true;
                }
            }
        }
    }

    public boolean balancePaths() //!< Ensures that each section of path across all m_paths will take the same amount of time to traverse, allowing synchronised manoeuvres.
    {
        if (m_paths.size() == 0) {return false; }

        int size = m_paths.get(0).nodes.size();
        boolean sameSize = true;
        for (int i = 0; i < m_paths.size() && sameSize; i++) { sameSize = (size == m_paths.get(i).nodes.size()); }
        if (!sameSize) { return false; }

        // Finished checking that we have m_paths and that they're probably the same length.
        // Start at one, because the first point obviously doesn't work like that.
        for (int i = 1; i < size; i ++) {
            if (m_paths.get(0).nodes.get(i).sync) {
                // Get the longest traversal time of any parallel section.
                float longestTraversalTime = 0.0f;
                for (int j = 0; j < m_paths.size(); j++) {
                    if (m_paths.get(j).nodes.get(i).getTravelTime() > longestTraversalTime)
                    { longestTraversalTime = m_paths.get(j).nodes.get(i).getTravelTime(); }
                }

                // Make it so that all parallel sections take the same time.
                for (int j = 0; j < m_paths.size() && longestTraversalTime > 0.0f; j++) {
                    m_paths.get(j).nodes.get(i).setTravelTime(longestTraversalTime);
                }
            }
        }

        return true;
    }


}
