package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.values.ValueType
import space.kscience.dataforge.values.asValue
import space.kscience.visionforge.*
import space.kscience.visionforge.Vision.Companion.VISIBLE_KEY
import space.kscience.visionforge.solid.Solid.Companion.DETAIL_KEY
import space.kscience.visionforge.solid.Solid.Companion.IGNORE_KEY
import space.kscience.visionforge.solid.Solid.Companion.LAYER_KEY
import space.kscience.visionforge.solid.Solid.Companion.POSITION_KEY
import space.kscience.visionforge.solid.Solid.Companion.ROTATION_KEY
import space.kscience.visionforge.solid.Solid.Companion.SCALE_KEY
import space.kscience.visionforge.solid.Solid.Companion.X_KEY
import space.kscience.visionforge.solid.Solid.Companion.X_POSITION_KEY
import space.kscience.visionforge.solid.Solid.Companion.X_ROTATION_KEY
import space.kscience.visionforge.solid.Solid.Companion.X_SCALE_KEY
import space.kscience.visionforge.solid.Solid.Companion.Y_KEY
import space.kscience.visionforge.solid.Solid.Companion.Y_POSITION_KEY
import space.kscience.visionforge.solid.Solid.Companion.Y_ROTATION_KEY
import space.kscience.visionforge.solid.Solid.Companion.Y_SCALE_KEY
import space.kscience.visionforge.solid.Solid.Companion.Z_KEY
import space.kscience.visionforge.solid.Solid.Companion.Z_POSITION_KEY
import space.kscience.visionforge.solid.Solid.Companion.Z_ROTATION_KEY
import space.kscience.visionforge.solid.Solid.Companion.Z_SCALE_KEY
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Interface for 3-dimensional [Vision]
 */
public interface Solid : Vision {

    override val descriptor: NodeDescriptor get() = Companion.descriptor

    public companion object {
        //        val SELECTED_KEY = "selected".asName()
        public val DETAIL_KEY: Name = "detail".asName()
        public val LAYER_KEY: Name = "layer".asName()
        public val IGNORE_KEY: Name = "ignore".asName()

        public val GEOMETRY_KEY: Name = "geometry".asName()

        public val X_KEY: Name = "x".asName()
        public val Y_KEY: Name = "y".asName()
        public val Z_KEY: Name = "z".asName()

        public val POSITION_KEY: Name = "position".asName()

        public val X_POSITION_KEY: Name = POSITION_KEY + X_KEY
        public val Y_POSITION_KEY: Name = POSITION_KEY + Y_KEY
        public val Z_POSITION_KEY: Name = POSITION_KEY + Z_KEY

        public val ROTATION_KEY: Name = "rotation".asName()

        public val X_ROTATION_KEY: Name = ROTATION_KEY + X_KEY
        public val Y_ROTATION_KEY: Name = ROTATION_KEY + Y_KEY
        public val Z_ROTATION_KEY: Name = ROTATION_KEY + Z_KEY

        public val ROTATION_ORDER_KEY: Name = ROTATION_KEY + "order"

        public val SCALE_KEY: Name = "scale".asName()

        public val X_SCALE_KEY: Name = SCALE_KEY + X_KEY
        public val Y_SCALE_KEY: Name = SCALE_KEY + Y_KEY
        public val Z_SCALE_KEY: Name = SCALE_KEY + Z_KEY

        public val descriptor: NodeDescriptor by lazy {
            NodeDescriptor {
                value(VISIBLE_KEY) {
                    inherited = false
                    type(ValueType.BOOLEAN)
                    default(true)
                }

                //TODO replace by descriptor merge
                value(Vision.STYLE_KEY) {
                    type(ValueType.STRING)
                    multiple = true
                    hide()
                }

                node(POSITION_KEY){
                    hide()
                }

                node(ROTATION_KEY){
                    hide()
                }

                node(SCALE_KEY){
                    hide()
                }

                value(DETAIL_KEY) {
                    type(ValueType.NUMBER)
                    hide()
                }

                item(SolidMaterial.MATERIAL_KEY.toString(), SolidMaterial.descriptor)

                enum(ROTATION_ORDER_KEY, default = RotationOrder.XYZ) {
                    hide()
                }
            }
        }
    }
}

/**
 * Get the layer number this solid belongs to. Return 0 if layer is not defined.
 */
public var Solid.layer: Int
    get() = allProperties().getItem(LAYER_KEY).int ?: 0
    set(value) {
        setProperty(LAYER_KEY, value)
    }

// Common properties

public enum class RotationOrder {
    XYZ,
    YZX,
    ZXY,
    XZY,
    YXZ,
    ZYX
}

/**
 * Rotation order
 */
public var Solid.rotationOrder: RotationOrder
    get() = getProperty(Solid.ROTATION_ORDER_KEY).enum<RotationOrder>() ?: RotationOrder.XYZ
    set(value) = setProperty(Solid.ROTATION_ORDER_KEY, value.name.asValue())


/**
 * Preferred number of polygons for displaying the object. If not defined, uses shape or renderer default. Not inherited
 */
public var Solid.detail: Int?
    get() = getProperty(DETAIL_KEY, false).int
    set(value) = setProperty(DETAIL_KEY, value?.asValue())

/**
 * If this property is true, the object will be ignored on render.
 * Property is not inherited.
 */
public var Vision.ignore: Boolean?
    get() = getProperty(IGNORE_KEY, false).boolean
    set(value) = setProperty(IGNORE_KEY, value?.asValue())

//var VisualObject.selected: Boolean?
//    get() = getProperty(SELECTED_KEY).boolean
//    set(value) = setProperty(SELECTED_KEY, value)

internal fun float(name: Name, default: Number): ReadWriteProperty<Solid, Number> =
    object : ReadWriteProperty<Solid, Number> {
        override fun getValue(thisRef: Solid, property: KProperty<*>): Number {
            return thisRef.getOwnProperty(name)?.number ?: default
        }

        override fun setValue(thisRef: Solid, property: KProperty<*>, value: Number) {
            thisRef.setProperty(name, value)
        }
    }

internal fun point(name: Name, default: Float): ReadWriteProperty<Solid, Point3D?> =
    object : ReadWriteProperty<Solid, Point3D?> {
        override fun getValue(thisRef: Solid, property: KProperty<*>): Point3D? {
            val item = thisRef.getOwnProperty(name) ?: return null
            return object : Point3D {
                override val x: Float get() = item[X_KEY]?.float ?: default
                override val y: Float get() = item[Y_KEY]?.float ?: default
                override val z: Float get() = item[Z_KEY]?.float ?: default
            }
        }

        override fun setValue(thisRef: Solid, property: KProperty<*>, value: Point3D?) {
            if (value == null) {
                thisRef.setProperty(name, null)
            } else {
                thisRef.setProperty(name + X_KEY, value.x)
                thisRef.setProperty(name + Y_KEY, value.y)
                thisRef.setProperty(name + Z_KEY, value.z)
            }
        }
    }

public var Solid.position: Point3D? by point(POSITION_KEY, 0f)
public var Solid.rotation: Point3D? by point(ROTATION_KEY, 0f)
public var Solid.scale: Point3D? by point(SCALE_KEY, 1f)

public var Solid.x: Number by float(X_POSITION_KEY, 0f)
public var Solid.y: Number by float(Y_POSITION_KEY, 0f)
public var Solid.z: Number by float(Z_POSITION_KEY, 0f)

public var Solid.rotationX: Number by float(X_ROTATION_KEY, 0f)
public var Solid.rotationY: Number by float(Y_ROTATION_KEY, 0f)
public var Solid.rotationZ: Number by float(Z_ROTATION_KEY, 0f)

public var Solid.scaleX: Number by float(X_SCALE_KEY, 1f)
public var Solid.scaleY: Number by float(Y_SCALE_KEY, 1f)
public var Solid.scaleZ: Number by float(Z_SCALE_KEY, 1f)