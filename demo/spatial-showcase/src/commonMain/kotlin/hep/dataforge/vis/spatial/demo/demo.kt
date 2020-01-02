package hep.dataforge.vis.spatial.demo

import hep.dataforge.meta.buildMeta
import hep.dataforge.meta.invoke
import hep.dataforge.names.toName
import hep.dataforge.output.OutputManager
import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.*
import hep.dataforge.vis.spatial.specifications.CanvasSpec
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


fun OutputManager.demo(name: String, title: String = name, block: VisualGroup3D.() -> Unit) {
    val meta = buildMeta {
        "title" put title
    }
    val output = get(VisualObject::class, name.toName(), meta = meta)
    output.render(action = block)
}

val canvasOptions = CanvasSpec {
    minSize = 500
    axes {
        size = 500.0
        visible = true
    }
    camera {
        distance = 600.0
        latitude = PI/6
    }
}

fun OutputManager.showcase() {
    demo("shapes", "Basic shapes") {
        box(100.0, 100.0, 100.0) {
            z = -110.0
        }
        sphere(50.0) {
            x = 110
            detail = 16
        }
        tube(50, height = 10, innerRadius = 25, angle = PI) {
            y = 110
            detail = 16
            rotationX = PI / 4
        }
    }

    demo("dynamic", "Dynamic properties") {
        val group = group {
            box(100, 100, 100) {
                z = 110.0
            }

            box(100, 100, 100) {
                visible = false
                x = 110.0
                //override color for this cube
                color(1530)

                GlobalScope.launch(Dispatchers.Main) {
                    while (isActive) {
                        delay(500)
                        visible = !(visible ?: false)
                    }
                }
            }
        }

        GlobalScope.launch(Dispatchers.Main) {
            val random = Random(111)
            while (isActive) {
                delay(1000)
                group.color(random.nextInt(0, Int.MAX_VALUE))
            }
        }
    }

    demo("rotation", "Rotations") {
        box(100, 100, 100)
        group {
            x = 200
            rotationY = PI / 4
            box(100, 100, 100) {
                rotationZ = PI / 4
                color(Colors.red)
            }
        }
    }

    demo("extrude", "extruded shape") {
        extrude {
            shape {
                polygon(8, 50)
            }
            for (i in 0..100) {
                layer(i * 5, 20 * sin(2 * PI / 100 * i), 20 * cos(2 * PI / 100 * i))
            }
            color(Colors.teal)
            rotationX = -PI / 2
        }
    }

    demo("lines", "Track / line segments") {
        sphere(100) {
            detail = 32
            opacity = 0.4
            color(Colors.blue)
        }
        repeat(20) {
            polyline(Point3D(100, 100, 100), Point3D(-100, -100, -100)) {
                thickness = 3.0
                rotationX = it * PI2 / 20
                color(Colors.green)
                //rotationY = it * PI2 / 20
            }
        }
    }

    demo("text", "Box with a label") {
        box(100, 100, 50) {
            opacity = 0.3
        }
        label("Hello, world!", fontSize = 12) {
            z = 26
        }
    }
}

fun OutputManager.showcaseCSG() {
    demo("CSG.simple", "CSG operations") {
        composite(CompositeType.UNION) {
            box(100, 100, 100) {
                z = 50
            }
            sphere(50)
            material {
                color(Colors.lightgreen)
                opacity = 0.3f
            }
        }
        composite(CompositeType.INTERSECT) {
            y = 300
            box(100, 100, 100) {
                z = 50
            }
            sphere(50)
            color(Colors.red)
        }
        composite(CompositeType.SUBTRACT) {
            y = -300
            box(100, 100, 100) {
                z = 50
            }
            sphere(50)
            color(Colors.blue)
        }
    }

    demo("CSG.custom", "CSG with manually created object") {
        intersect {
            tube(60, 10) {
                detail = 64
            }
            box(100, 100, 100)
        }
    }
}