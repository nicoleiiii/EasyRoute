package com.example.easyroute.data

import com.example.easyroute.model.*
import org.osmdroid.util.GeoPoint
import java.util.PriorityQueue
import kotlin.math.abs

object JeepneyData {
    // --- CONSTANTS FROM JS ---
    private const val WALKING_SPEED_MPS = 1.4 // ~5 km/h
    private const val JEEPNEY_SPEED_MPS = 5.56 // ~20 km/h

    // Your existing routes data
    val routes = listOf(
        JeepneyRoute("route1", "CHECKPOINT - HOLY - HIGHWAY", "#8989ff", listOf(
            JeepneyStop("Highway Terminal", GeoPoint(15.1670069, 120.5802458)),
            JeepneyStop("Angeles City Hall", GeoPoint(15.1692110, 120.5867240)),
            JeepneyStop("Marisol Terminal", GeoPoint(15.1518836, 120.5915452)),
            JeepneyStop("Holy Family Hospital", GeoPoint(15.1372074, 120.5864935)),
            JeepneyStop("Holy Rosary Parish Church", GeoPoint(15.1347703, 120.5905817)),
            JeepneyStop("SM Clark", GeoPoint(15.1429697, 120.5965464)),
            JeepneyStop("AUF", GeoPoint(15.1456720, 120.5950569)),
            JeepneyStop("Highway Terminal", GeoPoint(15.1670069, 120.5802458))

        )),
        JeepneyRoute("route2", "HOLY - HIGHWAY - CHECKPOINT", "#8989ff", listOf(
            JeepneyStop("Checkpoint Terminal", GeoPoint(15.1518836, 120.5915452)),
            JeepneyStop("Holy Family Hospital", GeoPoint(15.1372074, 120.5864935)),
            JeepneyStop("Holy Rosary Parish Church", GeoPoint(15.1347703, 120.5905817)),
            JeepneyStop("SM Clark", GeoPoint(15.1429697, 120.5965464)),
            JeepneyStop("AUF", GeoPoint(15.1460053, 120.5949980)),
            JeepneyStop("Angeles City Hall", GeoPoint(15.1692110, 120.5867240)),
            JeepneyStop("Highway Terminal", GeoPoint(15.1670069, 120.5802458))
        )),
        JeepneyRoute("route3", "MARISOL - PAMPANG", "#2ECC71", listOf(
            JeepneyStop("Marisol Terminal", GeoPoint(15.1514673, 120.5911618)),
            JeepneyStop("Pampang Public Market", GeoPoint(15.1450051, 120.5887022)),
            JeepneyStop("Plaridel", GeoPoint(15.1380867, 120.5886441)),
            JeepneyStop("Holy Rosary Parish Church", GeoPoint(15.1347703, 120.5905817)),
            JeepneyStop("SM Clark", GeoPoint(15.1429697, 120.5965464)),
            JeepneyStop("Pampang Terminal", GeoPoint(15.1514743, 120.5924792))
        )),
        JeepneyRoute("route4", "PAMPANG - MARISOL", "#2ECC71", listOf(
            JeepneyStop("Marisol Market", GeoPoint(15.1463300, 120.5862477)),
            JeepneyStop("Pampang Public Market", GeoPoint(15.1450026, 120.5886574)),
            JeepneyStop("Holy Family Hospital", GeoPoint(15.1372074, 120.5864935)),
            JeepneyStop("Holy Rosary Parish Church", GeoPoint(15.1347703, 120.5905817)),
            JeepneyStop("SM Clark", GeoPoint(15.1429697, 120.5965464)),
            JeepneyStop("Pampang Terminal", GeoPoint(15.1514743, 120.5924792))
        )),
        JeepneyRoute("route5", "CAPAYA - ANGELES", "#ff70ff", listOf(
            JeepneyStop("Capaya Terminal", GeoPoint(15.1464343, 120.6141124)),
            JeepneyStop("Capaya Market", GeoPoint(15.1499977, 120.6145657)),
            JeepneyStop("Angeles University", GeoPoint(15.1536670, 120.6047337)),
            JeepneyStop("Robinsons Angeles", GeoPoint(15.1438748, 120.5971975)),
            JeepneyStop("SM Clark", GeoPoint(15.1427392, 120.5965313)),
            JeepneyStop("Holy Rosary Parish Church", GeoPoint(15.1347703, 120.5905817)),
            JeepneyStop("Pampang Crossing", GeoPoint(15.1380213, 120.5888340)),
            JeepneyStop("Holy Family Hospital", GeoPoint(15.1372241, 120.5864738)),
            JeepneyStop("Angeles City Proper", GeoPoint(15.1378667, 120.5886299))
        ))
    )

    // Helper: Find nearest stop (used in UI and Logic)
    fun findNearestStop(userLocation: GeoPoint, stops: List<JeepneyStop>): Pair<JeepneyStop, Double>? {
        if (stops.isEmpty()) return null
        var nearestStop: JeepneyStop? = null
        var minDistance = Double.MAX_VALUE
        for (stop in stops) {
            val distance = userLocation.distanceToAsDouble(stop.point)
            if (distance < minDistance) {
                minDistance = distance
                nearestStop = stop
            }
        }
        return if (nearestStop != null) Pair(nearestStop, minDistance) else null
    }

    // --- ALGORITHM: DIJKSTRA FOR MULTI-ROUTE ---
    private fun stopKey(p: GeoPoint): String = "${p.latitude},${p.longitude}"

    // Main Function called by UI
    fun calculateMultiRouteTrip(start: GeoPoint, end: GeoPoint): TripPlan? {
        val allStops = routes.flatMap { it.stops }

        // 1. Find Entry and Exit points on the network
        val startStopPair = findNearestStop(start, allStops) ?: return null
        val endStopPair = findNearestStop(end, allStops) ?: return null

        val startNode = startStopPair.first
        val endNode = endStopPair.first

        // 2. Build Graph
        val graph = mutableMapOf<String, MutableList<Pair<JeepneyStop, Double>>>()

        fun addEdge(from: JeepneyStop, to: JeepneyStop) {
            val dist = from.point.distanceToAsDouble(to.point)
            val k1 = stopKey(from.point)
            if (!graph.containsKey(k1)) graph[k1] = mutableListOf()
            graph[k1]?.add(Pair(to, dist))
        }

        routes.forEach { route ->
            for (i in 0 until route.stops.size - 1) {
                addEdge(route.stops[i], route.stops[i+1])
                // Add reverse edge only if we assume bidirectional or transfer possibility
                // Your JS adds simple edges. We'll stick to Route direction.
                // NOTE: If your JS assumed bidirectional graph, uncomment line below:
                // addEdge(route.stops[i+1], route.stops[i])
            }
        }

        // 3. Dijkstra
        val distances = mutableMapOf<String, Double>()
        val previous = mutableMapOf<String, JeepneyStop>()
        val pq = PriorityQueue<Pair<JeepneyStop, Double>>(compareBy { it.second })
        val stopMap = mutableMapOf<String, JeepneyStop>() // To retrieve objects by key

        graph.keys.forEach { distances[it] = Double.MAX_VALUE }

        val startKey = stopKey(startNode.point)
        distances[startKey] = 0.0
        pq.add(Pair(startNode, 0.0))
        stopMap[startKey] = startNode

        while (pq.isNotEmpty()) {
            val (u, dist) = pq.poll()!!
            val uKey = stopKey(u.point)

            if (dist > (distances[uKey] ?: Double.MAX_VALUE)) continue
            if (uKey == stopKey(endNode.point)) break

            graph[uKey]?.forEach { (v, weight) ->
                val vKey = stopKey(v.point)
                stopMap[vKey] = v // Cache

                val newDist = dist + weight
                if (newDist < (distances[vKey] ?: Double.MAX_VALUE)) {
                    distances[vKey] = newDist
                    previous[vKey] = u
                    pq.add(Pair(v, newDist))
                }
            }
        }

        // 4. Reconstruct Path
        val pathStops = ArrayList<JeepneyStop>()
        var curr: JeepneyStop? = endNode

        if (distances[stopKey(endNode.point)] == Double.MAX_VALUE) return null // No path found

        while (curr != null) {
            pathStops.add(0, curr)
            val prevKey = stopKey(curr.point)
            // Logic to get previous node. The Map stores key -> parent
            // But our 'previous' map keys are child, values are parent.
            curr = if (curr == startNode) null else previous[prevKey]
        }

        // 5. Build Segments (Walk -> Ride(s) -> Walk)
        val segments = ArrayList<TripSegment>()
        var totalDist = 0.0
        var totalTime = 0.0

        // Segment A: Walk User -> First Stop
        val walk1Dist = start.distanceToAsDouble(startNode.point)
        segments.add(TripSegment(SegmentType.WALK, null, listOf(start, startNode.point), walk1Dist, "Walk to ${startNode.name}"))
        totalDist += walk1Dist
        totalTime += walk1Dist / WALKING_SPEED_MPS

        // Segment B: The Rides
        if (pathStops.size > 1) {
            // Group stops into routes
            var currentSegmentStops = ArrayList<GeoPoint>()
            currentSegmentStops.add(pathStops[0].point)

            var currentRoute: JeepneyRoute? = findRouteForSegment(pathStops[0], pathStops[1])

            for (i in 0 until pathStops.size - 1) {
                val a = pathStops[i]
                val b = pathStops[i+1]
                val nextRoute = findRouteForSegment(a, b)

                if (nextRoute != currentRoute && nextRoute != null) {
                    // Route changed! Push previous segment
                    if (currentRoute != null) {
                        val segDist = calculatePathDist(currentSegmentStops)
                        segments.add(TripSegment(SegmentType.RIDE, currentRoute, ArrayList(currentSegmentStops), segDist, "Ride ${currentRoute.name}"))
                        totalDist += segDist
                        totalTime += segDist / JEEPNEY_SPEED_MPS
                    }
                    // Reset for new route
                    currentSegmentStops.clear()
                    currentSegmentStops.add(a.point)
                    currentRoute = nextRoute
                }
                currentSegmentStops.add(b.point)
            }

            // Push final ride segment
            if (currentRoute != null && currentSegmentStops.size > 1) {
                val segDist = calculatePathDist(currentSegmentStops)
                segments.add(TripSegment(SegmentType.RIDE, currentRoute, currentSegmentStops, segDist, "Ride ${currentRoute.name}"))
                totalDist += segDist
                totalTime += segDist / JEEPNEY_SPEED_MPS
            }
        }

        // Segment C: Walk Last Stop -> Dest
        val walk2Dist = endNode.point.distanceToAsDouble(end)
        segments.add(TripSegment(SegmentType.WALK, null, listOf(endNode.point, end), walk2Dist, "Walk to Destination"))
        totalDist += walk2Dist
        totalTime += walk2Dist / WALKING_SPEED_MPS

        return TripPlan(segments, totalTime, totalDist)
    }

    private fun findRouteForSegment(a: JeepneyStop, b: JeepneyStop): JeepneyRoute? {
        // Find a route that contains both stops ADJACENTLY (or close)
        return routes.find { r ->
            val idxA = r.stops.indexOfFirst { stopKey(it.point) == stopKey(a.point) }
            val idxB = r.stops.indexOfFirst { stopKey(it.point) == stopKey(b.point) }
            idxA != -1 && idxB != -1 && abs(idxA - idxB) == 1
        }
    }

    private fun calculatePathDist(points: List<GeoPoint>): Double {
        var d = 0.0
        for(i in 0 until points.size - 1) d += points[i].distanceToAsDouble(points[i+1])
        return d
    }
}