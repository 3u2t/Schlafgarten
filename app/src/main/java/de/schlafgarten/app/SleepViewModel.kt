package de.schlafgarten.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.schlafgarten.app.data.DEFAULT_GOAL_MINUTES
import de.schlafgarten.app.data.GoalStore
import de.schlafgarten.app.data.SleepDatabase
import de.schlafgarten.app.data.SleepEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SleepViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = SleepDatabase.get(application).sleepDao()
    private val goalStore = GoalStore(application)
    val entries: StateFlow<List<SleepEntry>?> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val busy = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    val goalMinutes = MutableStateFlow(goalStore.getGoalMinutes())

    fun save(entry: SleepEntry, onSuccess: () -> Unit) = mutate(onSuccess) { dao.upsert(entry) }

    fun delete(entry: SleepEntry, onSuccess: () -> Unit) = mutate(onSuccess) { dao.delete(entry) }

    fun setGoal(minutes: Long) {
        val clamped = minutes.coerceIn(240L, 720L)
        goalStore.setGoalMinutes(clamped)
        goalMinutes.value = clamped
    }

    fun defaultGoal(): Long = DEFAULT_GOAL_MINUTES

    private fun mutate(onSuccess: () -> Unit, action: suspend () -> Unit) {
        if (busy.value) return
        viewModelScope.launch {
            busy.value = true
            try {
                action()
                onSuccess()
            } catch (_: Exception) {
                error.value = "Die Änderung konnte nicht gespeichert werden. Bitte versuche es erneut."
            } finally {
                busy.value = false
            }
        }
    }
}
