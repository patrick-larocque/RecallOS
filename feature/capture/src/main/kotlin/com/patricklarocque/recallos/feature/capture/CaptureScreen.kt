package com.patricklarocque.recallos.feature.capture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CaptureScreen(
    onSaved: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    prefillTitle: String? = null,
    prefillBody: String? = null,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    LaunchedEffect(prefillTitle, prefillBody) {
        if (prefillTitle != null || prefillBody != null) {
            viewModel.prefill(title = prefillTitle, body = prefillBody)
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveState) {
        if (uiState.saveState is CaptureState.Success) {
            viewModel.acknowledgeSuccess()
            onSaved()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::setTitle,
                label = { Text("Title (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.body,
                onValueChange = viewModel::setBody,
                label = { Text("Note") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.saveState is CaptureState.Error) {
                Text(
                    text = (uiState.saveState as CaptureState.Error).message,
                    color = Color.Red,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Button(
                onClick = { viewModel.save() },
                enabled = uiState.body.isNotBlank() && uiState.saveState !is CaptureState.Saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }
        }

        if (uiState.saveState is CaptureState.Saving) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
