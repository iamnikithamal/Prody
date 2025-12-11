package com.prody.prashant.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

/**
 * Manager for haptic feedback providing rich tactile experiences.
 * Supports different feedback types for various interactions.
 */
class HapticFeedbackManager(
    private val context: Context,
    private val view: View?
) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Light tap for buttons and minor interactions
     */
    fun lightTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } else {
            performVibration(10)
        }
    }

    /**
     * Confirmation feedback for successful actions
     */
    fun confirm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            performVibration(longArrayOf(0, 30, 50, 30))
        }
    }

    /**
     * Rejection/error feedback
     */
    fun reject() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            performVibration(longArrayOf(0, 50, 30, 50, 30, 50))
        }
    }

    /**
     * Heavy click for important actions
     */
    fun heavyClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY_RELEASE)
        } else {
            performVibration(50)
        }
    }

    /**
     * Selection change feedback
     */
    fun selectionChange() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view?.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
        } else {
            performVibration(5)
        }
    }

    /**
     * Long press recognition feedback
     */
    fun longPress() {
        view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    /**
     * Achievement unlocked - celebratory pattern
     */
    fun achievementUnlocked() {
        performVibration(longArrayOf(0, 50, 100, 50, 100, 100))
    }

    /**
     * Streak continued - positive reinforcement
     */
    fun streakContinued() {
        performVibration(longArrayOf(0, 30, 50, 60))
    }

    /**
     * Badge earned - special celebration
     */
    fun badgeEarned() {
        performVibration(longArrayOf(0, 50, 80, 50, 80, 80, 80, 80))
    }

    /**
     * Level up - major achievement
     */
    fun levelUp() {
        performVibration(longArrayOf(0, 100, 100, 100, 100, 200))
    }

    /**
     * Message sent feedback
     */
    fun messageSent() {
        performVibration(longArrayOf(0, 20, 30, 40))
    }

    /**
     * Journal saved feedback
     */
    fun journalSaved() {
        performVibration(longArrayOf(0, 30, 60, 50))
    }

    /**
     * Swipe feedback
     */
    fun swipe() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view?.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
        } else {
            performVibration(15)
        }
    }

    /**
     * Toggle switch feedback
     */
    fun toggle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view?.performHapticFeedback(HapticFeedbackConstants.TOGGLE_ON)
        } else {
            performVibration(25)
        }
    }

    private fun performVibration(duration: Long) {
        if (vibrator?.hasVibrator() != true) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun performVibration(pattern: LongArray) {
        if (vibrator?.hasVibrator() != true) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
}

/**
 * Remember a HapticFeedbackManager instance tied to the current composition.
 */
@Composable
fun rememberHapticFeedback(): HapticFeedbackManager {
    val context = LocalContext.current
    val view = LocalView.current
    return remember(context, view) {
        HapticFeedbackManager(context, view)
    }
}

/**
 * Extension to convert Compose HapticFeedback to our custom manager actions.
 */
@Composable
fun HapticFeedback.performCustomFeedback(type: CustomHapticType) {
    val manager = rememberHapticFeedback()
    when (type) {
        CustomHapticType.LIGHT_TAP -> manager.lightTap()
        CustomHapticType.CONFIRM -> manager.confirm()
        CustomHapticType.REJECT -> manager.reject()
        CustomHapticType.HEAVY_CLICK -> manager.heavyClick()
        CustomHapticType.SELECTION -> manager.selectionChange()
        CustomHapticType.LONG_PRESS -> manager.longPress()
        CustomHapticType.ACHIEVEMENT -> manager.achievementUnlocked()
        CustomHapticType.STREAK -> manager.streakContinued()
        CustomHapticType.BADGE -> manager.badgeEarned()
        CustomHapticType.LEVEL_UP -> manager.levelUp()
        CustomHapticType.MESSAGE_SENT -> manager.messageSent()
        CustomHapticType.JOURNAL_SAVED -> manager.journalSaved()
        CustomHapticType.SWIPE -> manager.swipe()
        CustomHapticType.TOGGLE -> manager.toggle()
    }
}

enum class CustomHapticType {
    LIGHT_TAP,
    CONFIRM,
    REJECT,
    HEAVY_CLICK,
    SELECTION,
    LONG_PRESS,
    ACHIEVEMENT,
    STREAK,
    BADGE,
    LEVEL_UP,
    MESSAGE_SENT,
    JOURNAL_SAVED,
    SWIPE,
    TOGGLE
}
