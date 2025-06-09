import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

object StockfishEngine {
    val inputChannel = Channel<String>(Channel.UNLIMITED)
    val outputChannel = Channel<String>(Channel.UNLIMITED)
    var engineStarted = false

    private external fun startEngine()
    private external fun sendCommand(cmd: String)
    private external fun readOutput(): String

    fun start(context: Context) {
        if (engineStarted) return
        System.loadLibrary("stockfishjni")
        startEngine()
        engineStarted = true
        listenOutput()
        CoroutineScope(Dispatchers.IO).launch {
            sendInput()
        }

        // Copy NNUE file and send eval command
        CoroutineScope(Dispatchers.IO).launch {
            val nnuePath = copyNNUEFile(context, "nn-1c0000000000.nnue")
            inputChannel.send("uci")
            delay(100) // Wait for engine to output uciok
            inputChannel.send("setoption name EvalFile value $nnuePath")
            inputChannel.send("isready")
        }
    }

    private fun sendInput() = CoroutineScope(Dispatchers.IO).launch {
        CoroutineScope(Dispatchers.IO).launch {
            for (cmd in inputChannel) {
                sendCommand(cmd)
            }
        }
    }

    private fun listenOutput() = CoroutineScope(Dispatchers.IO).launch {
        while (true) {
            val output = readOutput()
            if (output.isNotBlank()) {
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
