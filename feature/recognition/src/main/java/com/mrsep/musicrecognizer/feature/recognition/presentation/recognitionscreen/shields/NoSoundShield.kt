package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.domain.preferences.AudioCaptureMode
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun AnimatedVisibilityScope.NoSoundShield(
    modifier: Modifier = Modifier,
    usedCaptureMode: AudioCaptureMode,
    usedAltDeviceSoundSource: Boolean,
    onRetryWithModeClick: (source: AudioCaptureMode) -> Unit,
    onDismissClick: () -> Unit,
) {
    val mayRestrictDeviceAudio = !usedAltDeviceSoundSource &&
            usedCaptureMode == AudioCaptureMode.Device || usedCaptureMode == AudioCaptureMode.Auto
    BaseShield(
        modifier = modifier,
        onDismissClick = onDismissClick
    ) {
        Icon(
            painter = painterResource(UiR.drawable.outline_no_sound_24),
            modifier = Modifier.size(72.dp),
            contentDescription = null
        )
        Text(
            text = stringResource(StringsR.string.result_title_no_sound_detected),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            text = when (usedCaptureMode) {
                AudioCaptureMode.Microphone -> stringResource(StringsR.string.result_message_no_sound_detected_microphone)
                AudioCaptureMode.Device,
                AudioCaptureMode.Auto -> stringResource(StringsR.string.result_message_no_sound_detected_device)
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        if (mayRestrictDeviceAudio) {
            Text(
                text = stringResource(StringsR.string.internal_audio_capture_may_be_blocked),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        Column(
            modifier = Modifier
                .padding(top = 24.dp)
                .width(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var selectedCaptureMode by rememberSaveable(usedCaptureMode) { mutableStateOf(usedCaptureMode) }
            if (mayRestrictDeviceAudio) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.height(IntrinsicSize.Max)
                ) {
                    AudioCaptureMode.entries.forEachIndexed { index, captureMode ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = AudioCaptureMode.entries.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeBorderColor = MaterialTheme.colorScheme.outline.copy(0.25f),
                                inactiveBorderColor = MaterialTheme.colorScheme.outline.copy(0.25f),
                                inactiveContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.1f),
                                inactiveContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                            onClick = { selectedCaptureMode = captureMode },
                            selected = selectedCaptureMode == captureMode,
                            icon = {},
                            modifier = Modifier.fillMaxHeight(),
                        ) {
                            Text(
                                text = captureMode.getTitle(),
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
            FilledTonalButton(
                onClick = { onRetryWithModeClick(selectedCaptureMode) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(StringsR.string.button_retry_recognition))
            }
        }
    }
}

@Composable
private fun AudioCaptureMode.getTitle(): String {
    return when (this) {
        AudioCaptureMode.Microphone -> stringResource(StringsR.string.audio_capture_mode_microphone)
        AudioCaptureMode.Device -> stringResource(StringsR.string.audio_capture_mode_device)
        AudioCaptureMode.Auto -> stringResource(StringsR.string.audio_capture_mode_auto)
    }
}
