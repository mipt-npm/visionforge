@file:UseSerializers(MetaSerializer::class)

package hep.dataforge.vis.common

import hep.dataforge.io.serialization.MetaSerializer
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers

@Serializable
class StyleSheet() {
    @Transient
    internal var owner: VisualObject? = null

    constructor(owner: VisualObject) : this() {
        this.owner = owner
    }

    private val styleMap = HashMap<String, Meta>()

    val items: Map<String, Meta> get() = styleMap

    operator fun get(key: String): Meta? {
        return styleMap[key] ?: (owner?.parent as? VisualGroup)?.styleSheet?.get(key)
    }

    operator fun set(key: String, style: Meta?) {
        val oldStyle = styleMap[key]
        if (style == null) {
            styleMap.remove(key)
        } else {
            styleMap[key] = style
        }
        owner?.styleChanged(key, oldStyle, style)
    }

    infix fun String.put(style: Meta?) {
        set(this, style)
    }

    operator fun set(key: String, builder: MetaBuilder.() -> Unit) {
        val newStyle = get(key)?.let { buildMeta(it, builder) } ?: buildMeta(builder)
        set(key, newStyle.seal())
    }
}

private fun VisualObject.styleChanged(key: String, oldStyle: Meta?, newStyle: Meta?) {
    if (styles.contains(key)) {
        //TODO optimize set concatenation
        val tokens: Collection<Name> = ((oldStyle?.items?.keys ?: emptySet()) + (newStyle?.items?.keys ?: emptySet()))
            .map { it.asName() }
        tokens.forEach { parent?.propertyChanged(it, oldStyle?.get(it), newStyle?.get(it)) }
    }
    if (this is VisualGroup) {
        this.forEach { it.styleChanged(key, oldStyle, newStyle) }
    }
}