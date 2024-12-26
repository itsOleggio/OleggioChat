package com.oleggio.view

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.oleggio.topchat.api.Chat
import com.oleggio.topchat.viewmodel.ChatListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController : NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onClick = {
                    scope.launch { drawerState.close() }
                },
                navController = navController
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Oleggio's topChat") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            content = { paddingValues ->
                when (LocalConfiguration.current.orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> ChatPortrait(navController, paddingValues)
                    Configuration.ORIENTATION_LANDSCAPE -> ChatAlbum(navController, paddingValues)
                }
            }
        )
    }
}

@Composable
fun DrawerContent(navController: NavController, onClick: () -> Unit, chatListViewModel: ChatListViewModel = hiltViewModel()) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        var login = chatListViewModel.getUsername()
        if (login == null) {
            login = "Unauthorized"
        }
        Text(
            text = "User <$login>",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider()

        if (login == "Unauthorized") {
            DrawerItem("Login", Icons.Default.Person) {
                onClick()
                navController.navigate("login")
            }
        }

        DrawerItem("Chats", Icons.Default.AccountBox) {
            onClick()
            navController.navigate("chats")
        }

        if (login != "Unauthorized") {
            DrawerItem("Logout", Icons.Default.Close) {
                onClick()
                chatListViewModel.logout()
                navController.navigate("login")
            }
        }

    }
}

@Composable
fun DrawerItem(title: String, icon: ImageVector, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(title) }
            .padding(16.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ChatPortrait(navController: NavController, paddingValues: PaddingValues) {
    Box(modifier = Modifier.padding(paddingValues)) {
        ChatList(navController)
    }
}

@Composable
fun ChatList(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = {2})
    val tabs = listOf("Channels", "Users")
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        ) {
            tabs.forEachIndexed { index, title ->
                Tab (
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            TabContent(page, navController)
        }
    }
}

@Composable
fun TabContent(page : Int, navController: NavController) {
    when (page) {
        0 -> ChannelList(navController)
        1 -> UserList(navController)
    }
}

@Composable
fun ChannelList(navController: NavController, chatListViewModel: ChatListViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val errorMessage by chatListViewModel.error.collectAsState()
    val chanList by chatListViewModel.chatList.collectAsState()

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { chatListViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                Button(onClick = { chatListViewModel.clearError() }) {
                    Text("OK")
                }
            },
        )
    }

    LaunchedEffect(chanList) {
        if (chanList.isEmpty()) {
            chatListViewModel.getChatList(context)
        }
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(chanList) { channel ->
            ChatItem(channel, navController)
        }
    }
}

@Composable
fun ChatItem(chat: Chat, navController: NavController, chatListViewModel: ChatListViewModel = hiltViewModel()) {
    val orientation = LocalConfiguration.current.orientation
    Surface (
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clickable {
                    chatListViewModel.selectChat(chat.name)
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        navController.navigate("messages")
                    }
                }
        ) {
            Text(text = chat.name, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun UserList(navController: NavController, chatListViewModel: ChatListViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val errorMessage by chatListViewModel.error.collectAsState()
    val userList by chatListViewModel.userList.collectAsState()

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { chatListViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                Button(onClick = { chatListViewModel.clearError() }) {
                    Text("OK")
                }
            },
        )
    }

    LaunchedEffect(userList) {
        if (userList.isEmpty()) {
            chatListViewModel.getUserList(context)
        }
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(userList) { channel ->
            ChatItem(channel, navController)
        }
    }
}

@Composable
fun ChatAlbum(navController: NavController, paddingValues: PaddingValues = PaddingValues(0.dp)) {
    Row (modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            ChatList(navController)
        }

        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            MessageScreen(navController)
        }
    }
}
