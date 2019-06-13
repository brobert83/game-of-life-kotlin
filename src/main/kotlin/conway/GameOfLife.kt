package conway

import conway.Cell.ALIVE
import conway.Cell.DEAD

enum class Cell {
    ALIVE,
    DEAD
}

fun Array<Array<Int>>.parseInput() =
        map { line ->
            line.map { cell ->
                when (cell) {
                    1 -> ALIVE
                    0 -> DEAD
                    else -> throw RuntimeException("Invalid input, only 0 and 1 are acceptable")
                }
            }.toTypedArray()
        }.toTypedArray()

fun Array<Array<Cell>>.toOutput() =
        map { line ->
            line.map { cell ->
                when (cell) {
                    ALIVE -> 1
                    DEAD -> 0
                }
            }.toTypedArray()
        }.toTypedArray()

val neighbours: Array<Pair<Int, Int>> = arrayOf(
        Pair(-1, -1), Pair(0, -1), Pair(1, -1),
        Pair(-1, 0), /*       */ Pair(1, 0),
        Pair(-1, 1), Pair(0, 1), Pair(1, 1)
)

val Pair<Int, Int>.x get() = first
val Pair<Int, Int>.y get() = second

fun Array<Array<Cell>>.countPopulation(x: Int, y: Int): Int =
        neighbours
                .map { route ->
                    elementAtOrElse(x + route.x) { _ -> arrayOf() }
                            .elementAtOrElse(y + route.y) { _ -> DEAD }
                }
                .filter { neighbour -> neighbour == ALIVE }
                .count()

fun newLine(size: Int) = Array(size) { _ -> DEAD }

fun Array<Array<Cell>>.padTop() = arrayOf(newLine(this[0].size)) + this
fun Array<Array<Cell>>.padBottom() = this + arrayOf(newLine(this[0].size))
fun Array<Array<Cell>>.padLeft() = map { line -> arrayOf(DEAD) + line }.toTypedArray()
fun Array<Array<Cell>>.padRight() = map { line -> line + arrayOf(DEAD) }.toTypedArray()

fun Array<Array<Cell>>.padMatrix() = padTop().padBottom().padLeft().padRight()


fun Array<Array<Cell>>.shrink(): Array<Array<Cell>> {

    var minX = this.size
    var minY = this[0].size
    var maxX = 0
    var maxY = 0

    forEachIndexed { x, line ->
        line.forEachIndexed cellLoop@{ y, cell ->

            if (cell == DEAD) return@cellLoop

            minX = if (minX > x) x else minX
            minY = if (minY > y) y else minY
            maxX = if (maxX < x) x else maxX
            maxY = if (maxY < y) y else maxY
        }
    }

    val lenX = maxX - minX + 1
    val lenY = maxY - minY + 1

    val innerMatrix = Array(lenX) { Array(lenY) { _ -> DEAD } }

    for (x in minX..maxX) {
        innerMatrix[x - minX] = this[x].sliceArray(minY..(minY + lenY - 1))
    }

    return innerMatrix
}

fun Cell.conway(population: Int) =
        when (this) {
            ALIVE -> if (population == 2 || population == 3) ALIVE else DEAD
            DEAD -> if (population == 3) ALIVE else DEAD
        }

tailrec fun Array<Array<Cell>>.generation(generationIndex: Int): Array<Array<Cell>> {

    return if (generationIndex == 0) this
    else {

        val newCellsInit = padMatrix()
        val newCells = Array(newCellsInit.size) { _ -> newLine(newCellsInit[0].size) }

        for (x in newCells.indices) {
            for (y in newCells[0].indices) {

                val popCount = newCellsInit.countPopulation(x, y)

                newCells[x][y] = newCellsInit[x][y].conway(popCount)
            }
        }

        newCells.shrink().generation(generationIndex - 1)
    }
}

fun generation(cells: Array<Array<Int>>, count: Int): Array<Array<Int>> = cells.parseInput().generation(count).toOutput()
