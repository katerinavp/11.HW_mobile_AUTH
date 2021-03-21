package org.example.dto.request

data class PasswordChangeRequestDto(
    val old: String,
    val new: String
)