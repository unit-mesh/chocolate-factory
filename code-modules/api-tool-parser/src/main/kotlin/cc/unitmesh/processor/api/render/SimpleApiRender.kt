package cc.unitmesh.processor.api.render

import cc.unitmesh.processor.api.base.ApiCollection
import cc.unitmesh.processor.api.base.ApiDetailRender
import cc.unitmesh.processor.api.base.ApiItem

class SimpleApiRender : ApiDetailRender {
    override fun renderCollection(collection: ApiCollection): String {
        val items = collection.items.joinToString("\n") {
            "${it.method} ${it.path} ${operationInformation(it)} "
        }

        return "${collection.name}\n$items"
    }

    private fun operationInformation(it: ApiItem): String {
        if (it.operationId.isEmpty()) return ""

        return " ${it.operationId}${ioParameters(it)}"
    }

    private fun ioParameters(details: ApiItem): String {
        val inputs = details.request.toString()
        val outputs = details.response.toString()
        if (inputs.isEmpty() && outputs.isEmpty()) return "()"
        if (inputs.isEmpty()) return "(): $outputs"
        if (outputs.isEmpty()) return "($inputs)"

        return "($inputs) : $outputs"
    }
}
