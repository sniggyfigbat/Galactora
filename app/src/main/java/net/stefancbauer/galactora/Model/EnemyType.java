package net.stefancbauer.galactora.Model;

/**
 * Created by P13186907 on 23/02/2018.
 */

public enum EnemyType {
    DRONE, // Normal, boring, unexceptional. Low fire rate, OHK.
    WARRIOR, // Fires bursts of 5 bullets.
    GUARDIAN, // Projects a shield in front of it that reflects the player's bullets. When hit from behind, explodes with a large radius.
    QUEEN // Attempts to capture player's ship. spawns new drones if all are destroyed. 2x2 grid size. Three hits to kill.
}
