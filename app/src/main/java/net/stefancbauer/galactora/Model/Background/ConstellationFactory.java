package net.stefancbauer.galactora.Model.Background;

import android.graphics.Rect;
import android.util.Pair;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.LocalMaths.Vector2i;
import net.stefancbauer.galactora.View.GameSurfaceView;

import java.util.Random;
import java.util.Vector;

/**
 * Spawns a full constellation of background objects in a customisable-but-random way.
 */

public class ConstellationFactory {
    private Random m_rand; //!< Random generator used by the constellation.

    public boolean m_toBeDestroyed = false; //!< Flag for whether object has run its course and should be destroyed.
    private final Vector<Pair<BackgroundSpawnOption, Integer>> m_options; //!< Factories for spawn possibilities. Int determines spawn weight of each option.
    private final int m_totalWeight; //!< The total combined weights of all options.
    private final float m_spawnY; //!< Y-Value at which BackgroundObjects are spawned.

    private final float m_yVelocity; //!< Base m_velocity of spawned BackgroundObjects on the y axis, in game-units per tick.
    private final float m_yVelocityVar; //!< Amount by which m_yVelocity mar vary. Eg, if m_yVelocity is 1.0 and m_yVelocityVar is 0.3, m_yVelocity may be anything between 0.7 and 1.3.

    private final float m_xRange; //!< The size of the range in which this constellation will spawn BackgroundObjects.
    private float m_xRangeCentre; //!< The centre of the range in which this constellation will spawn BackgroundObjects.
    private float m_xRangeDriftVel; //!< The rate at which the x range drifts, in GU per tick.

    private final Vector<BackgroundObject> m_entries; //!< All previously-spawned entries. Used for getting distances to them.
    private final Vector<Pair<Float, Float>> m_spawnChanceCurve; //!< Determines the probability of spawning at any given distance from another constellation entry. First in pair is distance from another entry, second is spawn-chance at that distance. Chance linearly-interpolates between points. First point should always be zero probability, last should always be 1 (100%) probability.
    private final Vector<Pair<Float, Float>> m_constellationEnd; //!< Determines the probability of finishing the constellation at any given size. First in pair is number of entries, second is end-chance at that size. Chance linearly-interpolates between points. First point should always be zero probability, last should always be 1 (100%) probability.

    public ConstellationFactory(Vector<BackgroundObject> drawContainer, Vector<Pair<BackgroundSpawnOption, Integer>> options, float spawnY, float yVelocity, float yVelocityVar, float xRange, float xRangeDriftVel, Vector<Pair<Float, Float>> spawnChanceCurve, Vector<Pair<Float, Float>> constellationEnd) //!< Full Constructor
    {
        m_rand = new Random();

        m_options = new Vector<Pair<BackgroundSpawnOption, Integer>>(options);
        int temp = 0;
        for (Pair<BackgroundSpawnOption, Integer> entry: m_options) { temp += entry.second; }
        m_totalWeight = temp;
        m_spawnY = spawnY;

        m_yVelocity = yVelocity;
        m_yVelocityVar = yVelocityVar;

        m_xRange = xRange;
        m_xRangeCentre = (m_rand.nextFloat() * 14.0f) - 7.0f;
        m_xRangeDriftVel = xRangeDriftVel;

        m_entries = new Vector<BackgroundObject>();
        m_spawnChanceCurve = new Vector<Pair<Float, Float>>(spawnChanceCurve);
        m_constellationEnd = new Vector<Pair<Float, Float>>(constellationEnd);

        Vector2f spawnPos = new Vector2f(m_xRangeCentre, m_spawnY);
        spawn(drawContainer, spawnPos);
    }

    public void update(Vector<BackgroundObject> drawContainer) //!< Checks whether it's going to spawn anything, and, if it is, spawns it and puts it in the drawContainer.
    {
        // Update m_xRange.
        m_xRangeCentre += m_xRangeDriftVel;

        // Gets a random float between 0 and 1, partially weighted towards being lower.
        float randomDist = m_rand.nextFloat();
        randomDist = (0.5f * randomDist) + (0.5f * (randomDist * randomDist));
        // Decides whether it'll be positive or negative.
        if (m_rand.nextBoolean()) { randomDist *= -1.0f; }
        // Multiplies it by the half-range to get the relative x value, then shifts it to the range centre.
        float spawnX = (randomDist * m_xRange * 0.5f) + m_xRangeCentre;

        Vector2f spawnPos = new Vector2f(spawnX, m_spawnY);
        if (!m_toBeDestroyed && testX(spawnPos)) { spawn(drawContainer, spawnPos); }
    }

    private boolean testX(Vector2f spawnPos) //!< Decides whether to spawn a Background object at a given X, taking range, distance, probability, etc into account.
    {
        // Start by finding the distance from the closest object in the constellation.
        float minDistSquared = 1000000.0f;
        for (BackgroundObject obj: m_entries) {
            Vector2f tempV = Vector2f.subtract(obj.getPosition(), spawnPos);
            float temp = tempV.getMagnitudeSquared();
            if (temp < minDistSquared) { minDistSquared = temp; }
        }
        float minDist = (float)Math.sqrt(minDistSquared);

        // Now that we've found the closest distance, it's time to work out the spawning probability.
        // Note: this relies on m_spawnChanceCurve having been set up correctly. If it isn't, you're hosed.
        if (minDist > m_spawnChanceCurve.lastElement().first) {
            // The last element of the spawn chance curve should always be the distance above which spawn-chance is 100%.
            // Therefore, since the minDist is greater than that, no probability testing is required as this is a 100% spawn.
            return true;
        } else if (minDist < m_spawnChanceCurve.get(0).first) {
            // The first element of the spawn chance curve should always be the minimum safe distance, below which spawn-chance is 0%.
            // Therefore, since the minDist is smaller than that, no probability testing is required as this m_position cannot be spawned at.
            return false;
        } else {
            // Full probability testing required.
            int endIndex = m_spawnChanceCurve.size() - 1;
            boolean foundEndIndex = false;
            for (int i = 1; !foundEndIndex && i < m_spawnChanceCurve.size(); i++) {
                if (minDist <= m_spawnChanceCurve.get(i).first) {
                    // The distance is between the previous entry and this one.
                    endIndex = i;
                    foundEndIndex = true;
                }
            }

            // Interpolate the values to get the spawnChance
            float interpolDist = (minDist - m_spawnChanceCurve.get(endIndex - 1).first) / (m_spawnChanceCurve.get(endIndex).first - m_spawnChanceCurve.get(endIndex - 1).first);
            float spawnChance = (interpolDist * (m_spawnChanceCurve.get(endIndex).second - m_spawnChanceCurve.get(endIndex - 1).second)) + m_spawnChanceCurve.get(endIndex - 1).second;

            // Test against that probability! Finally!
            return (m_rand.nextFloat() < spawnChance);
        }
    }

    private void spawn(Vector<BackgroundObject> drawContainer, Vector2f spawnPos) //!< Spawns a BackgroundObject at the specified coordinate (GU).
    {
        float spawnVelocity = m_yVelocity + (m_rand.nextFloat() * 2.0f * m_yVelocityVar) - m_yVelocityVar;

        int selector = m_rand.nextInt(m_totalWeight);
        boolean gotSpawn = false;

        for (int i = 0; !gotSpawn && i < m_options.size(); i++) {
            int optionWeight = m_options.get(i).second.intValue();
            if (selector < optionWeight) {
                // Spawns a BackgroundObject
                BackgroundObject spawn = m_options.get(i).first.getInstance(m_rand, spawnPos, spawnVelocity);
                m_entries.addElement(spawn);
                drawContainer.addElement(spawn);
                gotSpawn = true;

                testConstellationEnd();
            } else {
                selector -= optionWeight;
            }
        }
    }

    private void testConstellationEnd() //!< Decides whether to end the constellation, based on the number of entries created and m_constellationEnd.
    {
        float entries = m_entries.size();

        // Time to work out the ending probability.
        // Note: this relies on m_constellationEnd having been set up correctly. If it isn't, you're hosed.
        if (entries > m_constellationEnd.lastElement().first) {
            // The last element of the end chance curve should always be the maximum size above which end-chance is 100%.
            // Therefore, since entries is greater than that, no probability testing is required as this is a 100% end.
            m_toBeDestroyed = true;
        } else if (entries < m_constellationEnd.get(0).first) {
            // The first element of the end chance curve should always be the minimum size below which end-chance is 0%.
            // Therefore, since entries is smaller than that, no probability testing is required as the constellation cannot end here.
            m_toBeDestroyed = false;
        } else {
            // Full probability testing required.
            int endIndex = m_constellationEnd.size() - 1;
            boolean foundEndIndex = false;
            for (int i = 1; !foundEndIndex && i < m_constellationEnd.size(); i++) {
                if (entries <= m_constellationEnd.get(i).first) {
                    // The distance is between the previous entry and this one.
                    endIndex = i;
                    foundEndIndex = true;
                }
            }

            // Interpolate the values to get the spawnChance
            float interpolDist = (entries - m_constellationEnd.get(endIndex - 1).first) / (m_constellationEnd.get(endIndex).first - m_constellationEnd.get(endIndex - 1).first);
            float endChance = (interpolDist * (m_constellationEnd.get(endIndex).second - m_constellationEnd.get(endIndex - 1).second)) + m_constellationEnd.get(endIndex - 1).second;

            // Test against that probability! Finally!
            m_toBeDestroyed = (m_rand.nextFloat() < endChance);
        }
    }

    static ConstellationFactory getIntergalacticFactory(Random rand, Vector<BackgroundObject> drawContainer, String specialCommand) //!< A factory function to get a certain type of factory. Meta, but useful.
    {
        // ConstellationFactory(Vector<BackgroundObject> drawContainer, Vector<Pair<BackgroundSpawnOption, Integer>> options, float spawnY, float yVelocity, float yVelocityVar, float xRange, float xRangeDriftVel, Vector<Pair<Float, Float>> spawnChanceCurve, Vector<Pair<Float, Float>> constellationEnd) //!< Full Constructor
        // BackgroundSpawnOption(String baseTextureName, Rect baseTextureRect, Vector2f size, Vector2f origin, float sizeScalarVar, float rotation, float rotationVar, float rotationVel, float rotationVelVar) //!< Full Constructor


        // Set up normal SpawnOption Vectors, which can be edited if necessary.
        Vector<Pair<BackgroundSpawnOption, Integer>> options = new Vector<Pair<BackgroundSpawnOption, Integer>>();
        /* Normal Galaxy    */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(0, 640, 640, 1024), new Vector2f(7.0f, 4.2f), new Vector2f(3.5f, 2.1f), 0.5f, 0.0f, 180.0f), 18));
        /* Small Wormhole   */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(320, 0, 640, 320), new Vector2f(4.0f), new Vector2f(2.0f), 0.5f, 0.0f, 180.0f, 0.4f, 0.3f), 1));
        /* Large Wormhole   */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(320, 0, 640, 320), new Vector2f(8.0f), new Vector2f(4.0f), 0.5f, 0.0f, 180.0f, 0.4f, 0.3f), 1));

        float spawnY = GameSurfaceView.DPtoGU(new Vector2i(0, 0)).y + 15.0f;

        if (!(specialCommand.equals("normal") ||
                specialCommand.equals("supergalaxy"))) {
            // No override entered, pick one of the options, as weighted.
            int choice = rand.nextInt(20);

            if (choice < 19) { specialCommand = "normal"; }
            else { specialCommand = "supergalaxy"; }
        }

        ConstellationFactory returnConstellation;

        if (specialCommand.equals("normal")) {
            // Build a normal intergalactic constellation. Will rarely have more than one, very rarely may have as many as three.

            Vector<Pair<Float, Float>> spawnChanceCurve = new Vector<Pair<Float, Float>>();
            spawnChanceCurve.addElement(new Pair<Float, Float>(6.0f, 0.0f));
            spawnChanceCurve.addElement(new Pair<Float, Float>(10.0f, 1.0f));

            Vector<Pair<Float, Float>> constellationEnd = new Vector<Pair<Float, Float>>();
            constellationEnd.addElement(new Pair<Float, Float>(1.0f, 0.9f));
            constellationEnd.addElement(new Pair<Float, Float>(3.0f, 1.0f));

            float xRangeDriftVel = (rand.nextFloat() * 0.1f) - 0.05f;

            returnConstellation = new ConstellationFactory(drawContainer, options, spawnY, -0.02f, 0.0f, 4.0f, xRangeDriftVel, spawnChanceCurve, constellationEnd);
        } else { //if (specialCommand.equals("supergalaxy")) {
            // Build a supergalaxy, with loads of intergalactics.

            /* Tiny Galaxy    */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(0, 640, 640, 1024), new Vector2f(3.5f, 2.1f), new Vector2f(1.75f, 1.05f), 0.5f, 0.0f, 180.0f), 30));


            Vector<Pair<Float, Float>> spawnChanceCurve = new Vector<Pair<Float, Float>>();
            spawnChanceCurve.addElement(new Pair<Float, Float>(3.0f, 0.0f));
            spawnChanceCurve.addElement(new Pair<Float, Float>(5.0f, 0.5f));
            spawnChanceCurve.addElement(new Pair<Float, Float>(8.0f, 1.0f));

            Vector<Pair<Float, Float>> constellationEnd = new Vector<Pair<Float, Float>>();
            constellationEnd.addElement(new Pair<Float, Float>(2.0f, 0.0f));
            constellationEnd.addElement(new Pair<Float, Float>(5.0f, 0.5f));
            constellationEnd.addElement(new Pair<Float, Float>(10.0f, 1.0f));

            float xRangeDriftVel = (rand.nextFloat() * 0.1f) - 0.05f;
            //float xRangeDriftVel = 0.0f;

            returnConstellation = new ConstellationFactory(drawContainer, options, spawnY, -0.02f, 0.0f, 8.0f, xRangeDriftVel, spawnChanceCurve, constellationEnd);
        }

        return returnConstellation;
    }

    static ConstellationFactory getInterstellarFactory(Random rand, Vector<BackgroundObject> drawContainer, String specialCommand) //!< A factory function to get a certain type of factory. Meta, but useful.
    {
        // ConstellationFactory(Vector<BackgroundObject> drawContainer, Vector<Pair<BackgroundSpawnOption, Integer>> options, float spawnY, float yVelocity, float yVelocityVar, float xRange, float xRangeDriftVel, Vector<Pair<Float, Float>> spawnChanceCurve, Vector<Pair<Float, Float>> constellationEnd) //!< Full Constructor
        // BackgroundSpawnOption(String baseTextureName, Rect baseTextureRect, Vector2f size, Vector2f origin, float sizeScalarVar, float rotation, float rotationVar, float rotationVel, float rotationVelVar) //!< Full Constructor


        // Set up normal SpawnOption Vectors, which can be edited if necessary.
        Vector<Pair<BackgroundSpawnOption, Integer>> options = new Vector<Pair<BackgroundSpawnOption, Integer>>();
        /* Blue Star    */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(512, 1088, 640, 1216), new Vector2f(1.25f), 0.5f), 1));
        /* Violet Star  */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(384, 1088, 512, 1216), new Vector2f(1.0f), 0.5f), 5));
        /* Mauve Star   */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(512, 1216, 640, 1344), new Vector2f(0.8f), 0.5f), 5));
        /* Red Star     */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(384, 1216, 512, 1344), new Vector2f(0.6f), 0.5f), 5));

        Vector<Pair<Float, Float>> spawnChanceCurve = new Vector<Pair<Float, Float>>();
        spawnChanceCurve.addElement(new Pair<Float, Float>(1.0f, 0.2f));
        spawnChanceCurve.addElement(new Pair<Float, Float>(5.0f, 1.0f));

        float spawnY = GameSurfaceView.DPtoGU(new Vector2i(0, 0)).y + 15.0f;
        float yVel = -0.025f + (rand.nextFloat() * 0.01f) - 0.005f; // Stars i a single constellation all move at the same speed, but different constellations may have slightly different speeds.

        if (!(specialCommand.equals("small") ||
                specialCommand.equals("medium") ||
                specialCommand.equals("large") ||
                specialCommand.equals("huge"))) {
            // No override entered, pick one of the options, as weighted.
            int choice = rand.nextInt(8);

            if (choice < 3) { specialCommand = "small"; }
            if (choice < 6) { specialCommand = "medium"; }
            if (choice < 7) { specialCommand = "large"; }
            else { specialCommand = "huge"; }
        }

        ConstellationFactory returnConstellation;

        if (specialCommand.equals("small")) {
            // Build a small intestellar constellation. 3-8 stars.

            Vector<Pair<Float, Float>> constellationEnd = new Vector<Pair<Float, Float>>();
            constellationEnd.addElement(new Pair<Float, Float>(3.0f, 0.1f));
            constellationEnd.addElement(new Pair<Float, Float>(5.0f, 0.4f));
            constellationEnd.addElement(new Pair<Float, Float>(8.0f, 1.0f));

            float xRangeDriftVel = (rand.nextFloat() * 0.1f) - 0.05f;

            returnConstellation = new ConstellationFactory(drawContainer, options, spawnY, yVel, 0.0f, 3.0f, xRangeDriftVel, spawnChanceCurve, constellationEnd);
        } else if (specialCommand.equals("medium")) {
            // Build a medium intestellar constellation. 5-12 stars.

            Vector<Pair<Float, Float>> constellationEnd = new Vector<Pair<Float, Float>>();
            constellationEnd.addElement(new Pair<Float, Float>(5.0f, 0.1f));
            constellationEnd.addElement(new Pair<Float, Float>(9.0f, 0.4f));
            constellationEnd.addElement(new Pair<Float, Float>(12.0f, 1.0f));

            float xRangeDriftVel = (rand.nextFloat() * 0.1f) - 0.05f;

            returnConstellation = new ConstellationFactory(drawContainer, options, spawnY, yVel, 0.0f, 4.0f, xRangeDriftVel, spawnChanceCurve, constellationEnd);
        } else if (specialCommand.equals("large")) {
            // Build a large intestellar constellation. 10-15 stars.

            Vector<Pair<Float, Float>> constellationEnd = new Vector<Pair<Float, Float>>();
            constellationEnd.addElement(new Pair<Float, Float>(10.0f, 0.05f));
            constellationEnd.addElement(new Pair<Float, Float>(15.0f, 1.0f));

            float xRangeDriftVel = (rand.nextFloat() * 0.1f) - 0.05f;

            returnConstellation = new ConstellationFactory(drawContainer, options, spawnY, yVel, 0.0f, 5.0f, xRangeDriftVel, spawnChanceCurve, constellationEnd);
        } else { //if (specialCommand.equals("huge")) {
            // Build a huge intestellar constellation. 15-25 stars.

            Vector<Pair<Float, Float>> constellationEnd = new Vector<Pair<Float, Float>>();
            constellationEnd.addElement(new Pair<Float, Float>(15.0f, 0.05f));
            constellationEnd.addElement(new Pair<Float, Float>(20.0f, 0.3f));
            constellationEnd.addElement(new Pair<Float, Float>(25.0f, 1.0f));

            float xRangeDriftVel = (rand.nextFloat() * 0.1f) - 0.05f;

            returnConstellation = new ConstellationFactory(drawContainer, options, spawnY, yVel, 0.0f, 6.0f, xRangeDriftVel, spawnChanceCurve, constellationEnd);
        }

        return returnConstellation;
    }

    static ConstellationFactory getPlanetaryFactory(Random rand, Vector<BackgroundObject> drawContainer, String specialCommand) //!< A factory function to get a certain type of factory. Meta, but useful.
    {
        // ConstellationFactory(Vector<BackgroundObject> drawContainer, Vector<Pair<BackgroundSpawnOption, Integer>> options, float spawnY, float yVelocity, float yVelocityVar, float xRange, float xRangeDriftVel, Vector<Pair<Float, Float>> spawnChanceCurve, Vector<Pair<Float, Float>> constellationEnd) //!< Full Constructor
        // BackgroundSpawnOption(String baseTextureName, Rect baseTextureRect, Vector2f size, Vector2f origin, float sizeScalarVar, float rotation, float rotationVar, float rotationVel, float rotationVelVar) //!< Full Constructor


        // Set up normal SpawnOption Vectors, which can be edited if necessary.
        Vector<Pair<BackgroundSpawnOption, Integer>> options = new Vector<Pair<BackgroundSpawnOption, Integer>>();
        /* MoP Planet       */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(0, 0, 320, 320), new Vector2f(4.0f), 0.5f), 1));
        /* Violet Planet    */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(0, 320, 320, 640), new Vector2f(4.0f), 0.5f), 1));
        /* Mauve Planet     */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(320, 320, 640, 640), new Vector2f(4.0f), 0.5f), 1));

        Vector<Pair<Float, Float>> spawnChanceCurve = new Vector<Pair<Float, Float>>();
        spawnChanceCurve.addElement(new Pair<Float, Float>(4.0f, 0.0f));
        spawnChanceCurve.addElement(new Pair<Float, Float>(10.0f, 1.0f));

        float spawnY = GameSurfaceView.DPtoGU(new Vector2i(0, 0)).y + 15.0f;

        if (!(specialCommand.equals("single") ||
                specialCommand.equals("cluster"))) {
            // No override entered, pick one of the options, as weighted.
            int choice = rand.nextInt(3);

            if (choice < 2) { specialCommand = "single"; }
            else { specialCommand = "cluster"; }
        }

        ConstellationFactory returnConstellation;

        if (specialCommand.equals("single")) {
            // Build a single planet.

            Vector<Pair<Float, Float>> constellationEnd = new Vector<Pair<Float, Float>>();
            constellationEnd.addElement(new Pair<Float, Float>(0.9f, 0.1f));
            constellationEnd.addElement(new Pair<Float, Float>(1.0f, 1.0f));

            float xRangeDriftVel = (rand.nextFloat() * 0.1f) - 0.05f;

            returnConstellation = new ConstellationFactory(drawContainer, options, spawnY, -0.0325f, 0.0025f, 4.0f, xRangeDriftVel, spawnChanceCurve, constellationEnd);
        } else { //if (specialCommand.equals("cluster")) {
            // Build a cluster of planets. 2 to 5.

            Vector<Pair<Float, Float>> constellationEnd = new Vector<Pair<Float, Float>>();
            constellationEnd.addElement(new Pair<Float, Float>(2.0f, 0.25f));
            constellationEnd.addElement(new Pair<Float, Float>(5.0f, 1.0f));

            float xRangeDriftVel = (rand.nextFloat() * 0.1f) - 0.05f;

            returnConstellation = new ConstellationFactory(drawContainer, options, spawnY, -0.0325f, 0.0025f, 4.0f, xRangeDriftVel, spawnChanceCurve, constellationEnd);
        }

        return returnConstellation;
    }

    static ConstellationFactory getDebrisFactory(Random rand, Vector<BackgroundObject> drawContainer, String specialCommand) //!< A factory function to get a certain type of factory. Meta, but useful.
    {
        // ConstellationFactory(Vector<BackgroundObject> drawContainer, Vector<Pair<BackgroundSpawnOption, Integer>> options, float spawnY, float yVelocity, float yVelocityVar, float xRange, float xRangeDriftVel, Vector<Pair<Float, Float>> spawnChanceCurve, Vector<Pair<Float, Float>> constellationEnd) //!< Full Constructor
        // BackgroundSpawnOption(String baseTextureName, Rect baseTextureRect, Vector2f size, Vector2f origin, float sizeScalarVar, float rotation, float rotationVar, float rotationVel, float rotationVelVar) //!< Full Constructor


        // Set up normal SpawnOption Vectors, which can be edited if necessary.
        Vector<Pair<BackgroundSpawnOption, Integer>> options = new Vector<Pair<BackgroundSpawnOption, Integer>>();
        /* debrisRed0 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(0, 1024, 64, 1088), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisRed1 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(0, 1088, 64, 1152), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisRed2 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(0, 1152, 64, 1216), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisRed3 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(0, 1216, 64, 1280), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisRed4 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(0, 1280, 64, 1344), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisRed5 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(64, 1024, 128, 1088), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisRed6 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(64, 1088, 128, 1152), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisRed7 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(64, 1152, 128, 1216), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisRed8 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(64, 1216, 128, 1280), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisRed9 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(64, 1280, 128, 1344), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisPink0 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(128, 1024, 192, 1088), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisPink1 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(128, 1088, 192, 1152), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisPink2 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(128, 1152, 192, 1216), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisPink3 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(128, 1216, 192, 1280), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisPink4 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(128, 1280, 192, 1344), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisPink5 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(192, 1024, 256, 1088), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisPink6 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(192, 1088, 256, 1152), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisPink7 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(192, 1152, 256, 1216), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisPink8 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(192, 1216, 256, 1280), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisPink9 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(192, 1280, 256, 1344), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisViolet0 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(256, 1024, 320, 1088), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisViolet1 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(256, 1088, 320, 1152), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisViolet2 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(256, 1152, 320, 1216), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisViolet3 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(256, 1216, 320, 1280), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisViolet4 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(256, 1280, 320, 1344), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisViolet5 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(320, 1024, 384, 1088), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisViolet6 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(320, 1088, 384, 1152), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisViolet7 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(320, 1152, 384, 1216), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisViolet8 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(320, 1216, 384, 1280), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));
        /* debrisViolet9 */options.addElement(new Pair<BackgroundSpawnOption, Integer>(new BackgroundSpawnOption("game_background_spritesheet", new Rect(320, 1280, 384, 1344), new Vector2f(0.75f), new Vector2f(0.375f), 0.5f, 0.0f, 180.0f, 0.0f, 1.0f), 1));

        Vector<Pair<Float, Float>> spawnChanceCurve = new Vector<Pair<Float, Float>>();
        spawnChanceCurve.addElement(new Pair<Float, Float>(1.0f, 0.0f));
        spawnChanceCurve.addElement(new Pair<Float, Float>(3.0f, 0.75f));
        spawnChanceCurve.addElement(new Pair<Float, Float>(4.0f, 1.0f));

        float spawnY = GameSurfaceView.DPtoGU(new Vector2i(0, 0)).y + 15.0f;

        if (!(specialCommand.equals("small") ||
                specialCommand.equals("medium") ||
                specialCommand.equals("large"))) {
            // No override entered, pick one of the options, as weighted.
            int choice = rand.nextInt(25);

            if (choice < 2) { specialCommand = "small"; }
            else if (choice < 4) { specialCommand = "medium"; }
            else { specialCommand = "large"; }
        }

        ConstellationFactory returnConstellation;

        if (specialCommand.equals("small")) {
            // Build a small debris field. 2-5

            Vector<Pair<Float, Float>> constellationEnd = new Vector<Pair<Float, Float>>();
            constellationEnd.addElement(new Pair<Float, Float>(2.0f, 0.25f));
            constellationEnd.addElement(new Pair<Float, Float>(5.0f, 1.0f));

            float xRangeDriftVel = (rand.nextFloat() * 0.2f) - 0.01f;

            returnConstellation = new ConstellationFactory(drawContainer, options, spawnY, -0.05f, 0.005f, 4.0f, xRangeDriftVel, spawnChanceCurve, constellationEnd);
        } else if (specialCommand.equals("medium")) {
            // Build a medium debris field. 5-10

            Vector<Pair<Float, Float>> constellationEnd = new Vector<Pair<Float, Float>>();
            constellationEnd.addElement(new Pair<Float, Float>(5.0f, 0.1f));
            constellationEnd.addElement(new Pair<Float, Float>(10.0f, 0.75f));

            float xRangeDriftVel = (rand.nextFloat() * 0.2f) - 0.01f;

            returnConstellation = new ConstellationFactory(drawContainer, options, spawnY, -0.05f, 0.005f, 4.0f, xRangeDriftVel, spawnChanceCurve, constellationEnd);
        } else { //if (specialCommand.equals("large")) {
            // Build a large debris field. 10-15

            Vector<Pair<Float, Float>> constellationEnd = new Vector<Pair<Float, Float>>();
            constellationEnd.addElement(new Pair<Float, Float>(10.0f, 0.1f));
            constellationEnd.addElement(new Pair<Float, Float>(15.0f, 0.75f));

            float xRangeDriftVel = (rand.nextFloat() * 0.2f) - 0.01f;

            returnConstellation = new ConstellationFactory(drawContainer, options, spawnY, -0.05f, 0.005f, 4.0f, xRangeDriftVel, spawnChanceCurve, constellationEnd);
        }

        return returnConstellation;
    }
}
