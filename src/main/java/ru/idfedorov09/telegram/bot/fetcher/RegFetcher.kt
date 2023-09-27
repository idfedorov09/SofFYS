package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.telegram.bot.data.enums.RegistrationStage
import ru.idfedorov09.telegram.bot.data.enums.UserResponseType
import ru.idfedorov09.telegram.bot.data.model.Team
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService

@Component
class RegFetcher(
    private val redisService: RedisService,
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

        when (exp.registrationStage) {
            RegistrationStage.CAP_REGISTRATION -> {
                when (userResponse.userResponseType) {
                    UserResponseType.MESSAGE_RESPONSE -> {
                        if (userResponse.initiator.isCaptain) {
                            return
                        }
                        selectTeamName(tui, userResponse, bot)
                    }

                    UserResponseType.BUTTON_RESPONSE -> {
                        confirmTeamCreate(tui, update)
                    }
                    else -> return
                }
            }

            RegistrationStage.TEAM_REGISTRATION -> {
                if (userResponse.userResponseType != UserResponseType.BUTTON_RESPONSE) {
                    return
                }
                messageFromNoCap(tui, update)
            }

            else -> return
        }
    }

            private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) =
                 InlineKeyboardMarkup().also { it.keyboard = keyboard }

    private fun createChooseKeyboard(
        teamName: String?,
    ) = createKeyboard(
        listOf(
            listOf(
                InlineKeyboardButton("Да ✅").also { it.callbackData = "team_confirm $teamName" },
                InlineKeyboardButton("Нет ❌").also { it.callbackData = "team_reg_cancel" },
            ),
        ),
    )

    private fun selectTeamName(
        tui: String,
        userResponse: UserResponse,
        bot: TelegramPollingBot,
    ) {
        val teamName = userResponse.messageText?.trim() ?: return

        teamRepository.findAll()
            .forEach { team ->
                if (team.teamName?.lowercase() == teamName.lowercase()) {
                    bot.execute(SendMessage(tui, "Команда с таким названием уже существует!!!"))
                    return
                }
            }

        bot.execute(
            SendMessage(
                tui,
                "Вы точно хотите зарегистрировать команду **$teamName**?",
            ).also {
                it.enableMarkdown(true)
                it.replyMarkup = createChooseKeyboard(teamName)
            },
        )
    }

    private fun confirmTeamCreate(
        tui: String,
        update: Update,
    ) {
        val answer = update.callbackQuery.data
        if (answer.startsWith("team_confirm")) {
            val thisUser = userInfoRepository.findByTui(tui) ?: return

            val savedTeam: Team =
                teamRepository.save(
                    Team(
                        teamName = answer.substring(13),
                    ),
                )
            userInfoRepository.save(
                thisUser.copy(
                    isCaptain = true,
                    teamId = savedTeam.id,
                ),
            )
        }
    }

    private fun messageFromNoCap(
        tui: String,
        update: Update,
    ) {
        val answer = update.callbackQuery.data
        val thisUser = userInfoRepository.findByTui(tui) ?: return
        userInfoRepository.save(
            thisUser.copy(
                teamId = answer.toLongOrNull(),
            ),
        )
    }
}
