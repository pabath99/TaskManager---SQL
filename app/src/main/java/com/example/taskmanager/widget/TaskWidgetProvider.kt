package com.example.taskmanager.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.taskmanager.R
import com.example.taskmanager.repository.TaskRepository
import com.example.taskmanager.ui.activity.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            // Create a RemoteViews object, binding it to the widget_task layout
            val views = RemoteViews(context.packageName, R.layout.widget_task)

            // Use coroutines to fetch tasks from the Room database
            val repository = TaskRepository(context)
            CoroutineScope(Dispatchers.IO).launch {
                // Fetch upcoming tasks from the database
                val upcomingTasks = repository.getUpcomingTasksSync()

                // Update the widget UI on the main thread
                withContext(Dispatchers.Main) {
                    // Set task details or placeholder if no upcoming tasks
                    if (upcomingTasks.isEmpty()) {
                        views.setTextViewText(R.id.tvWidgetTask, "No upcoming tasks")
                        views.setTextViewText(R.id.tvWidgetDueDate, "")
                    } else {
                        val task = upcomingTasks.first() // Show the first upcoming task
                        views.setTextViewText(R.id.tvWidgetTask, task.title)
                        views.setTextViewText(R.id.tvWidgetDueDate, "Due: ${task.dueDateFormatted()}")
                    }

                    // Set up click handler to open the MainActivity when the widget is clicked
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    views.setOnClickPendingIntent(R.id.tvWidgetTask, pendingIntent)

                    // Update the widget
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
