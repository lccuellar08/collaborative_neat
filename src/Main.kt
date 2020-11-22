import com.sun.org.apache.xpath.internal.functions.FuncFalse
import neat.Client
import neat.Neat
import sim.Food
import sim.Individual
import sim.World
import java.lang.Math.atan2
import java.lang.Math.pow
import kotlin.math.sqrt

fun prepInputData(world: World, pov: List<World.Place>): DoubleArray {
    var inputData = DoubleArray(pov.size)
    for(i in 0 until pov.size) {
        val x = pov[i].x
        val y = pov[i].y
        if(x < 0 || y < 0 || x >= world.numCols || y >= world.numRows) {
            inputData[i] = 0.0
            continue
        }
        var entity = world.getVal(pov[i].x, pov[i].y)
        if(entity == null)
            inputData[i] = 0.05
        else
            inputData[i] = 1.0
    }
    return inputData
}

fun prepInputData(individual: Individual, pov: List<World.Place>): DoubleArray {
    var inputData = DoubleArray(4)
    var closestDistance = Double.MAX_VALUE
    var closestAngle = 0.001
    for(i in 0 until pov.size) {
        if(pov[i].value is Food) {
            val x = pov[i].x
            val y = pov[i].y
            val distance = sqrt(pow((individual.x - x).toDouble(), 2.0) + pow((individual.y - y).toDouble(), 2.0))
            if (distance < closestDistance) {
                closestDistance = distance
                closestAngle = atan2((individual.y - y).toDouble(), (individual.x - x).toDouble())
            }
        }

    }
    if(closestDistance == Double.MAX_VALUE)
        closestDistance = 0.001
    inputData[0] = closestDistance
    inputData[1] = closestAngle

    val dir = (individual.dir?.ordinal!!.toDouble()) / 7.0
    val norm_dir = 2 * kotlin.math.PI * dir / 7.0

    inputData[2] = kotlin.math.cos(norm_dir)
    inputData[3] = kotlin.math.sin(norm_dir)
    return inputData
}

fun processOutput(outputData: DoubleArray): Int {
    var emptyFlag = true
    for(output in outputData)
        if(output != 0.5)
            emptyFlag = false
    if(emptyFlag)
        return -1
    return outputData.max()?.let { outputData.indexOf(it) } ?: -1
}

fun runSimulation(client: Client, inputSize: Int, worldSize: Int, printResults: Boolean = false): Double {
    val world = World(worldSize, worldSize / 2)
    val indiv = Individual(1, world.numCols / 2,world.numRows / 2)
    world.placeIndividual(indiv)
    world.placeRandomFood(worldSize * 2)


    while(indiv.isAlive()) {
        if(printResults) {
            world.printGrid()
            println("-----------------------------------------------------")
            Thread.sleep(350L)
        }
        val inputData = prepInputData(indiv, world.getIndividualPov(indiv))
        if(inputData.size != inputSize) {
            indiv.kill()
            continue
        }

        val outputData = client.calculate(inputData) ?: continue
        //println(decision)
        when(processOutput(outputData)) {
            0 -> {world.moveIndividualForward(indiv)}
            1 -> indiv.turnLeft()
            2 -> indiv.turnRight()
        }
        //totalSteps += 1.0
        indiv.age()
    }
    return indiv.food
}

fun main(args: Array<String>) {
    var inputSize = 4
    var neat = Neat(inputSize = inputSize, outputSize = 3, maxClients = 500)
    var generations = 1000
    var num_iterations = 5

    for(i in 0 until generations) {
        for(client in neat.clients.data) {
            var total_score = 0.0
            for(i in (0 until num_iterations))
                total_score += runSimulation(client, inputSize, worldSize =  500)
            client.score = (total_score / num_iterations) / (client.genome?.nodes?.size() ?: 1)
        }
        neat.evolve()
        neat.printSpecies()
        println("Generation: $i")
        if(i % 5 == 0) {
            val specie = neat.species.data.maxBy{ it -> it.score} ?: continue
            val client = specie.representative
            client.toFile(filePath = "/Users/lc2377/Documents/Pers/github_projects/collaborative_neat/")
            runSimulation(client, inputSize, worldSize = 100, printResults = true)
        }
    }
}