package space.kscience.visionforge.ring

import kotlinx.css.*
import react.*
import react.dom.div
import react.dom.span
import ringui.Island
import ringui.IslandContent
import ringui.IslandHeader
import ringui.Link
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.isEmpty
import space.kscience.dataforge.names.length
import space.kscience.visionforge.VisionGroup
import space.kscience.visionforge.allProperties
import space.kscience.visionforge.ownProperties
import space.kscience.visionforge.react.ThreeCanvasComponent
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.react.flexRow
import space.kscience.visionforge.react.propertyEditor
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import styled.css
import styled.styledDiv

public external interface ThreeCanvasWithControlsProps : RProps {
    public var context: Context
    public var solid: Solid?
    public var selected: Name?
    public var additionalTabs: Map<String, RBuilder.() -> Unit>?
}

public fun ThreeCanvasWithControlsProps.tab(title: String, block: RBuilder.() -> Unit) {
    additionalTabs = (additionalTabs ?: emptyMap()) + (title to block)
}


public fun RBuilder.nameCrumbs(name: Name?, link: (Name) -> Unit): ReactElement = styledDiv {
    div {
        Link {
            attrs {
                onClick = {
                    link(Name.EMPTY)
                }
            }
            +"\u2302"
        }

        if (name != null) {
            val tokens = ArrayList<NameToken>(name.length)
            name.tokens.forEach { token ->
                tokens.add(token)
                val fullName = Name(tokens.toList())
                span { +"." }
                Link {
                    +token.toString()
                    attrs {
                        onClick = {
                            console.log("Selected = $fullName")
                            link(fullName)
                        }
                    }
                }
            }
        }
    }

}

@JsExport
public val ThreeCanvasWithControls: FunctionalComponent<ThreeCanvasWithControlsProps> =
    functionalComponent("ThreeViewWithControls") { props ->
        var selected by useState { props.selected }

        val onSelect: (Name?) -> Unit = {
            selected = it
        }

        val options = useMemo(props.context) {
            Canvas3DOptions.invoke {
                this.onSelect = onSelect
            }
        }

        val selectedVision = useMemo(selected) {
            selected?.let {
                when {
                    it.isEmpty() -> props.solid
                    else -> (props.solid as? VisionGroup)?.get(it)
                }
            }
        }

        flexRow {
            css {
                height = 100.pct
                flex(1.0, 1.0, FlexBasis.auto)
                flexWrap = FlexWrap.wrap
                alignItems = Align.stretch
                alignContent = Align.stretch
            }

            flexColumn {
                css {
                    minWidth = 600.px
                    flex(10.0, 1.0, FlexBasis("600px"))
                    position = Position.relative
                }

                child(ThreeCanvasComponent) {
                    attrs {
                        this.context = props.context
                        this.solid = props.solid
                        this.selected = selected
                        this.options = options
                    }
                }

                selectedVision?.let { vision ->
                    styledDiv {
                        css{
                            position = Position.absolute
                            top = 5.px
                            right = 5.px
                            width = 450.px
                        }
                        Island{
                            IslandHeader{
                                attrs {
                                    border = true
                                }
                                nameCrumbs(selected) { selected = it }
                            }
                            IslandContent{
                                propertyEditor(
                                    ownProperties = vision.ownProperties,
                                    allProperties = vision.allProperties(),
                                    updateFlow = vision.propertyChanges,
                                    descriptor = vision.descriptor,
                                    key = selected
                                )
                            }
                        }
                    }
                }
            }
            flexColumn {
                css {
                    padding(4.px)
                    minWidth = 400.px
                    flex(1.0, 10.0, FlexBasis("300px"))
                }
                ringThreeControls(options, props.solid, selected, onSelect, additionalTabs = props.additionalTabs)
            }
        }
    }
