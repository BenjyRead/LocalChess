import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

object StockfishEngine {
    val inputChannel = Channel<String>(Channel.UNLIMITED)
    val outputChannel = Channel<String>(Channel.UNLIMITED)
    var engineStarted = false

    private external fun startEngine()
    private external fun sendCommand(cmd: String)
    private external fun readOutput(): String

    fun start(context: Context, evalFileName: String, evalFileSmallName: String) {
        if (engineStarted) return
        System.loadLibrary("stockfishjni")
        startEngine()
        engineStarted = true
        listenOutput()
        CoroutineScope(Dispatchers.IO).launch {
            sendInput()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val evalFilePath = copyNNUEFile(context, evalFileName)
            val evalFileSmallPath = copyNNUEFile(context, evalFileSmallName)
            Log.d("StockfishEngine", "NNUE file copied to: $evalFilePath")
            inputChannel.send("uci")
            inputChannel.send("setoption name EvalFile value $evalFilePath")
            inputChannel.send("setoption name EvalFileSmall value $evalFileSmallPath")
            inputChannel.send("isready")
        }
    }

    private fun sendInput() = CoroutineScope(Dispatchers.IO).launch {
        CoroutineScope(Dispatchers.IO).launch {
            for (cmd in inputChannel) {
                Log.d("StockfishEngine", "Stockfish Input: $cmd")
                sendCommand(cmd)
            }
        }
    }

    private fun listenOutput() = CoroutineScope(Dispatchers.IO).launch {
        while (true) {
            val output = readOutput()
            if (output.isNotBlank()) {
                Log.d("StockfishEngine", "Stockfish output: $output")
                outputChannel.send(output)
            }
        }
    }

    private fun copyNNUEFile(context: Context, fileName: String): String {
        val outFile = context.filesDir.resolve(fileName)
        if (!outFile.exists()) {
            context.assets.open(fileName).use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return outFile.absolutePath
    }
}
