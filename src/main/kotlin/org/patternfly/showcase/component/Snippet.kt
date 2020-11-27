package org.patternfly.showcase.component

import ClipboardJS
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.elemento.By
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.minusAssign
import dev.fritz2.elemento.plusAssign
import dev.fritz2.elemento.querySelector
import hljs.highlightBlock
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.patternfly.PageSection
import org.patternfly.Size
import org.patternfly.fas
import org.patternfly.icon
import org.patternfly.modifier
import org.patternfly.pageSection
import org.patternfly.pushButton
import org.patternfly.title
import org.patternfly.util
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

fun RenderContext.intro(
    title: String,
    prefix: String? = null,
    key: String,
    text: String,
    link: Pair<String, String>? = null
): PageSection = pageSection("pb-0".util()) {
    title(size = Size.XL_3, baseClass = "mb-md".util()) { +title }
    p {
        prefix?.let { +it }
        strong { +key }
        +text
        link?.let { (url, text) ->
            a {
                href(url)
                target("pf4")
                +text
            }
        }
    }
}

fun RenderContext.snippet(header: String, code: String, content: RenderContext.() -> Unit): Snippet =
    register(Snippet(header, code, job, content), {})

internal class CodeStore : RootStore<Boolean>(true) {
    val toggle = handle { hidden -> !hidden }
}

class Snippet(header: String, code: String, job: Job, val content: RenderContext.() -> Unit) :
    Tag<HTMLElement>(tagName = "section", baseClass = "sc-snippet", job = job) {
    private var timeoutHandle = -1
    private val codeStore = CodeStore()
    private val copiedId = Id.unique("code")

    init {
        title(level = 3, size = Size.LG, baseClass = "sc-snippet__title") { +header }
        div("sc-snippet__content") {
            this@Snippet.content(this)
        }
        div("sc-snippet__toolbar") {
            pushButton(baseClass = "plain".modifier()) {
                icon("code".fas())
                clicks handledBy this@Snippet.codeStore.toggle
            }
            pushButton(baseClass = "plain".modifier()) {
                icon("copy".fas())
                val clipboard = ClipboardJS(domNode, object : ClipboardJS.Options {
                    override val text: (Element) -> String = { code }
                })
                clipboard.on("success") {
                    this@Snippet.showCopied()
                }
            }
            div(baseClass = "display-none".util(), id = this@Snippet.copiedId) {
                span("sc-snippet__copied") { +"Copied to clipboard" }
            }
        }
        div(baseClass = "sc-snippet__code") {
            classMap(this@Snippet.codeStore.data.map { hidden ->
                mapOf("display-none".util() to hidden)
            })
            pre {
                code("kotlin") {
                    +code
                    highlightBlock(domNode)
                }
            }
        }
    }

    private fun showCopied() {
        window.clearTimeout(timeoutHandle)
        domNode.querySelector(By.id(copiedId))?.let {
            it.classList -= "display-none".util()
            timeoutHandle = window.setTimeout({
                it.classList += "display-none".util()
            }, 2000)
        }
    }
}
