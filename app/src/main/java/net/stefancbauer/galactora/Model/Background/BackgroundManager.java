package net.stefancbauer.galactora.Model.Background;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Pair;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Armour;
import net.stefancbauer.galactora.Model.Enemy;

import java.util.Random;
import java.util.Vector;

/**
 * Handles all sprites not related to gameplay, notably the background, but also, confusingly, non-gameplay foreground effects such as explosions.
 */

public class BackgroundManager {
    /*
    * All sprites here are being taken from "game_background_spritesheet"
    * For ease of reference, I've set out the Rects below.
    *
    * Intergalactic:
    * intergalacticVortex -> "game_background_spritesheet", new Rect(320, 0, 640, 320)
    * intergalacticGalaxy -> "game_background_spritesheet", new Rect(0, 640, 640, 1024)
    *
    * Interstellar
    * interstellarBlue -> "game_background_spritesheet", new Rect(512, 1088, 640, 1216)
    * interstellarViolet -> "game_background_spritesheet", new Rect(384, 1088, 512, 1216)
    * interstellarMauve -> "game_background_spritesheet", new Rect(512, 1216, 640, 1344)
    * interstellarRed -> "game_background_spritesheet", new Rect(384, 1216, 512, 1344)
    *
    * Planets:
    * planetaryMoP -> "game_background_spritesheet", new Rect(0, 0, 320, 320)
    * planetaryViolet -> "game_background_spritesheet", new Rect(0, 320, 320, 640)
    * planetaryMagenta -> "game_background_spritesheet", new Rect(320, 320, 640, 640)
    *
    * Debris:
    * debrisRed0 -> "game_background_spritesheet", new Rect(0, 1024, 64, 1088)
    * debrisRed1 -> "game_background_spritesheet", new Rect(0, 1088, 64, 1152)
    * debrisRed2 -> "game_background_spritesheet", new Rect(0, 1152, 64, 1216)
    * debrisRed3 -> "game_background_spritesheet", new Rect(0, 1216, 64, 1280)
    * debrisRed4 -> "game_background_spritesheet", new Rect(0, 1280, 64, 1344)
    * debrisRed5 -> "game_background_spritesheet", new Rect(64, 1024, 128, 1088)
    * debrisRed6 -> "game_background_spritesheet", new Rect(64, 1088, 128, 1152)
    * debrisRed7 -> "game_background_spritesheet", new Rect(64, 1152, 128, 1216)
    * debrisRed8 -> "game_background_spritesheet", new Rect(64, 1216, 128, 1280)
    * debrisRed9 -> "game_background_spritesheet", new Rect(64, 1280, 128, 1344)
    * debrisPink0 -> "game_background_spritesheet", new Rect(128, 1024, 192, 1088)
    * debrisPink1 -> "game_background_spritesheet", new Rect(128, 1088, 192, 1152)
    * debrisPink2 -> "game_background_spritesheet", new Rect(128, 1152, 192, 1216)
    * debrisPink3 -> "game_background_spritesheet", new Rect(128, 1216, 192, 1280)
    * debrisPink4 -> "game_background_spritesheet", new Rect(128, 1280, 192, 1344)
    * debrisPink5 -> "game_background_spritesheet", new Rect(192, 1024, 256, 1088)
    * debrisPink6 -> "game_background_spritesheet", new Rect(192, 1088, 256, 1152)
    * debrisPink7 -> "game_background_spritesheet", new Rect(192, 1152, 256, 1216)
    * debrisPink8 -> "game_background_spritesheet", new Rect(192, 1216, 256, 1280)
    * debrisPink9 -> "game_background_spritesheet", new Rect(192, 1280, 256, 1344)
    * debrisViolet0 -> "game_background_spritesheet", new Rect(256, 1024, 320, 1088)
    * debrisViolet1 -> "game_background_spritesheet", new Rect(256, 1088, 320, 1152)
    * debrisViolet2 -> "game_background_spritesheet", new Rect(256, 1152, 320, 1216)
    * debrisViolet3 -> "game_background_spritesheet", new Rect(256, 1216, 320, 1280)
    * debrisViolet4 -> "game_background_spritesheet", new Rect(256, 1280, 320, 1344)
    * debrisViolet5 -> "game_background_spritesheet", new Rect(320, 1024, 384, 1088)
    * debrisViolet6 -> "game_background_spritesheet", new Rect(320, 1088, 384, 1152)
    * debrisViolet7 -> "game_background_spritesheet", new Rect(320, 1152, 384, 1216)
    * debrisViolet8 -> "game_background_spritesheet", new Rect(320, 1216, 384, 1280)
    * debrisViolet9 -> "game_background_spritesheet", new Rect(320, 1280, 384, 1344)
    */

    final Random m_rand;
    final Context p_context;

    // BackgroundObject Vectors
    private Vector<BackgroundObject> mv_intergalactic = new Vector<BackgroundObject>(); //!< All current intergalactic-type background objects.
    private Vector<BackgroundObject> mv_interstellar = new Vector<BackgroundObject>(); //!< All current interstellar-type background objects.
    private Vector<BackgroundObject> mv_planetary = new Vector<BackgroundObject>(); //!< All current planetary-type background objects.
    private Vector<BackgroundObject> mv_debris = new Vector<BackgroundObject>(); //!< All current debris-type background objects.
    private Vector<BackgroundObject> mv_foreground = new Vector<BackgroundObject>(); //!< All current foreground objects, such as explosions, etc.


    // ConstellationFactory Vectors
    private Vector<ConstellationFactory> mv_intergalacticFactories = new Vector<ConstellationFactory>(); //!< The current ConstellationFactories making intergalactic-type background assets.
    private Vector<ConstellationFactory> mv_interstellarFactories = new Vector<ConstellationFactory>(); //!< The current ConstellationFactories making interstellar-type background assets.
    private Vector<ConstellationFactory> mv_planetaryFactories = new Vector<ConstellationFactory>(); //!< The current ConstellationFactories making planetary-type background assets.
    private Vector<ConstellationFactory> mv_debrisFactories = new Vector<ConstellationFactory>(); //!< The current ConstellationFactories making debris-type background assets.

    private final Pair<Float, Float> intergalacticRange = new Pair<Float, Float>(200.0f, 1200.0f); //!< MAGIC: Used to determine weighted random length between intergalactic ConstellationFactory spawns.
    private final float intergalacticRangeCentre = 800.0f; //!< MAGIC: Used to determine weighted random length between intergalactic ConstellationFactory spawns.
    private int ticksToIntergalactic; //!< How many update ticks until another intergalactic ConstellationFactory should be spawned.

    private final Pair<Float, Float> interstellarRange = new Pair<Float, Float>(75.0f, 200.0f); //!< MAGIC: Used to determine weighted random length between interstellar ConstellationFactory spawns.
    private final float interstellarRangeCentre = 125.0f; //!< MAGIC: Used to determine weighted random length between interstellar ConstellationFactory spawns.
    private int ticksToInterstellar; //!< How many update ticks until another interstellar ConstellationFactory should be spawned.

    private final Pair<Float, Float> planetaryRange = new Pair<Float, Float>(200.0f, 800.0f); //!< MAGIC: Used to determine weighted random length between planetary ConstellationFactory spawns.
    private final float planetaryRangeCentre = 500.0f; //!< MAGIC: Used to determine weighted random length between planetary ConstellationFactory spawns.
    private int ticksToPlanetary; //!< How many update ticks until another planetary ConstellationFactory should be spawned.

    private final Pair<Float, Float> debrisRange = new Pair<Float, Float>(50.0f, 400.0f); //!< MAGIC: Used to determine weighted random length between debris ConstellationFactory spawns.
    private final float debrisRangeCentre = 150.0f; //!< MAGIC: Used to determine weighted random length between debris ConstellationFactory spawns.
    private int ticksToDebris; //!< How many update ticks until another debris ConstellationFactory should be spawned.

    public BackgroundManager(Context context, Random rand) {
        m_rand = rand;
        p_context = context;

        ticksToIntergalactic = m_rand.nextInt(100);
        ticksToInterstellar = m_rand.nextInt(100);
        ticksToPlanetary = m_rand.nextInt(100);
        ticksToDebris = m_rand.nextInt(100);
    }

    public void update() {
        ticksToIntergalactic--;
        ticksToInterstellar--;
        ticksToPlanetary--;
        ticksToDebris--;

        // Update everything.
        for (BackgroundObject obj: mv_intergalactic) { obj.update(); }
        for (BackgroundObject obj: mv_interstellar) { obj.update(); }
        for (BackgroundObject obj: mv_planetary) { obj.update(); }
        for (BackgroundObject obj: mv_debris) { obj.update(); }
        for (BackgroundObject obj: mv_foreground) { obj.update(); }

        for (ConstellationFactory fact: mv_intergalacticFactories) { fact.update(mv_intergalactic); }
        for (ConstellationFactory fact: mv_interstellarFactories) { fact.update(mv_interstellar); }
        for (ConstellationFactory fact: mv_planetaryFactories) { fact.update(mv_planetary); }
        for (ConstellationFactory fact: mv_debrisFactories) { fact.update(mv_debris); }

        // Spawn new constellations, if appropriate.

        if (ticksToIntergalactic <= 0) {
            mv_intergalacticFactories.addElement(ConstellationFactory.getIntergalacticFactory(m_rand, mv_intergalactic, ""));
            ticksToIntergalactic = (int)getWeightedValue(intergalacticRange, intergalacticRangeCentre, 0.5f);
        }

        if (ticksToInterstellar <= 0) {
            mv_interstellarFactories.addElement(ConstellationFactory.getInterstellarFactory(m_rand, mv_interstellar, ""));
            ticksToInterstellar = (int)getWeightedValue(interstellarRange, interstellarRangeCentre, 0.5f);
        }

        if (ticksToPlanetary <= 0) {
            mv_planetaryFactories.addElement(ConstellationFactory.getPlanetaryFactory(m_rand, mv_planetary, ""));
            ticksToPlanetary = (int)getWeightedValue(planetaryRange, planetaryRangeCentre, 0.5f);
        }

        if (ticksToDebris <= 0) {
            mv_debrisFactories.addElement(ConstellationFactory.getDebrisFactory(m_rand, mv_debris, ""));
            ticksToDebris = (int)getWeightedValue(debrisRange, debrisRangeCentre, 0.5f);
        }


        // Delete stuff with deletion flags.
        for (int i = mv_intergalactic.size() - 1; i >= 0; i--) {
            if (mv_intergalactic.get(i).m_toBeDestroyed) { mv_intergalactic.remove(i); }
        }
        for (int i = mv_interstellar.size() - 1; i >= 0; i--) {
            if (mv_interstellar.get(i).m_toBeDestroyed) { mv_interstellar.remove(i); }
        }
        for (int i = mv_planetary.size() - 1; i >= 0; i--) {
            if (mv_planetary.get(i).m_toBeDestroyed) { mv_planetary.remove(i); }
        }
        for (int i = mv_debris.size() - 1; i >= 0; i--) {
            if (mv_debris.get(i).m_toBeDestroyed) { mv_debris.remove(i); }
        }
        for (int i = mv_foreground.size() - 1; i >= 0; i--) {
            if (mv_foreground.get(i).m_toBeDestroyed) { mv_foreground.remove(i); }
        }
    }

    public void drawBackground(Canvas c, Paint p) {
        for (BackgroundObject obj: mv_intergalactic) { obj.draw(c, p); }
        for (BackgroundObject obj: mv_interstellar) { obj.draw(c, p); }
        for (BackgroundObject obj: mv_planetary) { obj.draw(c, p); }
        for (BackgroundObject obj: mv_debris) { obj.draw(c, p); }
    }

    public void drawForeground(Canvas c, Paint p) {
        for (BackgroundObject obj: mv_foreground) { obj.draw(c, p); }
    }

    private float getWeightedValue(Pair<Float, Float> range, float centre, float weightingFactor) //!< Used to get a value from a range with weighted randomness. Weighting factor determines the split between fully random (0.0f) and bell-curve random (1.0f), recommend 0.5f.
    {
        float pointOnDistribution = m_rand.nextFloat();
        pointOnDistribution = ((1.0f - weightingFactor) * pointOnDistribution) + (weightingFactor * (pointOnDistribution * pointOnDistribution));
        float rangeOnSide;
        if (m_rand.nextBoolean()) { rangeOnSide = range.first - centre; }
        else { rangeOnSide = range.second - centre; }
        return ((pointOnDistribution * rangeOnSide) + centre);
    }

    public void addEffect(String effectType, Vector2f position) //!< Adds effect to foreground.
    {
        BackgroundObject.getEffect(m_rand, mv_foreground, effectType, position);
    }

    public void addEnemyGibs(Enemy enemy) //!< Adds enemy gibs to debris layer.
    {
        BackgroundObject.getEnemyGibs(m_rand, mv_debris, enemy);
    }

    public void addArmourGibs(Armour armour) //!< Adds armour gibs to debris layer.
    {
        BackgroundObject.getArmourGibs(m_rand, mv_debris, armour);
    }

    public void addLevelEffect(String command, String parameters) //!< Adds level effect to debris layer.
    {
        if (command.equalsIgnoreCase("add_score")) { BackgroundObject.getLevelEffects(p_context, m_rand, mv_interstellar, command, parameters); }
        else if (command.equalsIgnoreCase("subtract_score")) { BackgroundObject.getLevelEffects(p_context, m_rand, mv_interstellar, command, parameters); }
        else { BackgroundObject.getLevelEffects(p_context, m_rand, mv_debris, command, parameters); }
    }
}
