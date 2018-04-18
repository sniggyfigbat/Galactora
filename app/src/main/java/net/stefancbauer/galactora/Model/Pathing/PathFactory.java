package net.stefancbauer.galactora.Model.Pathing;

import net.stefancbauer.galactora.LocalMaths.Vector2f;

/**
 * Created by P13186907 on 16/04/2018.
 */

public abstract class PathFactory {

    static final int curveIncrements = 5;
    public enum direction {
        LEFT,
        RIGHT,
        TOPLEFT,
        TOPRIGHT
    }

    public enum flybyType {
        TROMBONE, // A circular path with a fixed curved endcap and in/out sections that slide along the x axis.
        BICYCLE // A path with a loop on either end.
    }

    public enum exitType {
        DRIFT, // Gently meanders out.
    }

    public static Path getEntrance(direction spawnFrom, float stackDist, float offset) //!< Returns a path from a spawnpoint on the outside of the play area to the grid area.
    {
        Path returnPath = new Path();

        Vector2f curveCentre = new Vector2f();
        float curveRadius;

        if (spawnFrom == direction.TOPLEFT || spawnFrom == direction.TOPRIGHT) {
            // Path from a top-right spawn
            returnPath.addNode(3.0f - offset, 23.0f + stackDist);
            returnPath.addNode(3.0f - offset, 18.0f, false);

            // 45 degree curve to ~(1.5f, 14.5f)
            curveCentre.set(-2.0f, 18.0f);
            curveRadius = 5.0f - offset;
            for (int i = 360 - curveIncrements; i >= 315; i -= curveIncrements) {
                double angle = Math.toRadians(i);
                returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
            }

            // Go along a bit to ~(-1.5, 11.5)
            // 45 degree curve to (-3.0f, 8.0f)
            curveCentre.set(2.0f, 8.0f);
            curveRadius = 5.0f + offset;
            for (int i = 135; i <= 180; i += curveIncrements) {
                double angle = Math.toRadians(i);
                returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
            }

            // 180 degree curve to (3.0f, 8.0f)
            curveCentre.set(0.0f, 8.0f);
            curveRadius = 3.0f + offset;
            for (int i = 180 + curveIncrements; i <= 360; i += curveIncrements) {
                double angle = Math.toRadians(i);
                returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
            }

            // Path done

            if (spawnFrom == direction.TOPLEFT) {
                // Flip it across the Y axis.
                for (int i = 0; i < returnPath.nodes.size(); i++) { returnPath.nodes.get(i).coords.x *= -1.0f; }
            }
        }
        else {
            // Path from a right spawn
            returnPath.addNode(12.0f + stackDist, 5.0f + offset);
            returnPath.addNode(7.0f, 5.0f + offset, false);

            // 90 degree curve to (3.0f, 9.0f)
            curveCentre.set(7.0f, 9.0f);
            curveRadius = 4.0f - offset;
            for (int i = 270 - curveIncrements; i >= 180; i -= curveIncrements) {
                double angle = Math.toRadians(i);
                returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
            }

            // 180 degree curve to (7.0f, 9.0f)
            curveCentre.set(5.0f, 9.0f);
            curveRadius = 2.0f - offset;
            for (int i = 180 - curveIncrements; i >= 0; i -= curveIncrements) {
                double angle = Math.toRadians(i);
                returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
            }

            // 90 degree curve to (4.0f, 6.0f)
            curveCentre.set(4.0f, 9.0f);
            curveRadius = 3.0f - offset;
            for (int i = 360 - curveIncrements; i >= 270; i -= curveIncrements) {
                double angle = Math.toRadians(i);
                returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
            }

            // 90 degree curve to (0.0f, 10.0f)
            curveCentre.set(4.0f, 10.0f);
            curveRadius = 4.0f - offset;
            for (int i = 270 - curveIncrements; i >= 180; i -= curveIncrements) {
                double angle = Math.toRadians(i);
                returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
            }

            // Path done

            if (spawnFrom == direction.LEFT) {
                // Flip it across the Y axis.
                for (int i = 0; i < returnPath.nodes.size(); i++) { returnPath.nodes.get(i).coords.x *= -1.0f; }
            }
        }
        return returnPath;
    }

    public static Path getFlyby(flybyType type, Vector2f gridPos, float offset) //!< Returns a path starting from a grid position, doing a flyby in the lower areas, and returning.
    {
        Path returnPath = new Path();

        Vector2f curveCentre = new Vector2f();
        float curveRadius;

        float x;

        switch (type) {
            case TROMBONE:
                x = Math.abs(gridPos.x);

                returnPath.addNode(x, gridPos.y);
                returnPath.addNode(x, 7.0f + offset);

                // 90 degree curve to (x - 1.0f, 6.0f + offset)
                curveCentre.set(x - 1.0f, 7.0f + offset);
                curveRadius = 1.0f;
                for (int i = 360 - curveIncrements; i >= 270; i -= curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // Go along a bit to (-0.5f, 6.0f + offset)
                returnPath.addNode(-0.5f, 6.0f + offset, false);

                // 90 degree curve to (-1.5f + offset, 7.0f)
                curveCentre.set(-2.5f, 7.0f);
                curveRadius = 1.0f - offset;
                for (int i = 270 - curveIncrements; i >= 180; i -= curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // 90 degree curve to (-3.5f, 9.0f + offset)
                curveCentre.set(-3.5f, 7.0f);
                curveRadius = 2.0f + offset;
                for (int i = 0 + curveIncrements; i <= 90; i += curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // 180 degree curve to (-3.5f, 3.0f)
                curveCentre.set(-3.5f, 6.0f);
                curveRadius = 3.0f + offset;
                for (int i = 90; i <= 270; i += curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // Go along a bit to (x - 4.0f, 3.0f)
                returnPath.addNode(x - 4.0f, 3.0f - offset, false);

                // 180 degree curve to (x, 7.0f)
                curveCentre.set(x - 4.0f, 7.0f);
                curveRadius = 4.0f + offset;
                for (int i = 270 + curveIncrements; i <= 360; i += curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // Path done

                if (gridPos.x < 0.0f) {
                    // Flip it across the Y axis.
                    for (int i = 0; i < returnPath.nodes.size(); i++) { returnPath.nodes.get(i).coords.x *= -1.0f; }
                }
                break;
            case BICYCLE:
                // Accidentally made this one default to left side. Doesn't really matter, but just bear in mind while editing it.
                x = -Math.abs(gridPos.x);

                returnPath.addNode(x, gridPos.y);

                // Go along a bit to (x, 6.0f + offset)
                returnPath.addNode(x, 6.0f + offset);

                // 90 degree curve to (x - 2.0f, 4.0f + offset)
                curveCentre.set(x - 2.0f, 6.0f + offset);
                curveRadius = 2.0f;
                for (int i = 360 - curveIncrements; i >= 270; i -= curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // Go along a bit to (-7.0f, 4.0f + offset)
                returnPath.addNode(-7.0f, 4.0f + offset, false);

                // 180 degree curve to (-7.0, 8.0f)
                curveCentre.set(-7.0f, 6.0f);
                curveRadius = 2.0f - offset;
                for (int i = 270 - curveIncrements; i >= 90; i -= curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // Go along a bit to (4.0f, 8.0f - offset)
                returnPath.addNode(4.0f, 8.0f - offset, true);

                // 540 degree curve to (4.0f, 2.0f)
                curveCentre.set(4.0f, 5.0f);
                curveRadius = 3.0f - offset;
                for (int i = 450; i >= -90; i -= curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // Go along a bit to (x + 4.0f, 2.0f + offset)
                returnPath.addNode(x + 4.0f, 2.0f + offset, false);

                // 90 degree curve to (x, 6.0f + offset)
                curveCentre.set(x + 4.0f, 6.0f + offset);
                curveRadius = 4.0f;
                for (int i = 270; i >= 180; i -= curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                returnPath.addNode(x, 8.0f + offset);

                // Path done

                if (gridPos.x > 0.0f) {
                    // Flip it across the Y axis.
                    for (int i = 0; i < returnPath.nodes.size(); i++) { returnPath.nodes.get(i).coords.x *= -1.0f; }
                }
                break;
        }

        return returnPath;
    }

    public static Path getExit(exitType type, Vector2f gridPos) //!< Returns a path from a grid position to the outside of the play area.
    {
        Path returnPath = new Path();

        Vector2f curveCentre = new Vector2f();
        float curveRadius;

        switch (type) {
            case DRIFT:
                returnPath.addNode(gridPos.x, gridPos.y);

                // 45 degree curve to ~(x + 0.5f, y - 1.5f)
                curveCentre.set(gridPos.x + 2.0f, gridPos.y);
                curveRadius = 2.0f;
                for (int i = 180 + curveIncrements; i <= 225; i += curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // Go along a bit to ~(x + 2.0f, y - 2.5f)
                // 90 degree curve to ~(x + 2.0f, y - 5.5f)
                curveCentre.set(gridPos.x + 0.5f, gridPos.y - 4.0f);
                curveRadius = 2.0f;
                for (int i = 45; i >= -45; i -= curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // Go along a bit to ~(x - 2.0f, y - 8.5f)
                // 90 degree curve to ~(x - 2.0f, y - 11.5f)
                curveCentre.set(gridPos.x - 0.5f, gridPos.y - 10.0f);
                curveRadius = 2.0f;
                for (int i = 135; i <= 225; i += curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // Go along a bit to ~(x + 2.0f, y - 14.5f)
                // 90 degree curve to ~(x + 2.0f, y - 17.5f)
                curveCentre.set(gridPos.x + 0.5f, gridPos.y - 16.0f);
                curveRadius = 2.0f;
                for (int i = 45; i >= -45; i -= curveIncrements) {
                    double angle = Math.toRadians(i);
                    returnPath.addNode((float)(Math.cos(angle) * curveRadius) + curveCentre.x, (float)(Math.sin(angle) * curveRadius) + curveCentre.y);
                }

                // Path done
                break;
        }

        return returnPath;
    }
}
