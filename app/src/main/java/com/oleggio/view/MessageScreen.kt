package com.oleggio.view

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.oleggio.topchat.api.ApiService
import com.oleggio.topchat.api.Message
import com.oleggio.topchat.viewmodel.MessageListViewModel
import kotlinx.coroutines.launch

@Composable
fun MessageScreenChooser(navController: NavController) {
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        ChatScreen(navController)
    } else {
        MessageScreen(navController)
    }
}

@Composable
fun ViewImageFullscreen(imageUrl: String, onBack : () -> Unit) {
    BackHandler(onBack = onBack)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(enabled = false) {}
    ) {
        AsyncImage(
            model = ApiService.API_URL + "img/" + Uri.decode(imageUrl),
            contentDescription = "Full Screen Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}

@Composable
fun MessageItem(message: Message, navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (message.data?.image != null) {
                AsyncImage(
                    modifier = Modifier.fillMaxWidth()
                        .clickable {
                            navController.navigate("viewImage/${Uri.encode(message.data.image.link)}")
                        },
                    contentScale = ContentScale.Crop,
                    model = ApiService.API_URL + "thumb/" + message.data.image.link,
                    contentDescription = null
                )
            }
            Text(text = message.from, style = MaterialTheme.typography.titleSmall)
            Text(text = message.data?.text?.text ?: "", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(navController: NavController, messageListViewModel: MessageListViewModel = hiltViewModel()) {
    val messages by messageListViewModel.messageList.collectAsState()

    BackHandler(enabled = messages.isNotEmpty()) {
        navController.popBackStack()
        messageListViewModel.clearMessageList()
        messageListViewModel.clearSelected()
    }

    val selected by messageListViewModel.selectedChat.collectAsState()
    val messageField by messageListViewModel.messageInput.collectAsState()
    val isLoading by messageListViewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()
    val context = LocalContext.current

    val errorMessage by messageListViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == messages.size - 1) {
            messageListViewModel.getMessageList(context, lastId = messages.last().id)
        }
    }

    LaunchedEffect(selected) {
        if (selected != "") {
            messageListViewModel.clearMessageList()
            messageListViewModel.getMessageList(context, lastId = Int.MAX_VALUE)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selected) },
            )
        },
        snackbarHost = {SnackbarHost(hostState = snackbarHostState)},
        content = { paddingValues ->
            LaunchedEffect(errorMessage) {
                if (errorMessage != null) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = errorMessage ?: "An uknown error occured",
                            actionLabel = "Ok"
                        )
                        messageListViewModel.clearError()
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column (
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = listState,
                        reverseLayout = true
                    ) {
                        items(messages) { message ->
                            MessageItem(message, navController)
                        }

                        if (isLoading) {
                            item {
                                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
                            }
                        }
                    }
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = messageField,
                            onValueChange = { messageListViewModel.onTextChanged(it) },
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.Gray),
                            singleLine = true,
                        )
                        Button(
                            onClick = {
                                messageListViewModel.sendMessage(context)
                                messageListViewModel.cleanUserInput()
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text(text = "Send")
                            }
                        }
                    }
                }
            }
        }
    )
}