package com.example.cmsclonelite.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cmsclonelite.Announcement
import com.example.cmsclonelite.repository.CourseRepository
import com.example.cmsclonelite.viewmodels.AnnouncementsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var mAuth: FirebaseAuth

@Composable
fun DetailedAnnouncementScreen(
    navController: NavHostController,
    announcement: Announcement
) {
    mAuth = FirebaseAuth.getInstance()
    val uriHandler = LocalUriHandler.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    "${announcement.title}"
                ) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(Icons.Rounded.ArrowBack, "")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(top = 8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 12.dp),
                elevation = 24.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                    ) {
                        Text(
                            "${announcement.title}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Divider(
                        color = Color.LightGray, modifier = Modifier
                            .fillMaxWidth()
                            .width(1.dp)
                    )
                    Row(
                        modifier = Modifier.padding(
                            start = 8.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    ) {
                        Text(
                            "${announcement.body}",
                            fontSize = 16.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(top = 12.dp))
            if(announcement.downloadUri.toString() != "null" && announcement.fileName != "null") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 12.dp),
                    elevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                start = 8.dp,
                                end = 8.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            Text(
                                "Attachments:",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Divider(
                            color = Color.LightGray, modifier = Modifier
                                .fillMaxWidth()
                                .width(1.dp)
                        )
                        Row(
                            modifier = Modifier.padding(
                                start = 8.dp,
                                end = 8.dp,
                                top = 16.dp,
                                bottom = 16.dp
                            ).clickable(
                                onClick = {
                                    uriHandler.openUri(announcement.downloadUri.toString())
                                }
                            )
                        ) {
                            Text(
                                modifier = Modifier.weight(0.9f),
                                text = announcement.fileName,
                                fontSize = 16.sp
                            )
                            Icon(
                                modifier = Modifier.weight(0.1f),
                                imageVector = Icons.Filled.Download,
                                contentDescription = "Download file"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailedAnnouncementScreenPreview() {
    DetailedAnnouncementScreen(rememberNavController(), Announcement())
}