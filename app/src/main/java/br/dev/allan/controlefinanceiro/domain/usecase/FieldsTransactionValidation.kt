package br.dev.allan.controlefinanceiro.domain.usecase

import br.dev.allan.controlefinanceiro.domain.model.TransactionCategory

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)

class ValidateTitle {
    fun execute(title: String): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult(successful = false, errorMessage = "Campo obrigatorio")
        }
        return ValidationResult(successful = true)
    }
}

class ValidateAmount {
    fun execute(amount: String): ValidationResult {
        val value = amount.replace(",", ".").toDoubleOrNull()
        if (value == null || value <= 0.0) {
            return ValidationResult(
                successful = false,
                errorMessage = "Campo Obrigatorio, Insira um valor válido maior que zero"
            )
        }
        return ValidationResult(successful = true)
    }
}

class ValidateCategory {
    fun execute(category: TransactionCategory?): ValidationResult {
        if (category == null) {
            return ValidationResult(successful = false, errorMessage = "Selecione uma categoria")
        }
        return ValidationResult(successful = true)
    }
}
