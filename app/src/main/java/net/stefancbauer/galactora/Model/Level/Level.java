package net.stefancbauer.galactora.Model.Level;

import android.content.Context;
import android.graphics.RectF;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Armour;
import net.stefancbauer.galactora.Model.Background.BackgroundManager;
import net.stefancbauer.galactora.Model.Enemy;
import net.stefancbauer.galactora.Model.EnemyType;
import net.stefancbauer.galactora.Model.Game;
import net.stefancbauer.galactora.Model.Pathing.Path;
import net.stefancbauer.galactora.Model.Pathing.PathFactory;
import net.stefancbauer.galactora.Model.Pathing.PathGroup;
import net.stefancbauer.galactora.Model.Projectile;
import net.stefancbauer.galactora.Model.Shield;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Vector;

/**
 * Handles the sequencing and choreography of a level.
 */

public class Level {
    // All my sins have caught up to me. Turns out, abusing scope causes issues. It also turns out that Factory objects are better that static factory methods.
    private final Game p_game;
    private final Random p_rand;
    private final BackgroundManager p_bm;
    private final Vector<Enemy> p_enemies;
    private final Vector<Projectile> p_projectiles;
    private final Vector<Shield> p_shields;
    private final Vector<Armour> p_armours;

    public boolean m_levelComplete = false;


    public String m_fileName; //!< Name of level xml file.

    public int m_difficultyRating;

    public Vector<assetTrigger> m_onStart = new Vector<assetTrigger>(); //!< Effects to be called on level start.
    private boolean m_finishedOnStart = false;

    public Vector<assetTrigger> m_onEnd = new Vector<assetTrigger>(); //!< Effects to be called on level end.
    private boolean m_finishedOnEnd = false;

    public Vector<PathGroup> m_groups = new Vector<PathGroup>();
    public Vector<Wave> m_waves  = new Vector<Wave>();

    public boolean m_finishedStart = false;

    public int m_midCount;
    public int m_midDelay;
    public int m_midCurrentDelay;
    public Vector<FlybyTemplate> m_midOptions = new Vector<FlybyTemplate>();
    public float m_totalMidWeight = 0.0f;
    public boolean m_finishedMid = false;

    public PathFactory.exitType m_exitType; //!< What path type to give escaping enemies.
    public int m_exitDelay; //!< How long to wait after the last entrance or flyby behaviour before enemies start exiting.
    public int m_exitCurrentDelay = 0;
    public int m_exitStep; //!< How long between triggering new groups of exiting enemies.
    public int m_exitGroupSize; //!< How many enemies should exit in each group.
    public boolean m_hasAnEnemyEscaped = false; //!< Whether or not an enemy has escaped; If one has then it's not a perfect level.
    public boolean m_finishedEnd = false;

    public Level(Context context, String inp_filename, Game dest_game, BackgroundManager dest_bm) {
        p_game = dest_game;
        p_rand = p_game.m_rand;
        p_bm = dest_bm;
        p_enemies = p_game.m_enemies;
        p_projectiles = p_game.m_projectiles;
        p_shields = p_game.m_shields;
        p_armours = p_game.m_armours;

        // Load and parse xml
        try {
            parse(context.getAssets().open(inp_filename));
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        // Do onStart stuff.

        if (!m_finishedOnStart) {
            boolean done = false;
            while (m_onStart.size() > 0 && !done) {
                if (m_onStart.get(0).delay == 0) {
                    parseCommand(m_onStart.get(0).command);
                    m_onStart.remove(0);
                } else {
                    m_onStart.get(0).delay--;
                    done = true;
                }
            }

            if (m_onStart.size() == 0) { m_finishedOnStart = true; }
        }
        else if (!m_finishedStart) {
            // If there are waves to spawn, do that.
            boolean done = false;
            while ( m_waves.size() > 0 && !done) {
                if (m_waves.get(0).m_delay == 0) {
                    spawnWave(m_waves.get(0));
                    m_waves.remove(0);
                } else {
                    m_waves.get(0).m_delay--;
                    done = true;
                }
            }

            if (m_waves.size() == 0) { m_finishedStart = true; }
        }
        else if (!m_finishedMid) {
            if (m_midCurrentDelay > 0) { m_midCurrentDelay--; }
            else {
                // Try to trigger a flyby.
                boolean done = false;
                for (int i = 0; i < 10 && !done; i++) {
                    // Get a template
                    float chooser = p_rand.nextFloat() * m_totalMidWeight;
                    FlybyTemplate choice = m_midOptions.get(0);
                    float weightSoFar = 0.0f;
                    boolean foundChoice = false;
                    for (int j = 0; j < m_midOptions.size() && !foundChoice; j++) {
                        weightSoFar += m_midOptions.get(j).weight;
                        if (chooser < weightSoFar) {
                            choice = m_midOptions.get(j);
                            foundChoice = true;
                        }
                    }

                    // We have a template, now try to spawn it.
                    for (int j = 0; j < 5 && !done; j++) { done = trySpawnFlyby(choice); }
                }

                m_midCount--;
                m_midCurrentDelay = m_midDelay;
            }


            if (m_midCount == 0  && m_midCurrentDelay == 0) { m_finishedMid = true; }
        }
        else if (!m_finishedEnd) {
            // Enemy exits
            if (m_exitCurrentDelay > 0) { m_exitCurrentDelay--; }
            else if (p_enemies.size() > 0) {
                Vector<Enemy> availableCandidates = new Vector<Enemy>();

                for (int i = 0; i < p_enemies.size(); i++) {
                    if (p_enemies.get(i).inGridMode && !p_enemies.get(i).m_exiting) {
                        availableCandidates.addElement(p_enemies.get(i));
                    }
                    else if (p_enemies.get(i).inGridMode && p_enemies.get(i).m_exiting) {
                        p_enemies.get(i).delete();
                        m_hasAnEnemyEscaped = true;
                    }
                }

                if (availableCandidates.size() > 0) {
                    int numToDo = Math.min(m_exitGroupSize, availableCandidates.size());

                    for (int i = 0; i < numToDo; i++) {
                        int choice = p_rand.nextInt(availableCandidates.size());
                        Enemy enemy = availableCandidates.get(choice);

                        enemy.m_path = PathFactory.getExit(PathFactory.exitType.DRIFT, enemy.gridPos);
                        enemy.m_exiting = true;
                        enemy.inGridMode = false;
                        enemy.m_atStartPoint = false;
                        enemy.m_mayFollowPath = true;
                        availableCandidates.remove(choice);
                    }

                    m_exitCurrentDelay = m_exitStep;
                }
            } else {
                m_finishedEnd = true;
            }
        }
        else if (!m_finishedOnEnd) {
            boolean done = false;
            while (m_onEnd.size() > 0 && !done) {
                if (m_onEnd.get(0).delay == 0) {
                    parseCommand(m_onEnd.get(0).command);
                    m_onEnd.remove(0);
                } else {
                    m_onEnd.get(0).delay--;
                    done = true;
                }
            }

            if (m_onEnd.size() == 0) { m_finishedOnEnd = true; }
        }
        else { m_levelComplete = true; }

        // Rebalance and clean up path groups.
        for (int i = m_groups.size() - 1; i >= 0 ; i--) {
            m_groups.get(i).update();
            if (m_groups.get(i).m_toBeDestroyed) { m_groups.remove(i); }
        }
    }

    private void parseCommand(String command) {
        String parameters = "";

        if (command.equalsIgnoreCase("scorecard")) { parameters = Integer.toString(p_game.m_score); }
        else if (command.equalsIgnoreCase("victory")) { parameters = Integer.toString(p_game.m_score); }

        p_bm.addLevelEffect(command, parameters);
    }

    private void spawnWave(Wave wave) {
        PathGroup group = new PathGroup();
        for (int i = 0; i < wave.m_spawns.size(); i++) {
            EnemySpawnData template = wave.m_spawns.get(i);

            Path path = PathFactory.getEntrance(wave.m_entryDirection, template.m_stackDepth, template.m_offset);
            Vector2f startCoords = path.nodes.get(0).coords;
            Vector2f relative = Vector2f.subtract(path.nodes.get(1).coords, startCoords);
            float startAngle = (float)Math.toDegrees(Math.atan2(relative.y, relative.x));

            Enemy enemy;
            if (template.m_type == EnemyType.WARRIOR) { enemy = Enemy.getWarrior(p_game, p_bm, p_projectiles, startCoords, startAngle); }
            else if (template.m_type == EnemyType.GUARDIAN) { enemy = Enemy.getGuardian(p_game, p_bm, p_projectiles, startCoords, startAngle, p_shields); }
            else if (template.m_type == EnemyType.QUEEN) { enemy = Enemy.getQueen(p_game, p_bm, p_projectiles, startCoords, startAngle, p_armours); }
            else { enemy = Enemy.getDrone(p_game, p_bm, p_projectiles, startCoords, startAngle); }

            enemy.m_path = path;
            enemy.inGridMode = false;
            enemy.unmodifiedGridPos.set(template.m_gridPos);
            group.add(path, enemy);
            p_enemies.addElement(enemy);
        }

        group.balancePaths();
        m_groups.addElement(group);
    }

    private boolean trySpawnFlyby(FlybyTemplate template) {
        boolean onLeft = p_rand.nextBoolean(); // Whether to choose from left or right side.

        float xRange = 7.0f - template.range.x;
        float yRange = p_game.m_gridYMin + template.range.y;
        float xCentre = Math.round(2.0f * ((p_rand.nextFloat() * xRange) + 3.5 - (0.5f * xRange))) * 0.5f;
        if (onLeft) { xCentre *= -1.0f; }
        float yCentre = Math.round(2.0f * ((p_rand.nextFloat() * yRange) + (0.5f * p_game.m_gridYMin) - (0.5f * yRange))) * 0.5f;

        RectF selectionArea = new RectF(xCentre - (0.5f * template.range.x),
                yCentre + (0.5f * template.range.y) + 16.0f,
                xCentre + (0.5f * template.range.x),
                yCentre - (0.5f * template.range.y) + 16.0f);

        Vector<Enemy> inRange = new Vector<Enemy>();

        for (int i = 0; i < p_enemies.size(); i++) {
            Enemy current = p_enemies.get(i);
            if (current.inGridMode) {
                if (current.gridPos.x >= selectionArea.left &&
                        current.gridPos.x <= selectionArea.right &&
                        current.gridPos.y >= selectionArea.bottom &&
                        current.gridPos.y <= selectionArea.top) {
                    inRange.addElement(current);
                }
            }
        }

        if (inRange.size() < 2) { return false; }
        else {
            PathGroup group = new PathGroup();
            for (int i = 0; i < inRange.size(); i++) {
                Enemy current = inRange.get(i);

                float offset = (current.gridPos.y - (yCentre + 16.0f));
                Path path = PathFactory.getFlyby(template.type, current.gridPos, offset);

                current.m_path = path;
                current.inGridMode = false;
                current.m_atStartPoint = false;
                current.m_mayFollowPath = false;
                group.add(path, current);
            }

            group.balancePaths();
            m_groups.addElement(group);

            return true;
        }
    }

    // Modified code, courtesy of Sam Knight
    // https://pastebin.com/k1npx8rL
    public void parse (InputStream is) throws XmlPullParserException, IOException {
        XmlPullParserFactory m_Factory;
        XmlPullParser m_Parser;
        m_Factory = XmlPullParserFactory.newInstance();
        m_Parser = m_Factory.newPullParser();
        m_Factory.setNamespaceAware(true);
        m_Parser.setInput(is,null);

        boolean inOnStart = false;
        boolean inOnEnd = false;

        Wave wave = new Wave(PathFactory.direction.LEFT, 0);

        int eventType = m_Parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = m_Parser.getName();
            switch(eventType)
            {
                case XmlPullParser.START_TAG:
                    if (tagName.equalsIgnoreCase("level")) {
                        m_difficultyRating = Integer.parseInt(m_Parser.getAttributeValue(0));
                    }
                    else if (tagName.equalsIgnoreCase("onstart")) {
                        inOnStart = true;
                    }
                    else if (tagName.equalsIgnoreCase("onend")) {
                        inOnEnd = true;
                    }
                    else if (tagName.equalsIgnoreCase("asset")) {
                        String command = m_Parser.getAttributeValue(0);
                        Integer delay = Integer.parseInt(m_Parser.getAttributeValue(1));
                        // Check if it should be added to m_onStart or m_onEnd.
                        if (inOnStart) {
                            m_onStart.addElement(new assetTrigger(delay, command));
                        }
                        else if (inOnEnd) {
                            m_onEnd.addElement(new assetTrigger(delay, command));
                        }
                    }
                    else if (tagName.equalsIgnoreCase("wave")) {
                        String temp = m_Parser.getAttributeValue(0);
                        PathFactory.direction spawnDirection;

                        if (temp.equalsIgnoreCase("left")) { spawnDirection = PathFactory.direction.LEFT; }
                        else if (temp.equalsIgnoreCase("right")) { spawnDirection = PathFactory.direction.RIGHT; }
                        else if (temp.equalsIgnoreCase("topleft")) { spawnDirection = PathFactory.direction.TOPLEFT; }
                        else { spawnDirection = PathFactory.direction.TOPRIGHT; }

                        int delay = Integer.parseInt(m_Parser.getAttributeValue(1));
                        wave = new Wave(spawnDirection, delay);
                    }
                    else if (tagName.equalsIgnoreCase("enemy")) {
                        String temp = m_Parser.getAttributeValue(0);
                        EnemyType type;

                        if (temp.equalsIgnoreCase("warrior")) { type = EnemyType.WARRIOR; }
                        else if (temp.equalsIgnoreCase("guardian")) { type = EnemyType.GUARDIAN; }
                        else if (temp.equalsIgnoreCase("queen")) { type = EnemyType.QUEEN; }
                        else { type = EnemyType.DRONE; }

                        float stackDepth = Float.parseFloat(m_Parser.getAttributeValue(1));
                        float offset = Float.parseFloat(m_Parser.getAttributeValue(2));
                        float gridX = Float.parseFloat(m_Parser.getAttributeValue(3));
                        float gridY = Float.parseFloat(m_Parser.getAttributeValue(4));
                        wave.addEnemySpawn(type, stackDepth, offset, gridX, gridY);
                    }
                    else if (tagName.equalsIgnoreCase("onmid")) {
                        m_midCount = Integer.parseInt(m_Parser.getAttributeValue(0));
                        m_midDelay = Integer.parseInt(m_Parser.getAttributeValue(1));
                        m_midCurrentDelay = m_midDelay;
                    }
                    else if (tagName.equalsIgnoreCase("flyby")) {
                        String temp = m_Parser.getAttributeValue(0);
                        PathFactory.flybyType type;

                        if (temp.equalsIgnoreCase("trombone")) { type = PathFactory.flybyType.TROMBONE; }
                        else if (temp.equalsIgnoreCase("bicycle")) { type = PathFactory.flybyType.BICYCLE; }
                        else { type = PathFactory.flybyType.BICYCLE; }

                        float weight = Float.parseFloat(m_Parser.getAttributeValue(1));
                        float xRange = Float.parseFloat(m_Parser.getAttributeValue(2));
                        float yRange = Float.parseFloat(m_Parser.getAttributeValue(3));

                        m_midOptions.addElement(new FlybyTemplate(type, weight, xRange, yRange));
                        m_totalMidWeight += weight;
                    }
                    else if (tagName.equalsIgnoreCase("exit")) {
                        String temp = m_Parser.getAttributeValue(0);
                        PathFactory.exitType type;

                        if (temp.equalsIgnoreCase("drift")) { type = PathFactory.exitType.DRIFT; }
                        else { type = PathFactory.exitType.DRIFT; }

                        m_exitType = type;
                        m_exitDelay = Integer.parseInt(m_Parser.getAttributeValue(1));
                        m_exitStep = Integer.parseInt(m_Parser.getAttributeValue(2));
                        m_exitGroupSize = Integer.parseInt(m_Parser.getAttributeValue(3));
                    }

                    break;

                case XmlPullParser.TEXT:
                    //m_Text = m_Parser.getText();
                    break;

                case XmlPullParser.END_TAG:
                    if (tagName.equalsIgnoreCase("onstart")) {
                        inOnStart = false;
                    }
                    else if (tagName.equalsIgnoreCase("onend")) {
                        inOnEnd = false;
                    }
                    else if (tagName.equalsIgnoreCase("wave")) {
                        if (wave.m_spawns.size() > 0) {
                            // Make sure there's actually something in the wave.
                            m_waves.addElement(wave);
                        }
                    }

                    break;
            }
            eventType = m_Parser.next();
        }
    }
}
