package com.heard.mobile.ui.screens.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.heard.mobile.R
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.composables.CustomBottomBar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

// Data classes


// Mock data
val mockCurrentUser = User(
    id = "current_user",
    name = "Tu",
    avatar = R.drawable.ic_launcher_foreground
)

val mockUserGroup = Group(
    id = "group_1",
    name = "Gruppo1",
    description = "Gruppo per attività sportive e benessere",
    memberCount = 3,
    maxMembers = 10,
    leader = User(
        id = "leader_1",
        name = "Marco Rossi",
        avatar = R.drawable.ic_launcher_foreground,
        isLeader = true
    ),
    members = listOf(
        User(
            id = "leader_1",
            name = "Marco Rossi",
            avatar = R.drawable.ic_launcher_foreground,
            isLeader = true
        ),
        User(
            id = "member_1",
            name = "Anna Bianchi",
            avatar = R.drawable.ic_launcher_foreground
        ),
        User(
            id = "current_user",
            name = "Tu",
            avatar = R.drawable.ic_launcher_foreground
        )
    )
)

val mockAvailableGroups = listOf(
    AvailableGroup(
        id = "group_2",
        name = "Runners Club",
        description = "Gruppo per appassionati di corsa",
        memberCount = 5,
        maxMembers = 15,
        category = "Sport"
    ),
    AvailableGroup(
        id = "group_3",
        name = "Fitness Team",
        description = "Allenamenti in palestra insieme",
        memberCount = 8,
        maxMembers = 12,
        category = "Fitness"
    ),
    AvailableGroup(
        id = "group_4",
        name = "Yoga & Mindfulness",
        description = "Pratiche di yoga e meditazione",
        memberCount = 6,
        maxMembers = 10,
        category = "Benessere"
    )
)

