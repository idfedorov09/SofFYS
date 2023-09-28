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
    @Column(name = "problems_pool", columnDefinition = "BIGINT")
    val problemsPool: MutableList<Long> = mutableListOf(),

    @ElementCollection
    @Column(name = "completed_problems", columnDefinition = "BIGINT")
    val completedProblems: MutableList<Long> = mutableListOf(),

    @Column(name = "points")
    val points: Long = 0,

    /**
     * Хэш тг картинки доски команды
     */
    @Column(name = "last_board_hash", columnDefinition = "TEXT")
    val lastBoardHash: String? = null,

    @ElementCollection
    @Column(name = "appealed_problems", columnDefinition = "BIGINT")
    val appealedProblems: MutableList<Long> = mutableListOf(),
)
