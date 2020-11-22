package sim

class Food constructor(var x: Int, var y: Int, val saturation: Int = 1): Entity(x,y) {
    override fun toString(): String = "F"
}