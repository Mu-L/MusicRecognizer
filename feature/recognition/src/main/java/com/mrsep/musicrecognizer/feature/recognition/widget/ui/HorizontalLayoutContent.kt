package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.widget.WidgetUiState
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.buttonHorizontalPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.dividerHorizontalPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.widgetPadding
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun HorizontalLayoutContent(
    layout: RecognitionWidgetLayout.Horizontal,
    uiState: WidgetUiState,
    onLaunchRecognition: Action,
    onCancelRecognition: Action,
    onRetryRecognition: Action,
    onWidgetClick: Action,
) {
    val context = LocalContext.current
    // Clickable modifier affects the shape of widget during smooth transition,
    // it's better when it is not placed on root view, so wrap it in extra box
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .roundedWidgetBackground()
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(rippleOverride = -1, onClick = onWidgetClick)
                .padding(widgetPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (val status = uiState.status) {
                RecognitionStatus.Ready,
                is RecognitionStatus.Recognizing -> StatusInfo(
                    title = context.getWidgetTitleForStatus(status),
                    subtitle = context.getWidgetSubtitleForStatus(status)
                        .takeIf { !layout.isNarrow }
                )

                is RecognitionStatus.Done -> when (val result = status.result) {
                    is RecognitionResult.Success -> TrackInfoHorizontal(
                        track = result.track,
                        artwork = uiState.artwork,
                        layout = layout
                    )
                    else -> StatusInfo(
                        title = context.getWidgetTitleForStatus(status),
                        subtitle = context.getWidgetSubtitleForStatus(status)
                            .takeIf { !layout.isNarrow }
                    )
                }
            }
            Spacer(GlanceModifier.width(dividerHorizontalPadding))
            VerticalDivider()
            Spacer(GlanceModifier.width(dividerHorizontalPadding))
            Spacer(GlanceModifier.width(buttonHorizontalPadding(layout.recognitionButtonMaxSize)))
            val isRecognizing = uiState.status is RecognitionStatus.Recognizing
            val shouldUseRetryAction = when (val status = uiState.status) {
                is RecognitionStatus.Done -> when (status.result) {
                    is RecognitionResult.NoMatches,
                    RecognitionResult.NoSoundDetected -> true
                    else -> false
                }
                else -> false
            }
            AnimatedRecognitionButton(
                isRecognizing = isRecognizing,
                onClick = when {
                    isRecognizing -> onCancelRecognition
                    shouldUseRetryAction -> onRetryRecognition
                    else -> onLaunchRecognition
                },
                onClickLabel = context.getString(
                    when {
                        isRecognizing -> StringsR.string.action_cancel_recognition
                        shouldUseRetryAction -> StringsR.string.button_retry_recognition
                        else -> StringsR.string.action_recognize
                    }
                ),
                scaledButtonSize = layout.recognitionButtonMaxSize
            )
            Spacer(GlanceModifier.width(buttonHorizontalPadding(layout.recognitionButtonMaxSize)))
        }
    }
}
