package genome

import neat.Neat
import sun.font.TrueTypeFont

class ConnectionGene constructor(override var innovationNumber: Int, var fromGene: NodeGene, var toGene: NodeGene): Gene(innovationNumber)  {
    var weight = 0.0
    var enabled = true
    var replaceIndex = 0

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is NodeGene)
            return false
        val otherNode = other as ConnectionGene
        return (this.fromGene == other.fromGene) && (this.toGene == other.toGene)
    }

    override fun hashCode(): Int {
        return this.fromGene.innovationNumber * Neat.maxNodes + this.toGene.innovationNumber
    }
}