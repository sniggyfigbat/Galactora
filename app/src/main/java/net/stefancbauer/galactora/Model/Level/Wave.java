package net.stefancbauer.galactora.Model.Level;

import net.stefancbauer.galactora.Model.EnemyType;
import net.stefancbauer.galactora.Model.Pathing.PathFactory;

import java.util.Vector;

/**
 * Contains sequencing data for a single spawn wave.
 */

public class Wave {
    public PathFactory.direction m_entryDirection;
    public int m_delay;

    public Vector<EnemySpawnData> m_spawns = new Vector<EnemySpawnData>();

    public Wave(PathFactory.direction inp_entryDirection, int inp_delay) //!< Full Constructor
    {
        m_entryDirection = inp_entryDirection;
        m_delay = inp_delay;
    }

    public void addEnemySpawn(EnemyType inp_type, float inp_stackDepth, float inp_offset, float inp_gridX, float inp_gridY) //!< A very slightly more convenient way of adding to m_spawns.
    {
        m_spawns.addElement(new EnemySpawnData(inp_type, inp_stackDepth, inp_offset, inp_gridX, inp_gridY));
    }
}
