package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.telegram.bot.data.enums.BotGameStage
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.ProblemRepository
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData

@Component
class ApealFetcher(
    private val problemRepository: ProblemRepository,
    private val teamRepository: TeamRepository,
    private val userInfoRepository: UserInfoRepository,
) : GeneralFetcher() {
    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
        exp: ExpContainer,
        update: Update,
    ) {
        val tui = userResponse.initiator.tui ?: return

        if (!(exp.botGameStage == BotGameStage.APPEAL && userResponse.initiator.isCaptain)) {
            return
        }

        val problemId = userResponse.problemId ?: return
        val team = userResponse.initiatorTeam ?: return

        if (problemId !in team.completedProblems || problemId in team.appealedProblems) {
            return
        }

        team.appealedProblems.add(problemId)
        teamRepository.save(team)

        val problemCategory = problemRepository.findById(problemId).get().category
        val problemCost = problemRepository.findById(problemId).get().cost
        val realAnswer = problemRepository.findById(problemId).get().answers
        bot.execute(
            SendMessage(
                "920061911",
                "Команда ${team.teamName}. Задача:$problemCategory $problemCost." +
                    "\n Первый ответ команды: " +
                    "\n второй ответ команды: " +
                    "\n верый ответ $realAnswer", // TODO добавить ответы команды
            ).also {
                it.replyMarkup = createChooseKeyboard(userResponse)
            },
        )
    }

    private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) =
        InlineKeyboardMarkup().also { it.keyboard = keyboard }

    private fun createChooseKeyboard(
        userResponse: UserResponse,
    ) = createKeyboard(
        listOf(
            listOf(
                InlineKeyboardButton("Первый ответ верный ✅").also { it.callbackData = "First_true ${userResponse.initiatorTeam?.id} ${userResponse.problemId}" },
                InlineKeyboardButton("Второй ответ верый ✅").also { it.callbackData = "Second_true ${userResponse.initiatorTeam?.id} ${userResponse.problemId}" },
                InlineKeyboardButton("Ничего неверно ❌").also { it.callbackData = "No_true" },

            ),
        ),
    )
}
