package utils

import genome.Gene

class RandomHashSet<T> {
    var set: MutableSet<T> = mutableSetOf()
    var data: MutableList<T> = mutableListOf()

    fun contains(obj: T): Boolean {
        return set.contains(obj)
    }

    fun randomElement(): T? {
        if(this.size() == 0)
            return null
        val randIndex = (0 until this.size()).random()
        return this.get(randIndex)
    }

    fun size(): Int {
        return this.data.size
    }

    fun add(obj: T) {
        if(!this.contains(obj)) {
            this.set.add(obj)
            this.data.add(obj)
        }
    }

    fun addSorted(obj: T) {
        if(!this.contains(obj)) {
            for(i in (0 until this.size())) {
                if(obj is Gene) {
                    val gene = this.data[i] as Gene
                    val otherGene = obj as Gene
                    val innov = gene.innovationNumber
                    if(otherGene.innovationNumber < innov) {
                        this.data.add(i, obj)
                        this.set.add(obj)
                        return
                    }
                }
            }
            this.data.add(obj)
            this.set.add(obj)
        }
    }

    fun clear() {
        this.set.clear()
        this.data.clear()
    }

    fun get(i: Int): T? {
        if(i < 0 || i >= this.size())
            return null
        return this.data[i]
    }

    fun get(obj: T): T? {
        val objIndex = this.data.indexOf(obj)
        return this.get(objIndex)
    }

    fun remove(i: Int) {
        if(i < 0 || i >= this.size())
            return
        val obj = this.get(i)
        this.data.remove(obj)
        this.set.remove(obj)
    }

    fun remove(obj: T) {
        val objIndex = this.data.indexOf(obj)
        this.remove(objIndex)
    }
}