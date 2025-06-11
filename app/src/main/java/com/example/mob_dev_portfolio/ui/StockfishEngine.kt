import android.content.Context
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
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

        CoroutineScope(Dispatchers.IO).launch {
            val nnuePath = copyNNUEFile(context, "nn-1c0000000000.nnue")
            val nnuePath2 = copyNNUEFile(context, "nn-37f18f62d772.nnue")
            Log.d("StockfishEngine", "NNUE file copied to: $nnuePath")
            inputChannel.send("uci")
            inputChannel.send("setoption name EvalFile value $nnuePath")
            inputChannel.send("setoption name EvalFileSmall value $nnuePath2")
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
