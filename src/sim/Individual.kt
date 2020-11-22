package sim

import java.lang.Exception

class Individual constructor(val id: Int, var x: Int, var y: Int, var dir: Direction? = null,
                             val viewDistance: Int = 5) : Entity(x,y){
    var food = 0.01
    private var life = 10

    private val FOOD_DELTA = 10

    init {
        if(this.dir == null)
            this.dir = this.randomDirection()

    }

    public fun turnLeft() {
        // Go counter clockwise
        var currDir: Direction = this.dir ?: Direction.North
        val newDir = currDir.ordinal - 1
        if(newDir < 0)
            this.dir =  Direction.NorthWest
        else
            this.dir = Direction.fromInt(newDir)
    }

    public fun turnRight() {
        // Go clockwise
        var currDir: Direction = this.dir ?: Direction.North
        val newDir = currDir.ordinal +  1
        if(newDir > 7)
            this.dir =  Direction.North
        else
            this.dir = Direction.fromInt(newDir)
    }

    private fun randomDirection(): Direction {
        val newDir = (0..7).random()
        return Direction.fromInt(newDir)
    }

    public fun getForwardPlace(): Pair<Int, Int> {
        var dx = 0
        var dy = 0

        when(this.dir) {
            Direction.North ->
                dy = -1
            Direction.NorthEast ->
                {dx = 1; dy = -1}
            Direction.East ->
                dx = 1
            Direction.SouthEast ->
                {dx = 1; dy = 1}
            Direction.South ->
                dx = 0
            Direction.SouthWest ->
                {dx = -1; dy = 1}
            Direction.West ->
                dx = -1
            Direction.NorthWest ->
                {dx = -1; dy = -1}
        }

        return Pair(this.x + dx, this.y + dy)
    }

    public fun eat(food: Food) {
        this.food += food.saturation
        this.life += this.FOOD_DELTA
    }

    public fun age() {
        this.life -= 1
    }

    public fun kill() {
        this.life = 0
    }

    public fun isAlive(): Boolean {
        return this.life > 0
    }

    public fun isDead(): Boolean {
        return this.life <= 0
    }

    // TO-DO
    public fun getPovNatural(): List<Pair<Int,Int>> {
        return emptyList()
    }

    // TO-DO
    public fun getPovDiagonal(): List<Pair<Int,Int>> {
        return emptyList()
    }

    override fun toString(): String {
        //return this.id.toString()
        var currDir: Direction = this.dir ?: Direction.North
        return currDir.ordinal.toString()
    }

    enum class Direction(val dir: Int) {
        North(0),
        NorthEast(1),
        East(2),
        SouthEast(3),
        South(4),
        SouthWest(5),
        West(6),
        NorthWest(7);

        companion object {
            fun fromInt(dir: Int) = Direction.values().first { it.dir == dir }
        }
    }
}