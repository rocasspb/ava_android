package com.rocasspb.avaawaand

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun DisclaimerDialog(
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(text = "Disclaimer")
        },
        text = {
            Text(
                text = "This tool is for informational purposes only. It does not replace official avalanche bulletins or professional danger assessments. You are solely responsible for your safety; entering the backcountry involves significant risk."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.testTag("DisclaimerConfirmButton")
            ) {
                Text("I Understand")
            }
        },
        modifier = Modifier.testTag("DisclaimerDialog")
    )
}
