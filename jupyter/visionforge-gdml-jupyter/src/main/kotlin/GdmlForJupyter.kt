package space.kscience.visionforge.gdml.jupyter

import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.annotations.JupyterLibrary
import org.jetbrains.kotlinx.jupyter.api.libraries.*
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.gdml.Gdml
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionForge
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.Page
import space.kscience.visionforge.html.embedVisionFragment
import space.kscience.visionforge.html.fragment
import space.kscience.visionforge.plugins
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.visionManager

@JupyterLibrary
@DFExperimental
internal class GdmlForJupyter : JupyterIntegration() {

    private val jsBundle = ResourceFallbacksBundle(listOf(
        ResourceLocation("js/visionforge-three.js", ResourcePathType.CLASSPATH_PATH))
    )
    private val jsResource = LibraryResource(name = "visionforge-three", type = ResourceType.JS, bundles = listOf(jsBundle))

    private var counter = 0

    private fun produceHtmlVisionString(fragment: HtmlVisionFragment) = createHTML().div {
        val id = "visionforge.vision[${counter++}]"
        div {
            this.id = id
            embedVisionFragment(VisionForge.visionManager, fragment = fragment)
        }
        script {
            type = "text/javascript"
            unsafe { +"window.renderVisionsAt(\"$id\");" }
        }
    }

    override fun Builder.onLoaded() {
        resource(jsResource)

        onLoaded {
            VisionForge.plugins.fetch(Solids)
        }

        import(
            "space.kscience.gdml.*",
            "kotlinx.html.*",
            "space.kscience.visionforge.solid.*",
            "space.kscience.visionforge.html.Page",
            "space.kscience.visionforge.html.page",
            "space.kscience.visionforge.gdml.jupyter.*"
        )

        import<VisionForge>()

        render<Gdml> { gdmlModel ->
            val fragment = VisionForge.fragment {
                vision(gdmlModel.toVision())
            }
            HTML(produceHtmlVisionString(fragment))
        }

        render<Vision> { vision ->
            val fragment = VisionForge.fragment {
                vision(vision)
            }

            HTML(produceHtmlVisionString(fragment))
        }

        render<Page> { page ->
            HTML(page.render(createHTML()), true)
        }
    }
}