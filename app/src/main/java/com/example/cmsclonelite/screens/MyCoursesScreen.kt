package com.example.cmsclonelite.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.cmsclonelite.viewmodels.MainViewModel

@Composable
fun MyCoursesScreen(mainViewModel: MainViewModel) {
    LaunchedEffect(Unit) {
        mainViewModel.setTitle("My Courses")
    }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "My Courses",
            fontSize = MaterialTheme.typography.h3.fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
@Preview
fun MyCoursesScreenPreview() {
    val mainViewModel = MainViewModel()
    MyCoursesScreen(mainViewModel = mainViewModel)
}