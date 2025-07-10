package com.heard.mobile.ui.screens.group

import com.heard.mobile.R



data class GroupPlaceHolder(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val maxMembers: Int,
    val leader: User,
    val members: List<User> = emptyList()
)

data class User(
    val id: String,
    val name: String,
    val avatar: Int? = null,
    val isLeader: Boolean = false
)



data class AvailableGroup(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val maxMembers: Int,
    val category: String
)

val mockUserGroupPlaceHolder = GroupPlaceHolder(
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

