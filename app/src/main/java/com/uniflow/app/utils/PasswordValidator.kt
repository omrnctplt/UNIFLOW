package com.uniflow.app.utils

object PasswordValidator {
    private const val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"

    fun isValid(password: String): Boolean {
        return password.matches(Regex(PASSWORD_PATTERN))
    }

    fun getErrorMessage(): String {
        return "Şifre en az 8 karakter olmalı, en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter içermelidir."
    }
}
