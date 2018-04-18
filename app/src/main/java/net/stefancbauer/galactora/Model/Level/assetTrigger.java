package net.stefancbauer.galactora.Model.Level;

/**
 * Used to sequence the triggering of asset spawns.
 */

public class assetTrigger {
    public int delay;
    public String command;

    public assetTrigger(int inp_delay, String inp_command) {
        delay = inp_delay;
        command = inp_command;
    }
}
