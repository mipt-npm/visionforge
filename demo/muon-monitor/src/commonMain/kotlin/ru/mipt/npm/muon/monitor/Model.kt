package ru.mipt.npm.muon.monitor

import ru.mipt.npm.muon.monitor.Monitor.CENTRAL_LAYER_Z
import ru.mipt.npm.muon.monitor.Monitor.LOWER_LAYER_Z
import ru.mipt.npm.muon.monitor.Monitor.UPPER_LAYER_Z
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.removeAll
import space.kscience.visionforge.root
import space.kscience.visionforge.solid.*
import kotlin.math.PI

class Model(val manager: VisionManager) {
    private val map = HashMap<String, SolidGroup>()
    private val events = HashSet<Event>()

    private fun SolidGroup.pixel(pixel: SC1) {
        val group = group(pixel.name) {
            position = Point3D(pixel.center.x, pixel.center.y, pixel.center.z)
            box(pixel.xSize, pixel.ySize, pixel.zSize)
            label(pixel.name) {
                z = -Monitor.PIXEL_Z_SIZE / 2 - 5
                rotationY = PI
            }
        }
        map[pixel.name] = group
    }

    private fun SolidGroup.detector(detector: SC16) {
        group(detector.name) {
            detector.pixels.forEach {
                pixel(it)
            }
        }
    }

    var tracks: SolidGroup

    val root: SolidGroup = SolidGroup().apply {
        root(this@Model.manager)
        rotationX = PI / 2
        group("bottom") {
            Monitor.detectors.filter { it.center.z == LOWER_LAYER_Z }.forEach {
                detector(it)
            }
        }

        group("middle") {
            Monitor.detectors.filter { it.center.z == CENTRAL_LAYER_Z }.forEach {
                detector(it)
            }
        }

        group("top") {
            Monitor.detectors.filter { it.center.z == UPPER_LAYER_Z }.forEach {
                detector(it)
            }
        }
        tracks = group("tracks")
    }

    private fun highlight(pixel: String) {
        map[pixel]?.color?.invoke("blue")
    }

    fun reset() {
        map.values.forEach {
            it.setProperty(SolidMaterial.MATERIAL_COLOR_KEY, null)
        }
        tracks.removeAll()
    }

    fun displayEvent(event: Event) {
        events.add(event)
        event.hits.forEach {
            highlight(it)
        }
        event.track?.let {
            tracks.polyline(*it.toTypedArray(), name = "track[${event.id}]") {
                thickness = 4
            }
        }
    }

    fun encodeToString(): String = manager.encodeToString(this.root)
}