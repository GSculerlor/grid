// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    /*FernBSPLayout(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF272329)))
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F2FA)))
    }*/
    FernBSPLayout(modifier = modifier.fillMaxSize()) {
        MultipleLayoutNode()
    }
}

@Composable
private fun HoverableBox(modifier: Modifier = Modifier) {
    var active by remember { mutableStateOf(false) }

    val size by animateDpAsState(if (active) 28.dp else 12.dp)
    val alpha by animateFloatAsState(if (active) 1f else 0.25f)

    Box(
        modifier = modifier
            .onPointerEvent(PointerEventType.Enter) { active = true }
            .onPointerEvent(PointerEventType.Exit) { active = false }
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(Color.White.copy(alpha = alpha))
                .align(Alignment.Center)

        )
    }
}

@Composable
private fun ModifierOrder(modifier: Modifier = Modifier) {
    FernBSPLayout(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .border(1.dp, Color.Red)
                    .padding(8.dp)
                    .size(32.dp)
                    .border(1.dp, Color.Blue)
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .border(1.dp, Color.Red)
                    .size(32.dp)
                    .padding(8.dp)
                    .border(1.dp, Color.Blue)
            )
        }
    }
}

@Composable
private fun MutableState(modifier: Modifier = Modifier) {
    val list = mutableStateListOf<Int>()
    val array = arrayListOf<Int>()

    FernBSPLayout(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            ImmutableLayout(
                list = list,
                onClick = { list.add(0) },
                modifier = Modifier.fillMaxSize().align(Alignment.Center)
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            MutableLayout(
                array = array,
                onClick = { array.add(0) },
                modifier = Modifier.fillMaxSize().align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun ImmutableLayout(list: List<Int>, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = list.joinToString(","))
        Button(onClick = onClick) {
            Text(text = "Append")
        }
    }
}

@Composable
private fun MutableLayout(array: ArrayList<Int>, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = array.joinToString(","))
        Button(onClick = onClick) {
            Text(text = "Append")
        }
    }
}

@Composable
private fun MultipleLayoutNode() {
    Text("Text")
    Button(onClick = { }) { Text("Button") }
    Text("Text 2")
    Button(onClick = { }) { Text("Button 2") }
}

fun main() = application {
    Window(title = "grid", onCloseRequest = ::exitApplication) {
        App()
    }
}
