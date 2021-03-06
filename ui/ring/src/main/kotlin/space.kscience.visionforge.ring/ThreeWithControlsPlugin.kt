package space.kscience.visionforge.ring

import org.w3c.dom.Element
import react.child
import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.ElementVisionRenderer
import space.kscience.visionforge.Vision
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.three.ThreePlugin
import kotlin.reflect.KClass

public class ThreeWithControlsPlugin : AbstractPlugin(), ElementVisionRenderer {
    public val three: ThreePlugin by require(ThreePlugin)

    override val tag: PluginTag get() = Companion.tag

    override fun rateVision(vision: Vision): Int =
        if (vision is Solid) ElementVisionRenderer.DEFAULT_RATING * 2 else ElementVisionRenderer.ZERO_RATING

    override fun render(element: Element, vision: Vision, meta: Meta) {
        react.dom.render(element) {
            child(ThreeCanvasWithControls) {
                attrs {
                    this.context = this@ThreeWithControlsPlugin.context
                    this.solid = vision as? Solid
                }
            }
        }
    }

    override fun content(target: String): Map<Name, Any> {
        return when (target) {
            ElementVisionRenderer.TYPE -> mapOf("three.withControls".asName() to this)
            else -> super.content(target)
        }
    }

    public companion object : PluginFactory<ThreeWithControlsPlugin> {
        override val tag: PluginTag = PluginTag("vision.threejs.withControls", PluginTag.DATAFORGE_GROUP)
        override val type: KClass<ThreeWithControlsPlugin> = ThreeWithControlsPlugin::class
        override fun invoke(meta: Meta, context: Context): ThreeWithControlsPlugin = ThreeWithControlsPlugin()
    }
}