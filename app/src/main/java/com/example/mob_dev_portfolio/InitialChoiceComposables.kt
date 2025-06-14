package com.example.mob_dev_portfolio

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

enum class ColorChoice {
    WHITE,
    BLACK,
    RANDOM,
}

enum class TimeControl {
    ONE_ZERO,
    FIVE_ZERO,
    TEN_ZERO,
    CUSTOM,
}

enum class Elo {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    CUSTOM,
}

enum class Error {
    NO_COLOR,
    NO_TIME_CONTROL,
    NO_ELO,
    NO_COLOR_OR_TIME_CONTROL,
    NO_ELO_OR_TIME_CONTROL,
    NO_ELO_OR_COLOR,
    NO_ELO_OR_TIME_CONTROL_OR_COLOR,
}

@Composable
fun InitialChoices(
    pieceColor: MutableState<ColorChoice?>,
    timeControlSelected: MutableState<TimeControl?>,
    timeControlMainInSeconds: MutableState<Int>,
    incrementInSeconds: MutableState<Int>,
    error: MutableState<Error?>,
    nextActivity: Class<*>,
    optionalExtras: Map<String, Any> = emptyMap(),
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        ChooseColorSegment(pieceColor, error.value)
        ChooseTimeControl(
            timeControlSelected,
            timeControlMainInSeconds,
            incrementInSeconds,
            error.value
        )
        StartGameButton(
            pieceColor.value,
            timeControlSelected.value,
            timeControlMainInSeconds.value,
            incrementInSeconds.value,
            error,
            nextActivity,
            optionalExtras
        )
    }
}

@Composable
fun EngineInitialChoices(
    pieceColor: MutableState<ColorChoice?>,
    timeControlSelected: MutableState<TimeControl?>,
    timeControlMainInSeconds: MutableState<Int>,
    incrementInSeconds: MutableState<Int>,
    eloSelected: MutableState<Elo?>,
    stockfishElo: MutableState<Int?>,
    error: MutableState<Error?>,
    nextActivity: Class<*>,
    optionalExtras: Map<String, Any> = emptyMap(),
) {
//    TODO: error messages dont carry all cases, need to find a better way
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        ChooseColorSegment(pieceColor, error.value, engineChoice = true)
        ChooseTimeControl(
            timeControlSelected,
            timeControlMainInSeconds,
            incrementInSeconds,
            error.value,
            engineChoice = true
        )
        ChooseElo(error.value, eloSelected, stockfishElo)
        EngineStartGameButton(
            pieceColor.value,
            timeControlSelected.value,
            timeControlMainInSeconds.value,
            incrementInSeconds.value,
            eloSelected.value,
            stockfishElo.value,
            error,
            nextActivity,
            optionalExtras
        )
    }
}

@Composable
fun EloButton(text: String, error: Error?, selected: Boolean, onClick: () -> Unit = {}) {

    var borderColor = BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary)
    if (selected) {
        borderColor = BorderStroke(2.dp, Color.Green)
    } else if (error == Error.NO_ELO || error == Error.NO_ELO_OR_COLOR || error == Error.NO_ELO_OR_TIME_CONTROL_OR_COLOR || error == Error.NO_ELO_OR_TIME_CONTROL) {
        borderColor = BorderStroke(2.dp, MaterialTheme.colorScheme.error)
    }

    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = borderColor,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun ChooseElo(
    error: Error?,
    eloSelected: MutableState<Elo?>,
    stockfishElo: MutableState<Int?>,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        var titleColor = MaterialTheme.colorScheme.onBackground
        var titleStyle = MaterialTheme.typography.labelMedium
        if (error == Error.NO_ELO || error == Error.NO_ELO_OR_COLOR || error == Error.NO_ELO_OR_TIME_CONTROL_OR_COLOR || error == Error.NO_ELO_OR_TIME_CONTROL) {
            titleColor = MaterialTheme.colorScheme.error
            titleStyle = MaterialTheme.typography.headlineMedium
        }

        Text(
            text = stringResource(id = R.string.choose_elo),
            color = titleColor,
            style = titleStyle,
        )
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EloButton(
                text = "Beginner (600 elo)",
                error = error,
                selected = eloSelected.value == Elo.BEGINNER,
                onClick = {
                    stockfishElo.value = 600
                    eloSelected.value = Elo.BEGINNER
                }
            )
            EloButton(
                text = "Intermediate (1200 elo)",
                error = error,
                selected = eloSelected.value == Elo.INTERMEDIATE,
                onClick = {
                    stockfishElo.value = 1200
                    eloSelected.value = Elo.INTERMEDIATE
                }
            )
            EloButton(
                text = "Advanced (1800 elo)",
                error = error,
                selected = eloSelected.value == Elo.ADVANCED,
                onClick = {
                    stockfishElo.value = 1800
                    eloSelected.value = Elo.ADVANCED
                }
            )
            EloButton(
                text = "Custom",
                error = error,
                selected = eloSelected.value == Elo.CUSTOM,
                onClick = {
                    eloSelected.value = Elo.CUSTOM
                }
            )
        }
        if (eloSelected.value == Elo.CUSTOM) {
            val sliderIndex = remember { mutableStateOf(1) };

            val sliderIndexToElo = { index: Int ->
                when (index) {
                    1 -> 100
                    2 -> 200
                    3 -> 400
                    4 -> 800
                    5 -> 1000
                    6 -> 1500
                    7 -> 1600
                    8 -> 1700
                    9 -> 2000
                    10 -> 2100
                    11 -> 2200
                    12 -> 2300
                    13 -> 2500
                    14 -> 3190
                    else -> 100;
                }
            }

            Column() {
                Text(
                    text = "${stockfishElo.value} elo",
                    color = titleColor,
                    style = titleStyle,
                )
                Slider(
                    value = sliderIndex.value.toFloat(),
                    onValueChange = {
                        sliderIndex.value = it.toInt()
                        stockfishElo.value = sliderIndexToElo(sliderIndex.value)
                    },
                    valueRange = 1f..14f,
                    steps = 14,
                )
            }

        }
    }
}

@Composable
fun ChooseColorButton(
    imageId: Int,
    contentDescription: String,
    error: Boolean,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var modifier = Modifier
        .size(screenWidth.div(4))
        .clickable { onClick() }
    if (selected) {
        modifier = modifier.border(BorderStroke(2.dp, Color.Green), RoundedCornerShape(8.dp))
    } else if (error) {
        modifier = modifier.border(
            BorderStroke(2.dp, MaterialTheme.colorScheme.error),
            RoundedCornerShape(8.dp)
        )
    } else {
        modifier = modifier.border(
            BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary),
            RoundedCornerShape(8.dp)
        )
    }


    Image(
        painter = painterResource(id = imageId),
        contentDescription = contentDescription,
        modifier = modifier,
    )
//    }
}

@Composable
fun ChooseColorSegment(
    pieceColor: MutableState<ColorChoice?>,
    error: Error?,
    engineChoice: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var titleColor = MaterialTheme.colorScheme.onBackground
        var titleStyle = MaterialTheme.typography.labelMedium
        var errorCondition = error == Error.NO_COLOR_OR_TIME_CONTROL || error == Error.NO_COLOR
        if (engineChoice) {
            errorCondition =
                errorCondition || error == Error.NO_ELO_OR_COLOR || error == Error.NO_ELO_OR_TIME_CONTROL_OR_COLOR
        }

        if (errorCondition) {
            titleColor = MaterialTheme.colorScheme.error
            titleStyle = MaterialTheme.typography.headlineMedium
        }


        Text(
            text = stringResource(id = R.string.choose_color),
            color = titleColor,
            style = titleStyle,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ChooseColorButton(
                R.drawable.w_king,
                "Choose White",
                selected = pieceColor.value == ColorChoice.WHITE,
                error = errorCondition,
                onClick = {
                    pieceColor.value = ColorChoice.WHITE
                })
            ChooseColorButton(
                R.drawable.b_king,
                "Choose Black",
                selected = pieceColor.value == ColorChoice.BLACK,
                error = errorCondition,
                onClick = {
                    pieceColor.value = ColorChoice.BLACK
                })
            ChooseColorButton(
                R.drawable.wb_king,
                "Random",
                selected = pieceColor.value == ColorChoice.RANDOM,
                error = errorCondition,
                onClick = {
                    pieceColor.value = ColorChoice.RANDOM
                })

        }
    }
}

@Composable
fun TimeControlButton(text: String, error: Boolean, selected: Boolean, onClick: () -> Unit = {}) {

    var borderColor = BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary)
    if (selected) {
        borderColor = BorderStroke(2.dp, Color.Green)
    } else if (error) {
        borderColor = BorderStroke(2.dp, MaterialTheme.colorScheme.error)
    }

    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = borderColor,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun ChooseTimeControl(
    timeControlSelected: MutableState<TimeControl?>,
    timeControlMain: MutableState<Int>,
    increment: MutableState<Int>,
    error: Error?,
    engineChoice: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        var titleColor = MaterialTheme.colorScheme.onBackground
        var titleStyle = MaterialTheme.typography.labelMedium
        var errorCondition =
            error == Error.NO_COLOR_OR_TIME_CONTROL || error == Error.NO_TIME_CONTROL
        if (engineChoice) {
            errorCondition =
                errorCondition || error == Error.NO_ELO_OR_TIME_CONTROL || error == Error.NO_ELO_OR_TIME_CONTROL_OR_COLOR
        }
        if (errorCondition) {
            titleColor = MaterialTheme.colorScheme.error
            titleStyle = MaterialTheme.typography.headlineMedium
        }

        Text(
            text = stringResource(id = R.string.choose_time_control),
            color = titleColor,
            style = titleStyle,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TimeControlButton(
                "1+0", errorCondition,
                selected = timeControlSelected.value == TimeControl.ONE_ZERO,
                onClick = {
                    timeControlSelected.value = TimeControl.ONE_ZERO
                    timeControlMain.value = 60
                    increment.value = 0
                })
            TimeControlButton(
                "5+0", errorCondition,
                selected = timeControlSelected.value == TimeControl.FIVE_ZERO,
                onClick = {
                    timeControlSelected.value = TimeControl.FIVE_ZERO
                    timeControlMain.value = 300
                    increment.value = 0
                })
            TimeControlButton(
                "10+0", errorCondition,
                selected = timeControlSelected.value == TimeControl.TEN_ZERO,
                onClick = {
                    timeControlSelected.value = TimeControl.TEN_ZERO
                    timeControlMain.value = 600
                    increment.value = 0
                })
        }
        TimeControlButton(
            "Custom", errorCondition,
            selected = timeControlSelected.value == TimeControl.CUSTOM,
            onClick = {
                timeControlSelected.value = TimeControl.CUSTOM
            })
        if (timeControlSelected.value == TimeControl.CUSTOM) {
            val timeControlSecondsToMinutes = { seconds: Int ->
                when (seconds) {
                    15 -> "1/4"
                    30 -> "1/2"
                    45 -> "3/4"
                    60 -> "1"
                    90 -> "1 1/2"
                    120 -> "2"
                    180 -> "3"
                    240 -> "4"
                    300 -> "5"
                    360 -> "6"
                    420 -> "7"
                    480 -> "8"
                    540 -> "9"
                    600 -> "10"
                    660 -> "11"
                    720 -> "12"
                    780 -> "13"
                    840 -> "14"
                    900 -> "15"
                    960 -> "16"
                    1020 -> "17"
                    1080 -> "18"
                    1140 -> "19"
                    1200 -> "20"
                    1500 -> "25"
                    1800 -> "30"
                    else -> throw IllegalArgumentException("Invalid seconds")

                }
            }

//            TODO: replace all strings with resources
            Text(
                text = "${stringResource(R.string.minutes_per_side)} ${
                    timeControlMain.value?.let { timeControlSecondsToMinutes(it) }
                }",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelMedium,
            )

            val sliderIndex = remember { mutableStateOf(0) };

            val sliderIndexToTimeControl = { index: Int ->
                when (index) {
                    1 -> 15
                    2 -> 30
                    3 -> 45
                    4 -> 60
                    5 -> 90
                    6 -> 120
                    7 -> 180
                    8 -> 240
                    9 -> 300
                    10 -> 360
                    11 -> 420
                    12 -> 480
                    13 -> 540
                    14 -> 600
                    15 -> 660
                    16 -> 720
                    17 -> 780
                    18 -> 840
                    19 -> 900
                    20 -> 960
                    21 -> 1020
                    22 -> 1080
                    23 -> 1140
                    24 -> 1200
                    25 -> 1500
                    26 -> 1800
                    else -> 300;
                }
            }


            Slider(
                value = sliderIndex.value.toFloat(),
                onValueChange = {
                    sliderIndex.value = it.toInt()
                    timeControlMain.value = sliderIndexToTimeControl(sliderIndex.value)

                },
                valueRange = 1f..26f,
                steps = 26,
            )

            Text(
                text = "${stringResource(R.string.increment_in_seconds)} ${increment.value}",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelMedium,
            )

            Slider(
                value = increment.value.toFloat() ?: 0f,
                onValueChange = {
                    increment.value = it.toInt()
                },
                valueRange = 0f..60f,
                steps = 61,
            )


        }

    }
}

@Composable
fun StartGameButton(
    pieceColor: ColorChoice?,
    timeControl: TimeControl?,
    timeControlMain: Int?,
    increment: Int?,
    error: MutableState<Error?>,
    nextActivity: Class<*>,
    optionalExtras: Map<String, Any> = emptyMap(),
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var borderColor = MaterialTheme.colorScheme.onPrimary
        if (error.value != null) {
            borderColor = MaterialTheme.colorScheme.error
        }
        OutlinedButton(
            onClick = {
                if (pieceColor == null && timeControl == null) {
                    error.value = Error.NO_COLOR_OR_TIME_CONTROL
                } else if (pieceColor == null) {
                    error.value = Error.NO_COLOR
                } else if (timeControl == null) {
                    error.value = Error.NO_TIME_CONTROL
                } else {
                    val intent = Intent(context, nextActivity).apply {
                        putExtra("color", pieceColor.name)
                        putExtra("timeControlMain", timeControlMain)
                        putExtra("increment", increment)
                        for ((key, value) in optionalExtras) {
                            when (value) {
                                is Int -> putExtra(key, value)
                                is String -> putExtra(key, value)
                                is Boolean -> putExtra(key, value)
                                else -> throw IllegalArgumentException("Unsupported type")
                            }
                        }
                    }

                    context.startActivity(intent)
                }


            },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            border = BorderStroke(2.dp, borderColor),
        ) {
            Text(
                text = stringResource(id = R.string.start_game),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelMedium,
            )

        }

        val messageDict = mapOf(
            Error.NO_COLOR to R.string.please_choose_color,
            Error.NO_TIME_CONTROL to R.string.please_choose_time_control,
            Error.NO_ELO to R.string.please_choose_elo,
            Error.NO_COLOR_OR_TIME_CONTROL to R.string.choose_color_time_control,
            Error.NO_ELO_OR_TIME_CONTROL to R.string.choose_elo_time_control,
            Error.NO_ELO_OR_COLOR to R.string.choose_elo_color,
            Error.NO_ELO_OR_TIME_CONTROL_OR_COLOR to R.string.choose_elo_color_time_control,
        )

        val errorMessage = messageDict[error.value]
        if (errorMessage != null) {
            Text(
                text = stringResource(id = errorMessage),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Composable
fun EngineStartGameButton(
    pieceColor: ColorChoice?,
    timeControl: TimeControl?,
    timeControlMain: Int?,
    increment: Int?,
    elo: Elo?,
    eloNumber: Int?,
    error: MutableState<Error?>,
    nextActivity: Class<*>,
    optionalExtras: Map<String, Any> = emptyMap(),
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var borderColor = MaterialTheme.colorScheme.onPrimary
        if (error.value != null) {
            borderColor = MaterialTheme.colorScheme.error
        }
        OutlinedButton(
            onClick = {
                error.value = when {
                    pieceColor == null && timeControl == null && elo == null -> Error.NO_ELO_OR_TIME_CONTROL_OR_COLOR
                    pieceColor == null && timeControl == null -> Error.NO_COLOR_OR_TIME_CONTROL
                    pieceColor == null && elo == null -> Error.NO_ELO_OR_COLOR
                    timeControl == null && elo == null -> Error.NO_ELO_OR_TIME_CONTROL
                    pieceColor == null -> Error.NO_COLOR
                    timeControl == null -> Error.NO_TIME_CONTROL
                    elo == null -> Error.NO_ELO
                    else -> null
                }
                if (error.value == null) {
                    val intent = Intent(context, nextActivity).apply {
                        putExtra("color", pieceColor!!.name)
                        putExtra("timeControlMain", timeControlMain)
                        putExtra("increment", increment)
                        putExtra("elo", eloNumber)
                        for ((key, value) in optionalExtras) {
                            when (value) {
                                is Int -> putExtra(key, value)
                                is String -> putExtra(key, value)
                                is Boolean -> putExtra(key, value)
                                else -> throw IllegalArgumentException("Unsupported type")
                            }
                        }
                    }

                    context.startActivity(intent)
                }


            },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            border = BorderStroke(2.dp, borderColor),
        ) {
            Text(
                text = stringResource(id = R.string.start_game),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelMedium,
            )

        }

        val messageDict = mapOf(
            Error.NO_COLOR to R.string.please_choose_color,
            Error.NO_TIME_CONTROL to R.string.please_choose_time_control,
            Error.NO_ELO to R.string.please_choose_elo,
            Error.NO_COLOR_OR_TIME_CONTROL to R.string.choose_color_time_control,
            Error.NO_ELO_OR_TIME_CONTROL to R.string.choose_elo_time_control,
            Error.NO_ELO_OR_COLOR to R.string.choose_elo_color,
            Error.NO_ELO_OR_TIME_CONTROL_OR_COLOR to R.string.choose_elo_color_time_control,
        )

        val errorMessage = messageDict[error.value]
        if (errorMessage != null) {
            Text(
                text = stringResource(id = errorMessage),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

