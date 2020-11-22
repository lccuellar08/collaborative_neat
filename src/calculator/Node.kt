package calculator

import kotlin.math.exp

class Node(var x: Double): Comparable<Node>{
    var output = 0.0
    var hasActivation = true
    var connections = mutableListOf<Connection>()

    fun calculate() {
        var totalSum = 0.0
        for(connection in connections) {
            if(connection.enabled)
                totalSum += connection.weight * connection.fromGene.output
        }
        if(hasActivation)
            this.output = this.activation(totalSum)
        else
            this.output = totalSum
    }

    fun activation(x: Double): Double {
        return 1.0 / (1.0 + exp(-x))
    }

    override operator fun compareTo(other: Node): Int {
        if(this.x == other.x)
            return 0
        if(this.x > other.x)
            return 1
        if(this.x < other.x)
            return -1
        return 0
    }
}