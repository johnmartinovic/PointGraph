package com.johnnym.pointgraph

import android.graphics.Path
import com.johnnym.pointgraph.utils.affineTransformXToY

class GraphPath : Path() {

    private var graphMinXPosition = 0f
    private var graphMinYPosition = 0f
    private var graphMaxXPosition = 0f
    private var graphMaxYPosition = 0f

    fun generatePath(
            pointsData: PointsData,
            graphMinXPosition: Float,
            graphMinYPosition: Float,
            graphMaxXPosition: Float,
            graphMaxYPosition: Float) {
        this.graphMinXPosition = graphMinXPosition
        this.graphMinYPosition = graphMinYPosition
        this.graphMaxXPosition = graphMaxXPosition
        this.graphMaxYPosition = graphMaxYPosition

        val knotsArr = getGraphPointsFromPointsData(pointsData)
        val (firstCP, secondCP) = createBezierSplineControlPoints(knotsArr)

        reset()

        // move to the start of the graph
        moveTo(graphMinXPosition, graphMinYPosition)
        lineTo(knotsArr[0].x, knotsArr[0].y)
        for (i in firstCP.indices) {
            cubicTo(firstCP[i].x, firstCP[i].y,
                    secondCP[i].x, secondCP[i].y,
                    knotsArr[i + 1].x, knotsArr[i + 1].y)
        }

        // move to the end of the graph
        lineTo(graphMaxXPosition, graphMinYPosition)
    }

    private fun getGraphPointsFromPointsData(pointsData: PointsData): List<Point> {
        val points = pointsData.points

        return List(points.size) { index ->
            Point(
                    affineTransformXToY(points[index].x, pointsData.minX, pointsData.maxX, graphMinXPosition, graphMaxXPosition),
                    affineTransformXToY(points[index].y, pointsData.minY, pointsData.maxY, graphMinYPosition, graphMaxYPosition))
        }
    }
}
