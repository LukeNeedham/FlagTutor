package com.flagtutor.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** The colours used by this app, named by how they are used rather than by Material slot. */
@Immutable
data class AppColors(
    /** Page background. */
    val background: Color,
    /** Content (text, icons) drawn directly on [background]; also the fill of primary action buttons. */
    val onBackground: Color,
    /** Default body text. */
    val text: Color,
    /** De-emphasised text and icons. */
    val textSecondary: Color,
    /** Brand accent, e.g. progress indicators and highlighted icons. */
    val primary: Color,
    val onPrimary: Color,
    /** Default fill of an answer option, and the content on it. */
    val option: Color,
    val onOption: Color,
    /** Fill of the correct answer, and the content on it. */
    val correct: Color,
    val onCorrect: Color,
    val error: Color,
    /** Fill of raised cards. */
    val card: Color,
    val divider: Color,
    val logoBackground: Color,
)

internal val LightAppColors = AppColors(
    background = LightBackground,
    onBackground = LightOnBackground,
    text = LightText,
    textSecondary = LightTextSecondary,
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    option = LightOption,
    onOption = LightOnOption,
    correct = LightCorrect,
    onCorrect = LightOnCorrect,
    error = LightError,
    card = LightCard,
    divider = LightDivider,
    logoBackground = LightLogoBackground,
)

internal val DarkAppColors = AppColors(
    background = DarkBackground,
    onBackground = DarkOnBackground,
    text = DarkText,
    textSecondary = DarkTextSecondary,
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    option = DarkOption,
    onOption = DarkOnOption,
    correct = DarkCorrect,
    onCorrect = DarkOnCorrect,
    error = DarkError,
    card = DarkCard,
    divider = DarkDivider,
    logoBackground = DarkLogoBackground,
)
