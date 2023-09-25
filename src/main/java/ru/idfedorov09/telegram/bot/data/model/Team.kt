package ru.idfedorov09.telegram.bot.data.model

import jakarta.persistence.*

@Entity
@Table(name = "team_table")
data class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = -1,

    @Column(name = "team_name", columnDefinition = "TEXT")
    val teamName: String? = null,

    @ElementCollection
    @Column(name = "current_problems", columnDefinition = "BIGINT[]")
    val problemsPool: List<Long> = mutableListOf(),

    @ElementCollection
    @Column(name = "completed_problems", columnDefinition = "BIGINT[]")
    val completedProblems: List<Long> = mutableListOf(),

    @Column(name = "points")
    val points: Long = 0,

    /**
     * Хэш тг картинки доски команды
     */
    @Column(name = "last_board_cache", columnDefinition = "TEXT")
    val lastBoardCache: String? = null
)
