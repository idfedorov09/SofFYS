
package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.idfedorov09.telegram.bot.data.model.Action
import ru.idfedorov09.telegram.bot.data.model.IsAnswer
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.model.CountActionsByTeamIdAndProblemIdAndAction
import ru.idfedorov09.telegram.bot.data.repo.ActionRepository
import ru.idfedorov09.telegram.bot.flow.InjectData

@Component
class ActionFetcher(
    private val actionRepository: ActionRepository,
) : GeneralFetcher() {
    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        isAnswer: IsAnswer?,
        countActionsByTeamIdAndProblemIdAndAction: CountActionsByTeamIdAndProblemIdAndAction?,
    ) {
        isAnswer ?: return
        val countAnswers = countActionsByTeamIdAndProblemIdAndAction?.countActionsByTeamIdAndProblemIdAndAction ?: 1L
        // TODO: сохранять только нуные нам action
        val action = Action(
            teamId = userResponse.initiatorTeam?.id,
            time = userResponse.receiveTime,
            action = userResponse.action,
            problemId = userResponse.problemId,
            isCorrectAnswer = isAnswer.isAnswer,
            correctAnswerAttempt = countAnswers,
        )

        actionRepository.save(action)
    }
}
