

package ru.idfedorov09.telegram.bot.data.repo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.idfedorov09.telegram.bot.data.model.Action

interface ActionRepository : JpaRepository<Action, Long> {

    @Query(
        "SELECT COUNT(a) " +
            "FROM actions_table a " +
            "WHERE a.teamId = :teamId AND a.problemId = :problemId AND a.action = 'SEND_ANSWER'",
    )
    fun countActionsByTeamIdAndProblemIdAndAction(teamId: Long, problemId: Long): Long

    @Query(
        "SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END " +
            "FROM actions_table a " +
            "WHERE a.teamId = :teamId AND a.problemId = :problemId AND a.action = 'SEND_ANSWER' " +
            "AND a.isCorrectAnswer = false",
    )
    fun countByTeamIdAndProblemIdAndActionAndIsCorrectAnswer(teamId: Long, problemId: Long): Boolean
}
