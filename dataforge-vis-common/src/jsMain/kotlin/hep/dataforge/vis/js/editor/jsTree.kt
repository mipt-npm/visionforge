package hep.dataforge.vis.js.editor

import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.plus
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.isEmpty
import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import kotlinx.html.js.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import kotlin.dom.clear

fun Element.objectTree(
    token: NameToken,
    obj: VisualObject,
    clickCallback: (Name, VisualObject) -> Unit = {_,_->}
) {
    clear()
    append {
        card("Object tree") {
            subTree(Name.EMPTY, token, obj, clickCallback)
        }
    }
}

private fun TagConsumer<HTMLElement>.subTree(
    parentName: Name,
    token: NameToken,
    obj: VisualObject,
    clickCallback: (Name, VisualObject) -> Unit
) {
    val fullName = parentName + token

    //display as node if any child is visible
    if (obj is VisualGroup && obj.children.keys.any { !it.body.startsWith("@") }) {
        lateinit var toggle: HTMLSpanElement
        div("d-inline-block text-truncate") {
            toggle = span("objTree-caret")
            label("objTree-label") {
                +token.toString()
                onClickFunction = { clickCallback(fullName, obj) }
            }
        }
        val subtree = ul("objTree-subtree")
        toggle.onclick = {
            toggle.classList.toggle("objTree-caret-down")
            subtree.apply {
                //If expanded, add children dynamically
                if (toggle.classList.contains("objTree-caret-down")) {
                    obj.children.entries
                        .filter { !it.key.toString().startsWith("@") } // ignore statics and other hidden children
                        .sortedBy { (it.value as? VisualGroup)?.isEmpty ?: true }
                        .forEach { (childToken, child) ->
                            append {
                                li().apply {
                                    subTree(fullName, childToken, child, clickCallback)
                                }
                            }
                        }
                } else {
                    // if not, clear them to conserve memory on very long lists
                    this.clear()
                }
            }
        }
    } else {
        val div = div("d-inline-block text-truncate") {
            span("objTree-leaf")
            label("objTree-label") {
                +token.toString()
                onClickFunction = { clickCallback(fullName, obj) }
            }
        }
    }
}

