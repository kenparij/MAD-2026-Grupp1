package com.example.townexplorer.ui.screens

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.tooling.preview.Preview
import com.example.townexplorer.map.MapLibreView


/**
 * Displays the exploration map and the POI bottom sheet.
 *
 * Sources:
 *   https://developer.android.com/develop/ui/compose
 */
@Composable
fun HomeScreen() {
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // Displays the MapLibre map and fog overlay.
        MapLibreView(
            modifier = Modifier.fillMaxSize()
        )

        // Opens the POI details bottom sheet.
        Button(
            onClick = { openBottomSheet = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(text = "POI")
        }
    }

    if (openBottomSheet) {
        POIItem(
            onDismissRequest = { openBottomSheet = false }
        )
    }
}


/**
 * Displays an overlay bottom sheet on top of the active screen content.
 * Code example taken from: https://developer.android.com/reference/kotlin/androidx/compose/material3/ModalBottomSheet.composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIItem(
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.75f)
                .padding(20.dp)
        ){
            Text(text = "Point of interest",
                style = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(30.dp))            //Image()
            Text(text = "Pulvinar vivamus fringilla lacus nec metus bibendum egestas. Iaculis massa nisl malesuada lacinia integer nunc posuere. Ut hendrerit semper vel class aptent taciti sociosqu. Ad litora torquent per conubia nostra inceptos himenaeos.")
        }

    }
}