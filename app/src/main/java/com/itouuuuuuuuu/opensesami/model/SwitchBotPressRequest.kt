package com.itouuuuuuuuu.opensesami.model

class SwitchBotPressRequest(
    private val command: String = "press",
    private val parameter: String = "default",
    private val commandType: String = "command"
)