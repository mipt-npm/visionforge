package space.kscience.visionforge.solid.three

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Face3
import info.laht.threekt.core.Geometry
import info.laht.threekt.math.Vector3
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.visionforge.solid.GeometryBuilder
import space.kscience.visionforge.solid.Point3D

internal fun Point3D.toVector() = Vector3(x, y, z)

/**
 * An implementation of geometry builder for Three.js [BufferGeometry]
 */
public class ThreeGeometryBuilder : GeometryBuilder<BufferGeometry> {

    private val vertices = ArrayList<Vector3>()
    private val faces = ArrayList<Face3>()

    private val vertexCache = HashMap<Point3D, Int>()

    private fun append(vertex: Point3D): Int {
        val index = vertexCache[vertex] ?: -1//vertices.indexOf(vertex)
        return if (index > 0) {
            index
        } else {
            vertices.add(vertex.toVector())
            vertexCache[vertex] = vertices.size - 1
            vertices.size - 1
        }
    }

    override fun face(vertex1: Point3D, vertex2: Point3D, vertex3: Point3D, normal: Point3D?, meta: Meta) {
        val face = Face3(append(vertex1), append(vertex2), append(vertex3), normal?.toVector() ?: Vector3(0, 0, 0))
        meta["materialIndex"].int?.let { face.materialIndex = it }
        meta["color"]?.getColor()?.let { face.color = it }
        faces.add(face)
    }


    override fun build(): BufferGeometry {
        return Geometry().apply {
            vertices = this@ThreeGeometryBuilder.vertices.toTypedArray()
            faces = this@ThreeGeometryBuilder.faces.toTypedArray()
            computeBoundingSphere()
            computeFaceNormals()
        }.toBufferGeometry()
    }
}