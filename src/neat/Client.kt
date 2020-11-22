package neat

import calculator.Calculator
import genome.Genome
import java.io.File

class Client : Comparable<Client>{
    var genome: Genome? = null
    var score = 0.0
    var species: Species? = null
    var calculator: Calculator? = null

    fun distance(otherClient: Client): Double {
        if(this.genome != null && otherClient.genome != null)
            return this.genome!!.distance(otherClient.genome!!)
        return 0.0
    }

    fun mutate() {
        this.genome?.mutate()
    }
    fun generateCalculator() {
        this.calculator = this.genome?.let { Calculator(it) }
    }
    fun calculate(input_data: DoubleArray): DoubleArray? {
        return this.calculator?.calculate(input_data)
    }

    fun toFile(filePath: String) {
        var txt = ""

        val file = File("$filePath$this.txt")
        txt += "Genome: ${this.genome}\n"
        for(node in this.genome?.nodes?.data!!)
            txt += "\t[${node.innovationNumber}]\n"
        for(connection in this?.genome?.connections?.data!!)
            txt += "\t\t[${connection.fromGene.innovationNumber}] -${connection.weight}-> [${connection.toGene.innovationNumber}]\n"

        file.writeText(txt)
    }

    override operator fun compareTo(other: Client): Int {
        if(this.score == other.score)
            return 0
        if(this.score > other.score)
            return 1
        if(this.score < other.score)
            return -1
        return 0
    }
}