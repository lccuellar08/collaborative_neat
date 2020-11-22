package sim

import javafx.beans.binding.When
import kotlin.math.abs

class World constructor(val numCols: Int = 100, val numRows: Int = 100){
    private var places: List<List<Place>> = emptyList()
    private var individuals: MutableList<Individual> = mutableListOf<Individual>()

    init {
        this.places = this.getEmptyGrid(this.numCols, this.numRows)
    }

    private fun getEmptyGrid(cols: Int, rows: Int): List<List<Place>> {
        val places = mutableListOf<List<Place>>()
        for(row in 0 until rows) {
            val rowPlaces = mutableListOf<Place>()
            for(col in 0 until cols) {
                rowPlaces.add(Place(col,row,null))
            }
            places.add(rowPlaces)
        }
        return places
    }

    fun printGrid() {
        for(row in 0 until this.numRows) {
            var rowStr = ""
            for(col in 0 until this.numCols) {
                val e: Entity? = this.getVal(col, row)
                if(e == null)
                    rowStr += ". "
                else
                    rowStr += e.toString() +" "
            }
            println(message = rowStr)
        }
    }

    fun removeIndividual(ind: Individual) {
        this.individuals.remove(ind)
    }

    fun getVal(x: Int, y: Int): Entity? {
        try {
            return this.places[y][x].value
        }
        catch (e: java.lang.IndexOutOfBoundsException) {
            return null
        }
    }

    fun setVal(x: Int, y: Int, entity: Entity?) {
        this.places[y][x].value = entity
    }

    fun placeFood(x: Int, y: Int): Boolean {
        if(this.isValidPlace(x,y)) {
            this.setVal(x, y, Food(x,y))
            return true
        }
        return false
    }

    fun placeRandomFood(numFood: Int) {
        for(i in 0 until numFood) {
            while(true) {
                val x = (0 until this.numCols).random()
                val y = (0 until this.numRows).random()
                val e = this.getVal(x,y)
                if(e == null) {
                    this.placeFood(x,y)
                    break
                }
            }
        }
    }

    fun getPovSize(): Int {
        val ind = Individual(-1, (this.numCols / 2), (this.numRows / 2))
        val povs = this.getIndividualPov(ind)
        return povs.size
    }

    fun placeIndividual(ind: Individual): Boolean {
        if(this.isValidPlace(ind.x,ind.y)) {
            this.setVal(ind.x, ind.y, ind)
            return true
        }
        return false
    }

    fun placeRandomIndividual() {
        while(true) {
            val x = (0 until this.numCols).random()
            val y = (0 until this.numRows).random()
            val e = this.getVal(x,y)
            if(e == null) {
                val ind = Individual(this.individuals.size, x, y, null)
                this.placeIndividual(ind)
                this.individuals.add(ind)
                break
            }
        }
    }

    fun getIndividualForwardPlace(ind: Individual): Place {
        var dx = 0
        var dy = 0
        when(ind.dir) {
            Individual.Direction.North ->
                dy = -1
            Individual.Direction.NorthEast ->
                {dx = 1; dy = -1}
            Individual.Direction.East ->
                dx = 1
            Individual.Direction.SouthEast ->
                {dx = 1; dy = 1}
            Individual.Direction.South ->
                {dy = 1}
            Individual.Direction.SouthWest ->
                {dx = -1; dy = 1}
            Individual.Direction.West ->
                {dx = -1}
            Individual.Direction.NorthWest ->
                {dx = -1; dy = -1}
        }
        return Place(ind.x + dx, ind.y + dy, null)
    }

    fun getIndividualPov(ind: Individual): List<Place> {
        var x_values = (ind.x - ind.viewDistance .. ind.x + ind.viewDistance)
        var y_values = (ind.y - ind.viewDistance .. ind.y + ind.viewDistance)

        val places: MutableList<Place> = mutableListOf()
        for(x in x_values) {
            for(y in y_values) {
                when(ind.dir) {
                    Individual.Direction.North -> {
                        if((y - ind.y) > abs(x - ind.x)) {
                            val newY = (((y - ind.y) * -1) + (ind.y))
                            places.add(Place(x, newY, getVal(x, newY)))
                        }
                    }
                    Individual.Direction.South -> {
                        if((y - ind.y) > abs(x - ind.x) && y > ind.y)
                            places.add(Place(x, y, getVal(x,y)))
                    }
                    Individual.Direction.West -> {
                        if(y >= ind.y) {
                            // y <=  -x
                            if((y - ind.y) < (-1 * (x - ind.x))) {
                                places.add(Place(x, y, getVal(x, y)))
                            }
                        }
                        else if(y < ind.y) {
                            // y >= x
                            if((y - ind.y) > (x - ind.x)) {
                                places.add(Place(x, y, getVal(x, y)))
                            }
                        }
                    }
                    Individual.Direction.East -> {
                        if(y >= ind.y) {
                            // y <=  x
                            if((y - ind.y) < (x - ind.x)) {
                                places.add(Place(x, y, getVal(x, y)))
                            }
                        }
                        else if(y < ind.y) {
                            // y >= x
                            if((y - ind.y) > (-1 * (x - ind.x))) {
                                places.add(Place(x, y, getVal(x, y)))
                            }
                        }
                    }
                    Individual.Direction.SouthEast -> {
                        if((y - ind.y) > 0 && (x - ind.x) > 0)
                            places.add(Place(x, y, getVal(x, y)))
                    }
                    Individual.Direction.SouthWest -> {
                        if((y - ind.y) > 0 && (-1 * (x - ind.x)) > 0)
                            places.add(Place(x, y, getVal(x, y)))
                    }
                    Individual.Direction.NorthWest -> {
                        if((-1 * (y - ind.y)) > 0 && (-1 * (x - ind.x)) > 0)
                            places.add(Place(x, y, getVal(x, y)))
                    }
                    Individual.Direction.NorthEast -> {
                        if((-1 * (y - ind.y)) > 0 && (x - ind.x) > 0)
                            places.add(Place(x, y, getVal(x, y)))
                    }
                }

            }
        }

        return places
    }

    public fun moveIndividualForward(ind: Individual): Boolean {
        if(ind.isAlive()) {
            val forwardPlace = this.getIndividualForwardPlace(ind)

            if(forwardPlace.x < 0 || forwardPlace.y < 0 || forwardPlace.x >= this.numCols || forwardPlace.y >= this.numRows) {
                return false
            }
            this.setVal(ind.x, ind.y, null)
            ind.x = forwardPlace.x % this.numCols
            ind.y = forwardPlace.y % this.numRows

            val currEntity = this.getVal(ind.x, ind.y)
                if(currEntity is Food) {
                    ind.eat(currEntity)
                }
            // eat food
            return(this.placeIndividual(ind))

        }
        return false
    }

    private fun isValidPlace(x: Int, y: Int): Boolean {
        return (x >= 0 && x < this.numCols) && (y >= 0 && y < this.numRows)
    }

    class Pov constructor(var x: Int, var y: Int): Entity(x,y) {
        override fun toString(): String = "P"
    }

    data class Place(val x: Int, val y: Int, var value: Entity?)
}

fun main(args: Array<String>) {
    val world = World(20,20)
    //world.placeFood(4,4)
    val ind = Individual(1, 10,10, Individual.Direction.North)
    world.placeIndividual(ind)

    var povs = world.getIndividualPov(ind)
    var forwardPlace = world.getIndividualForwardPlace(ind)
    var forwardInd = Individual(2, forwardPlace.x, forwardPlace.y, ind.dir)
    world.placeFood(10, 7)
    world.placeFood(10, 13)
    world.placeFood(7,10)
    world.placeFood(13,10)
    println(povs.size)
//    for(pov in povs)
//        world.setVal(pov.x, pov.y, pov.value)
//
//    world.placeIndividual(forwardInd)
//    world.printGrid()
//    for(pov in povs)
//        world.setVal(pov.x, pov.y, null)

    while(true) {
        println("Press Enter")
        var inputValue = Integer.valueOf(readLine())
        var newDir = Individual.Direction.fromInt(inputValue)
        ind.dir = newDir

        world.moveIndividualForward(ind)
        forwardPlace = world.getIndividualForwardPlace(ind)
        forwardInd = Individual(2, forwardPlace.x, forwardPlace.y, ind.dir)
        povs = world.getIndividualPov(ind)
        println(povs.size)
//        for(pov in povs)
//            world.setVal(pov.x, pov.y, pov.value)

        world.placeIndividual(forwardInd)


        world.printGrid()
        world.removeIndividual(forwardInd)
        for(i in (0 until povs.size)) {
            println("$i = (${povs[i].x}, ${povs[i].y}): ${povs[i].value}")
        }
    }
}