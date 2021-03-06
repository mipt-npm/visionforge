package space.kscience.visionforge.solid.three

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.objects.Mesh
import space.kscience.dataforge.names.cutFirst
import space.kscience.dataforge.names.firstOrNull
import space.kscience.dataforge.names.toName
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidReferenceGroup
import space.kscience.visionforge.solid.SolidReferenceGroup.Companion.REFERENCE_CHILD_PROPERTY_PREFIX
import kotlin.reflect.KClass

public object ThreeReferenceFactory : ThreeFactory<SolidReferenceGroup> {
    private val cache = HashMap<Solid, Object3D>()

    override val type: KClass<SolidReferenceGroup> = SolidReferenceGroup::class

    private fun Object3D.replicate(): Object3D {
        return when (this) {
            is Mesh -> Mesh(geometry as BufferGeometry, material).also {
                it.applyMatrix4(matrix)
            }
            else -> clone(false)
        }.also { obj: Object3D ->
            obj.name = this.name
            children.forEach { child: Object3D ->
                obj.add(child.replicate())
            }
        }
    }

    override fun invoke(three: ThreePlugin, obj: SolidReferenceGroup): Object3D {
        val template = obj.prototype
        val cachedObject = cache.getOrPut(template) {
            three.buildObject3D(template)
        }

        val object3D: Object3D = cachedObject.replicate()
        object3D.updatePosition(obj)

        if(object3D is Mesh){
            //object3D.material = ThreeMaterials.buildMaterial(obj.getProperty(SolidMaterial.MATERIAL_KEY).node!!)
            object3D.applyProperties(obj)
        }

        //TODO apply child properties

        obj.onPropertyChange(three.updateScope) { name->
            if (name.firstOrNull()?.body == REFERENCE_CHILD_PROPERTY_PREFIX) {
                val childName = name.firstOrNull()?.index?.toName() ?: error("Wrong syntax for reference child property: '$name'")
                val propertyName = name.cutFirst()
                val referenceChild = obj[childName] ?: error("Reference child with name '$childName' not found")
                val child = object3D.findChild(childName) ?: error("Object child with name '$childName' not found")
                child.updateProperty(referenceChild, propertyName)
            } else {
                object3D.updateProperty(obj, name)
            }
        }


        return object3D
    }
}