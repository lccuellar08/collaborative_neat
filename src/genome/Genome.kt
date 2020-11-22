package genome

import com.sun.tools.javah.Gen
import neat.Neat
import utils.RandomHashSet
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class Genome(var neat: Neat){
    var connections = RandomHashSet<ConnectionGene>()
    var nodes = RandomHashSet<NodeGene>()

    companion object {
        fun crossover(genome1: Genome, genome2: Genome): Genome {
            val neat = genome1.neat
            var genome: Genome = neat.emptyGenome()
            var index_g1 = 0
            var index_g2 = 0

            while((index_g1 < genome1.connections.size()) && (index_g2 < genome2.connections.size())) {
                val connGene1 = genome1.connections.get(index_g1)
                val connGene2 = genome2.connections.get(index_g2)

                val innov1 = connGene1?.innovationNumber
                val innov2 = connGene2?.innovationNumber

                if(innov1 == innov2) {
                    //Case 1: Similar Genes
                    // Choose randomly which connection we should get
                    val r = Random.nextDouble()
                    if(r > 0.5)
                        genome.connections.add(neat.copyConnection(connGene1))
                    else
                        genome.connections.add(neat.copyConnection(connGene2))
                    index_g1 += 1
                    index_g2 += 1
                }
                else if (innov1 != null) {
                    if(innov1 > innov2!!) {
                        // Case 2: Disjoint gene of g2
                        // No need to add disjoint genes of the second genome
                        index_g2 += 1
                    } else {
                        // Case 3: Disjoint gene of g1
                        index_g1 += 1
                        genome.connections.add(neat.copyConnection(connGene1))
                    }
                }
            }

            while(index_g1 < genome1.connections.size()) {
                val connGene1 = genome1.connections.get(index_g1)
                genome.connections.add(neat.copyConnection(connGene1))
                index_g1 += 1
            }

            for(connGene in genome.connections.data) {
                genome.nodes.add(connGene.fromGene)
                genome.nodes.add(connGene.toGene)
            }

            return genome
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is NodeGene)
            return false
        val otherGenome = other as Genome

        var equalFlag = true
        for(conn in connections.data) {
            if(!otherGenome.connections.contains(conn)) {
                equalFlag = false
                break
            }
        }
        if(equalFlag) {
            for(node in nodes.data) {
                if(!otherGenome.nodes.contains(node))
                    return false
            }
        }
        return equalFlag
    }

    fun distance(genome2: Genome): Double {
        var genome1 = this

        // We must ensure that genome1 has a higher innovation number than genome2:
        var maxInnov1 = 0
        var maxInnov2 = 0
        if(genome1.connections.size() > 0)
            maxInnov1 = genome1.connections.get(genome1.connections.size() - 1)?.innovationNumber ?: 0
        if(genome2.connections.size() > 0)
            maxInnov2 = genome2.connections.get(genome2.connections.size() - 1)?.innovationNumber ?: 0

        // Classic Switcharoo
        if(maxInnov1 < maxInnov2) {
            // Switch
            val tempGenome = genome1
            genome1 = genome2
            genome1 = tempGenome
        }

        var index_g1 = 0
        var index_g2 = 0

        var numDisjoints = 0
        var numExcess = 0
        var numSimilar = 0
        var totalWeightDifference = 0.0

        while((index_g1 < genome1.connections.size()) && (index_g2 < genome2.connections.size())) {
            val connGene1 = genome1.connections.get(index_g1)
            val connGene2 = genome2.connections.get(index_g2)

            val innov1 = connGene1?.innovationNumber
            val innov2 = connGene2?.innovationNumber

            if(innov1 == innov2 && (innov1 != null && innov2 != null)) {
                //Case 1: Similar Genes
                numSimilar += 1
                val weightDiff = abs(connGene1.weight - connGene2.weight)
                totalWeightDifference += weightDiff
                index_g1 += 1
                index_g2 += 1
            }
            else if (innov1 != null) {
                if(innov1 > innov2!!) {
                    // Case 2: Disjoint gene of g2
                    // No need to add disjoint genes of the second genome
                    numDisjoints += 1
                    index_g2 += 1
                } else {
                    // Case 3: Disjoint gene of g1
                    index_g1 += 1
                    numDisjoints += 1
                }
            }
        }

        // Given that genom1 MUST have a higher innovation number than genome2
        // The excess genes exist in genome 1
        numExcess = genome1.connections.size() - index_g1

        // Scaled weight difference:
        var scaledWeightDiff = 0.0
        if(numSimilar != 0)
            scaledWeightDiff = totalWeightDifference / numSimilar

        // N: Constant: Max number of connections between genome1 and genome2
        // According to the paper: If N can be set to 1, if N < 20

        var N = max(genome1.connections.size(), genome2.connections.size())
        if(N < 20)
            N = 1

        return neat.c1 * (numDisjoints / N) + neat.c2 * (numExcess / N) + neat.c3 * scaledWeightDiff
    }

    fun mutate() {
        if(this.neat.probabilityMutateLink > Random.nextDouble())
            this.mutateLink()
        if(this.neat.probabilityMutateNode > Random.nextDouble())
            this.mutateNode()
        if(this.neat.probabilityMutateWeightShift > Random.nextDouble())
            this.mutateWeightShift()
        if(this.neat.probabilityMutateWeightRandom > Random.nextDouble())
            this.mutateWeightRandom()
        if(this.neat.probabilityMutateToggleLink > Random.nextDouble())
            this.mutateLinkToggle()
        if(this.neat.probabilityMutateActivation > Random.nextDouble())
            this.mutateActivation()
    }

    fun mutateLink() {
        // We need 2 nodes, n1 and n2 where n1.x > n2.x
        // 100 tries
        for(i in (0 until 100)) {
            val n1 = this.nodes.randomElement()
            val n2 = this.nodes.randomElement()
            if(n1 == null || n2 == null)
                continue
            if(n1.x == n2.x)
                continue

            // Don't add innovation number yet
            var connGene = {
                if(n1.x < n2.x)
                    ConnectionGene(0, n1, n2)
                else
                    ConnectionGene(0, n2,n1)
            }.invoke()
            if(this.connections.contains(connGene))
                continue
            connGene = this.neat.getConnection(connGene.fromGene, connGene.toGene)
            connGene.weight = (Random.nextDouble() * 2 - 1) * this.neat.weightShiftStrength
            this.connections.addSorted(connGene)
            //println(this.connections.size())
            return
        }
    }

    fun mutateNode() {
        // Choose a random conncetion, replace it with 2 new connections and a node in the middle
        var connGene = this.connections.randomElement() ?: return
        var fromGene = connGene.fromGene
        var toGene = connGene.toGene
        var middleGene = NodeGene(-1)
        var replaceIndex = this.neat.getReplaceIndex(fromGene, toGene)

        if(replaceIndex == 0) {
            middleGene = this.neat.getNewNode()

            // Set x value to the middle between from and to
            middleGene.x = (fromGene.x + toGene.x) / 2
            middleGene.y = (fromGene.y + toGene.y) / 2

            this.neat.setReplaceIndex(fromGene, toGene, middleGene.innovationNumber)
        }
        else {
            middleGene = this.neat.getNode(replaceIndex)
        }

        val newConn1 = this.neat.getConnection(fromGene, middleGene)
        val newConn2 = this.neat.getConnection(middleGene, toGene)

        newConn1.weight = 1.0
        newConn2.weight = connGene.weight
        newConn2.enabled = connGene.enabled

        this.connections.remove(connGene)
        this.connections.add(newConn1)
        this.connections.add(newConn2)
        this.nodes.add(middleGene)
    }

    fun mutateWeightShift() {
        // Let's mutate a random connection
        val connGene = this.connections.randomElement() ?: return
        val weightShift = (Random.nextDouble() * 2 - 1) * this.neat.weightShiftStrength
        connGene.weight += weightShift
    }

    fun mutateWeightRandom() {
        val connGene = this.connections.randomElement() ?: return
        val weightShift = (Random.nextDouble() * 2 - 1) * this.neat.weightRandomStrength
        connGene.weight = weightShift
    }

    fun mutateLinkToggle() {
        val connGene = this.connections.randomElement() ?: return
        connGene.enabled = !connGene.enabled
    }

    fun mutateActivation() {
        val nodeGene = this.nodes.randomElement() ?: return
        nodeGene.hasActivation = !nodeGene.hasActivation
    }

    fun printGenome() {
        println("Genome: $this")
        for(node in this.nodes.data)
            println("[${node.innovationNumber}]")
        for(connection in this.connections.data)
            //s = f"[{connection.from_gene.innovation_number}] -{connection.weight}-> [{connection.to_gene.innovation_number}]"
            println("[${connection.fromGene.innovationNumber}] -${connection.weight}-> [${connection.toGene.innovationNumber}]")
    }
}