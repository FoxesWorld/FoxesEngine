package org.foxesworld.engine.utils;

import java.awt.geom.Point2D;

public class BezierCurve {
    private final Point2D.Float[] controlPoints;

    public BezierCurve(Point2D.Float... controlPoints) {
        this.controlPoints = controlPoints;
    }

    public Point2D.Float compute(float t) {
        if (controlPoints.length < 2) {
            throw new IllegalArgumentException("At least two control points are required");
        }

        int n = controlPoints.length - 1;
        Point2D.Float[] points = new Point2D.Float[n + 1];
        System.arraycopy(controlPoints, 0, points, 0, n + 1);

        for (int k = 1; k <= n; k++) {
            for (int i = 0; i <= n - k; i++) {
                points[i].x = (1 - t) * points[i].x + t * points[i + 1].x;
                points[i].y = (1 - t) * points[i].y + t * points[i + 1].y;
            }
        }

        return points[0];
    }
}
