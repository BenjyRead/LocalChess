cmake_minimum_required(VERSION 3.18.1)

add_link_options("LINKER:--build-id=none")

project("stockfish")

add_library(stockfishjni SHARED
        # Stockfish files:
        bridge.cpp
        stockfish/engine.cpp
        # stockfish/main.cpp
        stockfish/timeman.cpp
        stockfish/evaluate.cpp
        stockfish/position.cpp
        stockfish/uci.cpp
        stockfish/movepick.cpp
        stockfish/benchmark.cpp
        stockfish/ucioption.cpp
        stockfish/search.cpp
        stockfish/thread.cpp
        stockfish/score.cpp
        stockfish/memory.cpp
        stockfish/syzygy/tbprobe.cpp
        stockfish/nnue/network.cpp
        stockfish/nnue/nnue_misc.cpp
        stockfish/nnue/nnue_accumulator.cpp
        stockfish/nnue/features/half_ka_v2_hm.cpp
        stockfish/bitboard.cpp
        stockfish/tt.cpp
        stockfish/movegen.cpp
        stockfish/misc.cpp
        stockfish/tune.cpp
)

# Path to the Android NDK
set(ANDROID_NDK $ENV{ANDROID_NDK_HOME})

# Platform-dependent JNI path
set(JNI_INCLUDE_DIR "${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include")
set(JNI_INCLUDE_DIR2 "${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/android")

target_include_directories(stockfishjni PRIVATE
        stockfish
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_CURRENT_SOURCE_DIR}/stockfish
        ${JNI_INCLUDE_DIR}
        ${JNI_INCLUDE_DIR2}
)

target_compile_definitions(stockfishjni PRIVATE
        __ANDROID__
        ANDROID
        NNUE_EMBEDDING_OFF
        # GIT_DATE = "May 25 2025"
        GIT_DATE="20250525"
        GIT_SHA="73c55e894959b2dcd40c179ade6e1eafc9d952ea"
)



