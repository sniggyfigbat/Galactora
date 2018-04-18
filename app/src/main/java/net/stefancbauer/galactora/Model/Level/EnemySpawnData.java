package net.stefancbauer.galactora.Model.Level;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.EnemyType;

/**
 * Contains sequencing data for a single enemy in a spawn wave.
 */

public class EnemySpawnData {
    //<enemy type="guardian" stack_depth="0.0" offset="1.0" grid_x="-1.5" grid_y="-3.5"/>
    public EnemyType m_type;
    public float m_stackDepth;
    public float m_offset;
    public Vector2f m_gridPos = new Vector2f();

    public EnemySpawnData(EnemyType inp_type, float inp_stackDepth, float inp_offset, float inp_gridX, float inp_gridY) //!< Full Constructor
    {
        m_type = inp_type;
        m_stackDepth = inp_stackDepth;
        m_offset = inp_offset;
        m_gridPos.set(inp_gridX, inp_gridY);
    }
}
