package utils

import kotlin.random.Random

class RandomSelector<T> {
    private var objects: MutableList<T> = mutableListOf()
    private var scores: MutableList<Double> = mutableListOf()
    private var totalScore: Double = 0.0

    fun add(obj: T, score: Double) {
        this.objects.add(obj)
        this.scores.add(score)
        this.totalScore += score
    }

    fun random(): T? {
        val v = Random.nextDouble(until = this.totalScore)
        var c = 0.0
        for(i in 0 until this.objects.size) {
            c += this.scores[i]
            if(c > v)
                return this.objects[i]
        }
        return null
    }

    fun reset() {
        this.objects.clear()
        this.scores.clear()
        this.totalScore = 0.0
    }
}