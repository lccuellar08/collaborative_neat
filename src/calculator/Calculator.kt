package calculator

import genome.Genome

class Calculator(var genome: Genome){
    var inputNodes = mutableListOf<Node>()
    var hiddenNodes = mutableListOf<Node>()
    var outputNodes = mutableListOf<Node>()

    init {
        val nodes = this.genome.nodes
        val conns = this.genome.connections
        val nodeHashMap: MutableMap<Int, Node> = mutableMapOf()

        for(nodeGene in nodes.data) {
            val node = Node(nodeGene.x)
            node.hasActivation = nodeGene.hasActivation
            nodeHashMap[nodeGene.innovationNumber] = node

            if(nodeGene.x <= 0.1)
                this.inputNodes.add(node)
            else if(nodeGene.x >= 0.9)
                this.outputNodes.add(node)
            else
                this.hiddenNodes.add(node)
        }
        this.hiddenNodes.sort()

        for(connectionGene in conns.data) {
            val fromNodeGene = connectionGene.fromGene
            val toNodeGene = connectionGene.toGene

            val nodeFrom = nodeHashMap[fromNodeGene.innovationNumber]
            val nodeTo = nodeHashMap[toNodeGene.innovationNumber]

            if(nodeFrom != null && nodeTo != null) {
                val conn = Connection(nodeFrom, nodeTo)
                conn.weight = connectionGene.weight
                conn.enabled = connectionGene.enabled
                nodeTo.connections.add(conn)
            }
        }
    }

    fun calculate(inputData: DoubleArray): DoubleArray {
        if(inputData.size != this.inputNodes.size)
            throw Exception("Data Doesn't Fit")

        var outputData = DoubleArray(outputNodes.size)
        for(i in (0 until this.inputNodes.size))
            this.inputNodes[i].output = inputData[i]

        for(hiddenNode in this.hiddenNodes)
            hiddenNode.calculate()

        for(i in (0 until this.outputNodes.size)) {
            this.outputNodes[i].calculate()
            outputData[i] = this.outputNodes[i].output
        }
        return outputData
    }
}