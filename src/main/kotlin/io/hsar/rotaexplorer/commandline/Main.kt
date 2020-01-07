package io.hsar.rotaexplorer.commandline

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.hsar.rotaexplorer.RotaExplorer
import io.hsar.rotaexplorer.input.CSVResponsesParser
import java.io.File
import kotlin.system.exitProcess

abstract class Command(val name: String) : Runnable

@Parameters(commandDescription = "Creates a rota from filesystem inputs.")
class FileSystemRotaCommand : Command("create-rota-filesystem") {

    @Parameter(
            names = arrayOf("--responses-csv"),
            description = "Path to responses in comma-separated values (CSV) file format.",
            required = false
    )
    private var csvResponsesPath = ""

    override fun run() {
        if (csvResponsesPath.isBlank()) {
            throw IllegalArgumentException("Response input not found.")
        }

        val responses = CSVResponsesParser().parseFile(File(csvResponsesPath))

        RotaExplorer(responses = responses).execute()
                .let { result ->
                    println(jacksonObjectMapper().writeValueAsString(result))
                }
    }
}

fun main(args: Array<String>) {
    val instances =
            listOf(
                    FileSystemRotaCommand()
            )
                    .associateBy { it.name }

    JCommander()
            .also { commander ->
                instances.forEach { (name, command) ->
                    commander.addCommand(name, command)
                }
            }
            .let { commander ->
                if (args.isEmpty()) {
                    commander.usage()
                    System.err.println("Expected some arguments")
                    exitProcess(1)
                }

                try {
                    commander.parse(*args)
                    instances
                            .getValue(commander.parsedCommand)
                            .run()
                } catch (e: Exception) {
                    e.printStackTrace()
                    exitProcess(1)
                }
            }
}
