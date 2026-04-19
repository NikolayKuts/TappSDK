package com.tapp.sdk.library.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.tapp.sdk.library.internal.TappSdkDiContainer
import com.tapp.sdk.library.internal.logD
import com.tapp.sdk.library.widget.TappWidgetIntentFactory.hasSpinAction

class TappWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIdentifiers: IntArray
    ) {
        appWidgetIdentifiers.forEach { appWidgetIdentifier ->
            updateTappWidget(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetIdentifier = appWidgetIdentifier,
                wheelRotationDegrees = 0f
            )
        }
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.hasSpinAction()) {
            val applicationContext = context.applicationContext
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val appWidgetIdentifier = intent.getClickedAppWidgetIdentifier() ?: return
            val widgetConfiguration = TappSdkDiContainer.widgetConfigurationStorage
                .getWidgetConfiguration(appWidgetIdentifier)
            val pendingResult = goAsync()
            val animationConfiguration = widgetConfiguration.animationConfiguration

            logD("onReceive: $animationConfiguration")

            TappWidgetAnimationController.startSpinAnimation(
                appWidgetIdentifier = appWidgetIdentifier,
                animationConfiguration = animationConfiguration,
                pendingResult = pendingResult,
                updateTappWidget = { wheelRotationDegrees ->
                    updateTappWidget(
                        context = applicationContext,
                        appWidgetManager = appWidgetManager,
                        appWidgetIdentifier = appWidgetIdentifier,
                        wheelRotationDegrees = wheelRotationDegrees
                    )
                },
            )
            return
        }

        super.onReceive(context, intent)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIdentifier: Int,
        newOptions: Bundle
    ) {
        updateTappWidget(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetIdentifier = appWidgetIdentifier,
            wheelRotationDegrees = 0f
        )
    }

    override fun onDeleted(
        context: Context,
        appWidgetIdentifiers: IntArray
    ) {
        appWidgetIdentifiers.forEach { appWidgetIdentifier ->
            TappSdkDiContainer.widgetConfigurationStorage
                .removeWidgetConfiguration(appWidgetIdentifier)
        }
    }

    private fun updateTappWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIdentifier: Int,
        wheelRotationDegrees: Float
    ) {
        val widgetConfiguration = TappSdkDiContainer.widgetConfigurationStorage
            .getWidgetConfiguration(appWidgetIdentifier)
        val widgetAssetFiles = TappWidgetAssetFileResolver.resolve(
            widgetConfiguration = widgetConfiguration,
            assetStorage = TappSdkDiContainer.assetStorage
        )
        val remoteViews = TappWidgetRenderer.createRemoteViews(
            context = context,
            appWidgetIdentifier = appWidgetIdentifier,
            widgetAssetFiles = widgetAssetFiles,
            wheelRotationDegrees = wheelRotationDegrees
        )

        appWidgetManager.updateAppWidget(appWidgetIdentifier, remoteViews)
    }

    private fun Intent.getClickedAppWidgetIdentifier(): Int? {
        val appWidgetIdentifier = getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        return appWidgetIdentifier.takeIf {
            it != AppWidgetManager.INVALID_APPWIDGET_ID
        }
    }
}
