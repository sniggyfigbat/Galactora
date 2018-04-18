package net.stefancbauer.galactora.Model.Engine;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.GameObject;

import java.util.Vector;

/**
 * An object that physically connects two GameObjects, whose local origins are in the same place.
 */

public class Weld {
    public GameObject m_objA;
    public GameObject m_objB;
    public boolean m_toBeDestroyed = false;

    public static Vector<Weld> welds = new Vector<Weld>();

    private Weld(GameObject inp_objA, GameObject inp_objB) //!< Private constructor, called by weldObjects().
    {
        m_objA = inp_objA;
        m_objB = inp_objB;
    }

    public static void weldObjects(GameObject inp_objA, GameObject inp_objB) //!< Creates a weld between two GameObjects.
    {
        welds.addElement(new Weld(inp_objA, inp_objB));
        inp_objA.setIsWelded(true);
        inp_objB.setIsWelded(true);
    }

    public static Vector<GameObject> getWelded(GameObject inp_obj) //!< Returns a vector of all GameObjects DIRECTLY welded to the input.
    {
        Vector<GameObject> returnVector = new Vector<GameObject>();
        for (Weld weld: welds) {
            if (weld.m_objA == inp_obj && weld.m_objB != inp_obj) { returnVector.addElement(weld.m_objB); }
            if (weld.m_objB == inp_obj && weld.m_objA != inp_obj) { returnVector.addElement(weld.m_objA); }
        }
        return returnVector;
    }

    public static void chainUpdate(GameObject inp_obj, Vector<GameObject> exceptions, Vector2f position, float rotation) //!< Used to chain updates by calling tryUpdate() in all welds.
    {
        for (Weld weld: welds) { weld.tryUpdate(inp_obj, exceptions, position, rotation);}
    }

    public void tryUpdate(GameObject in_obj, Vector<GameObject> exceptions, Vector2f position, float rotation) //!< Tests to see if the object is one of this weld's, then updates the other in the weld with the inputted state.
    {
        if (in_obj == m_objA && !exceptions.contains(m_objB)) {
            m_objB.updateState(position, rotation, exceptions);
        }

        if (in_obj == m_objB && !exceptions.contains(m_objA)) {
            m_objA.updateState(position, rotation, exceptions);
        }
    }

    public static void chainDelete(GameObject inp_obj) //!< Used to chain deletion by calling tryDelete() in all welds.
    {
        for (Weld weld: welds) { weld.tryDelete(inp_obj);}
    }

    public void tryDelete(GameObject inp_obj) //!< Tests to see if the object is one of this weld's, then calls delete() in the other in the weld.
    {
        if (inp_obj == m_objA && !m_objB.isToBeDestroyed()) {
            m_objB.delete();
            m_toBeDestroyed = true;
        }

        if (inp_obj == m_objB && !m_objA.isToBeDestroyed()) {
            m_objA.delete();
            m_toBeDestroyed = true;
        }
    }

    public static void disconnectObject(GameObject in_obj) //!< Deletes all welds connecting to specified object, without affecting the GameObjects involved.
    {
        for (Weld weld: welds) {
            if (in_obj == weld.m_objA || in_obj == weld.m_objB) { weld.m_toBeDestroyed = true;}
        }
    }

    public static void disconnectObjects(GameObject inp_objA, GameObject inp_objB) //!< Deletes weld connecting two specified objects, without affecting the GameObjects involved.
    {
        for (Weld weld: welds) {
            if ((inp_objA == weld.m_objA && inp_objB == weld.m_objB) || (inp_objA == weld.m_objB && inp_objB == weld.m_objA)) { weld.m_toBeDestroyed = true;}
        }
    }

    public static void deleteWelds() //!< Cleans up all welds marked for deletion. Should be called before deleting the actual objects themselves.
    {
        Vector<GameObject> checkIfWelded = new Vector<GameObject>(); //!< Surviving GameObjects whose m_isWelded must be updated.

        for (int i = welds.size() - 1; i >= 0; i--) // Go from end to beginning to marginally reduce copy cost on deletion.
        {
            Weld tempHolder = welds.get(i);
            if (tempHolder.m_toBeDestroyed) {
                if (!tempHolder.m_objA.isToBeDestroyed()) { checkIfWelded.addElement(tempHolder.m_objA); }
                if (!tempHolder.m_objB.isToBeDestroyed()) { checkIfWelded.addElement(tempHolder.m_objB); }
                welds.remove(i);
            }
        }

        // Update all m_isWelded if necessary.
        if (checkIfWelded.size() > 0) {
            for (GameObject obj: checkIfWelded) {
                boolean stillWelded = false;
                for (Weld weld: welds) {
                    if (obj == weld.m_objA || obj == weld.m_objB) { stillWelded = true; }
                }
                obj.setIsWelded(stillWelded);
            }
        }
    }
}
