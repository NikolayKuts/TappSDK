package com.tapp.sdk.library.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent

internal object TappWidgetIntentFactory {

    private const val SPIN_ACTION = "com.tapp.sdk.library.widget.action.SPIN"

    fun Intent.hasSpinAction(): Boolean = action == SPIN_ACTION

    fun createSpinPendingIntent(
        context: Context,
        appWidgetIdentifier: Int
    ): PendingIntent {
        val intent = Intent(context, TappWidgetProvider::class.java).apply {
            action = SPIN_ACTION
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIdentifier)
        }

        return PendingIntent.getBroadcast(
            context,
            appWidgetIdentifier,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
