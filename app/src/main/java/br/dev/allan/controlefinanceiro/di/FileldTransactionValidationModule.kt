package br.dev.allan.controlefinanceiro.di

import br.dev.allan.controlefinanceiro.domain.usecase.ValidateAmount
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateCategory
import br.dev.allan.controlefinanceiro.domain.usecase.ValidateTitle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ValidationModule {

    @Provides
    fun provideValidateTitle(): ValidateTitle = ValidateTitle()

    @Provides
    fun provideValidateAmount(): ValidateAmount = ValidateAmount()

    @Provides
    fun provideValidateCategory(): ValidateCategory = ValidateCategory()

}