package neat

import genome.ConnectionGene
import genome.Genome
import genome.NodeGene
import utils.RandomHashSet
import utils.RandomSelector
import kotlin.math.pow
import kotlin.random.Random

class Neat constructor(var inputSize: Int, var outputSize: Int, var maxClients: Int){
    companion object {
        val maxNodes = (2.0.pow(20.0)).toInt()
    }

    val c1 = 1.5
    val c2 = 1
    val c3 = 0.5
    val cp = 1.8

    val survivors = 0.3

    val weightShiftStrength = 0.3
    val weightRandomStrength = 1

    val probabilityMutateLink = 0.3
    val probabilityMutateNode = 0.1
    val probabilityMutateWeightShift = 0.02
    val probabilityMutateWeightRandom = 0.02
    val probabilityMutateToggleLink = 0.01
    val probabilityMutateActivation = 0.1

    var allConnections: MutableMap<ConnectionGene, ConnectionGene> = mutableMapOf()
    var allNodes = RandomHashSet<NodeGene>()

    var clients = RandomHashSet<Client>()
    var species = RandomHashSet<Species>()

    init {
        initNodes()
        initClients()
    }

    fun initClients() {
        for(i in (0 until this.maxClients)) {
            val client = Client()
            client.genome = this.emptyGenome()
            client.generateCalculator()

            this.clients.add(client)
        }
    }

    fun initNodes() {
        // Initialize input node_genes with x value of 0.1
        for(i in (0 until inputSize)) {
            val n: NodeGene = this.getNewNode()
            n.x = 0.1
            n.y = (i + 1.0) / (inputSize + 1.0)
        }

        // Initialize output node_genes with x value of 0.9
        for(i in (0 until outputSize)) {
            val n: NodeGene = this.getNewNode()
            n.x = 0.9
            n.y = (i + 1.0) / (inputSize + 1.0)
        }
    }

    fun reset(inputSize: Int, outputSize: Int, maxClients: Int) {
        this.inputSize = inputSize
        this.outputSize = outputSize
        this.maxClients = maxClients

        this.allConnections.clear()
        this.allNodes.clear()

        this.initNodes()
        this.clients.clear()
        this.initClients()
    }

    fun getClient(i: Int): Client {
        val c = this.clients.get(i)
        if(c != null)
            return c
        else
            return Client()
    }

    fun setReplaceIndex(node1: NodeGene, node2: NodeGene, i: Int) {
        val conn = allConnections[ConnectionGene(0,node1, node2)] ?: return
        conn.replaceIndex = i
    }

    fun getReplaceIndex(node1: NodeGene, node2: NodeGene): Int {
        val conn = ConnectionGene(0, node1, node2)
        val connData = allConnections[conn] ?: return 0
        return connData.replaceIndex
    }

    fun emptyGenome(): Genome {
        val genome = Genome(this)
        for(i in (0 until (inputSize + outputSize)))
            genome.nodes.add(getNode(i + 1))
        return genome
    }

    // This method does 3 things:
    //	1: Create new Node node with a new innovation number
    //	2: Add it to the all_nodes list
    //	3: Return the new node
    fun getNewNode(): NodeGene {
        val innovNumber = allNodes.size() + 1
        val newNode = NodeGene(innovNumber)
        allNodes.add(newNode)
        return newNode
    }

    fun getNode(i: Int): NodeGene {
        if(i <= allNodes.size())
            return allNodes.get(i - 1) ?: getNewNode()
        return getNewNode()
    }

    fun copyConnection(connGene: ConnectionGene?): ConnectionGene {
        if(connGene == null)
            return ConnectionGene(1, NodeGene(1), NodeGene(2))
        val connCopy = ConnectionGene(connGene.innovationNumber, connGene.fromGene, connGene.toGene)
        connCopy.weight = connGene.weight
        connCopy.enabled = connCopy.enabled
        return connCopy
    }

    fun getConnection(node1: NodeGene, node2: NodeGene): ConnectionGene {
        val connGene = ConnectionGene(0, fromGene = node1, toGene = node2)
        if(allConnections.containsKey(connGene)) {
            connGene.innovationNumber = allConnections[connGene]?.innovationNumber ?: 0
        }
        else
            connGene.innovationNumber = allConnections.size + 1
        return connGene
    }

    fun evolve() {
        this.generateSpecies()
        this.killPercentage()
        this.removeExtinctSpecies()
        this.reproduce()
        this.mutate()
        for(client in this.clients.data)
            client.generateCalculator()
    }

    fun generateSpecies() {
        for(species in this.species.data)
            species.reset()

        for(client in this.clients.data) {
            if(client.species != null)
                this.species.add(client.species!!)
            var foundSpeciesFlag = false
            for(species in this.species.data) {
                if(species.put(client)) {
                    foundSpeciesFlag = true
                    break
                }
            }
            if(!foundSpeciesFlag) {
                println("Creating new species")
                this.species.add(Species(representative = client))
            }
        }

        for(species in this.species.data)
            species.evaluateScore()
    }

    fun killPercentage() {
        for(species in this.species.data)
            species.kill(1 - this.survivors)
    }

    fun removeExtinctSpecies() {
        val lenSpecies = this.species.size() - 1
        for(i in lenSpecies downTo 0) {
            if(this.species.get(i)!!.size() <= 1) {
                val specie = this.species.get(i) ?: continue
                specie.goExtinct()
                this.species.remove(specie)
            }
        }
    }

    fun reproduce() {
        val selector = RandomSelector<Species>()
        for(species in this.species.data)
            selector.add(species, species.score)
        for(client in this.clients.data) {
            if(client.species == null) {
                val s = selector.random() ?: continue
                client.genome = s.breed()
                s.forcePut(client)
            }
        }
    }

    fun mutate() {
        for(client in this.clients.data)
            client.mutate()
    }

    fun printSpecies() {
        println("#####################################################")
        println("Num of Species: ${species.size()}")
        for(species in this.species.data) {
            println(species.toString() + "   " + species.score.toString() + "     " + species.size().toString())
            //species.representative.genome?.printGenome()
        }
    }
}

fun main(args: Array<String>) {
    val neat = Neat(inputSize = 10, outputSize = 10, maxClients = 1000)
    var inputData = DoubleArray(10)
    for(i in 0 until 10)
        inputData[i] = Random.nextDouble()
    val generations = 100
    for(i in 0 until generations) {
        for(client in neat.clients.data) {
            val score = client.calculate(inputData) ?: continue
            client.score = score[0]
        }
        neat.evolve()
        println("Generation: " + i.toString())
        neat.printSpecies()
    }
}

