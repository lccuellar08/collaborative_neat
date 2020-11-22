package genome

import javax.xml.soap.Node

class NodeGene constructor(override var innovationNumber: Int): Gene(innovationNumber) {
    var x = 0.0
    var y = 0.0
    var hasActivation = true

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is NodeGene)
            return false
        val otherNode = other as NodeGene
        return this.innovationNumber == otherNode.innovationNumber
    }

    override fun hashCode(): Int {
        return this.innovationNumber
    }
}