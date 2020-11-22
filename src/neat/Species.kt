package neat

import genome.Genome
import utils.RandomHashSet

class Species(var representative: Client) {
    var clients = RandomHashSet<Client>()
    var score = 0.0
    init {
        this.representative.species = this
    }

    fun put(client: Client): Boolean {
        if(representative.genome == null)
            return false
        else {
            if(client.distance(this.representative) < this.representative.genome!!.neat.cp) {
                client.species = this
                this.clients.add(client)
                return true
            } else
                return false
        }
    }

    fun forcePut(client: Client) {
        client.species = this
        this.clients.add(client)
    }

    fun goExtinct() {
        for(client in this.clients.data)
            client.species = null
    }

    fun evaluateScore() {
        if(this.size() == 0)
            return
        var addedScore = 0.0
        for(client in this.clients.data)
            addedScore += client.score
        this.score = addedScore / this.size()
    }

    fun reset() {
        this.representative = this.clients.randomElement() ?: Client()
        for(client in this.clients.data)
            client.species = null
        this.clients.clear()
        this.clients.add(this.representative)
        this.representative.species = this
        this.score = 0.0
    }

    fun kill(percentage: Double) {
        this.clients.data.sort()
        val ammount: Int = (percentage * this.clients.size()).toInt()
        for(i in (0 until ammount)) {
            this.clients.get(i)?.species = null
            this.clients.remove(i)
        }
    }

    fun breed(): Genome {
        val client1: Client = this.clients.randomElement() ?: representative
        val client2: Client = this.clients.randomElement() ?: representative

        val client1Score = client1.score
        val client2Score = client2.score
        if(client1.genome != null && client2.genome != null) {
            if (client1Score > client2Score)
                return Genome.crossover(client1.genome!!, client2.genome!!)
            else
                return Genome.crossover(client2.genome!!, client1.genome!!)
        }
        else
            throw Exception()
    }

    fun size(): Int {
        return this.clients.size()
    }
}