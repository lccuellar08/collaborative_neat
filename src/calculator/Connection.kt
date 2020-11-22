package calculator

class Connection(var fromGene: Node, var toGene: Node) {
    var weight = 0.0
    var enabled = true

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is Connection)
            return false
        val otherGenome = other as Connection
        return (this.fromGene == other.fromGene) && (this.toGene == other.toGene)
    }
}