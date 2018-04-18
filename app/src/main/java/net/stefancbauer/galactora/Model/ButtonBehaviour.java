package net.stefancbauer.galactora.Model;

/**
 * Various behaviour types of buttons.
 */

public enum ButtonBehaviour {
    ONPRESS, // Button triggers immediately when pressed.
    ONRELEASE, // Button triggers when released.
    WHILEHELD, // Button constantly triggered while held.
    TOGGLE // Button toggles immediately when pressed.
}
