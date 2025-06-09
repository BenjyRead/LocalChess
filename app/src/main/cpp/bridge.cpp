#include <cstring>
#include <iostream>
#include <jni.h>
#include <sstream>
#include <string>
#include <thread>
#include <unistd.h>
#include <vector>

#include "stockfish/bitboard.h"
#include "stockfish/misc.h"
#include "stockfish/position.h"
#include "stockfish/types.h"
#include "stockfish/uci.h"
#include "stockfish/tune.h"

int input_pipe[2];
int output_pipe[2];

void stockfish_main() {
    using namespace Stockfish;

    const int argc = 1;
    const char *argv[] = {"stockfish"};

    std::cout << engine_info() << std::endl;

    Bitboards::init();
    Position::init();

    UCIEngine uci(argc, const_cast<char **>(argv));
    Tune::init(uci.engine_options());

    uci.loop();
}

extern "C" JNIEXPORT void JNICALL
Java_StockfishEngine_startEngine(JNIEnv *, jobject) {
    pipe(input_pipe);  // stdin
    pipe(output_pipe); // stdout

    dup2(input_pipe[0], STDIN_FILENO);   // stdin
    dup2(output_pipe[1], STDOUT_FILENO); // stdout

    std::thread([]() { stockfish_main(); }).detach();
}

extern "C" JNIEXPORT void JNICALL
Java_StockfishEngine_sendCommand(JNIEnv *env, jobject,
                                 jstring jcmd) {
    const char *cmd = env->GetStringUTFChars(jcmd, 0);
    write(input_pipe[1], cmd, strlen(cmd));
    write(input_pipe[1], "\n", 1);
    env->ReleaseStringUTFChars(jcmd, cmd);
}

extern "C" JNIEXPORT jstring JNICALL
Java_StockfishEngine_readOutput(JNIEnv *env, jobject) {
    char buffer[1024] = {0};
    int len = read(output_pipe[0], buffer, sizeof(buffer) - 1);
    if (len > 0)
        return env->NewStringUTF(buffer);
    return env->NewStringUTF("");
}
