package de.schlafgarten.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.schlafgarten.app.data.SleepCsv
import de.schlafgarten.app.data.SleepEntry
import de.schlafgarten.app.ui.EntryDialog
import de.schlafgarten.app.ui.GoalDialog
import de.schlafgarten.app.ui.HeatmapMode
import de.schlafgarten.app.ui.SleepHeatmap
import de.schlafgarten.app.ui.StatsPanel
import de.schlafgarten.app.ui.theme.SchlafgartenTheme
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val viewModel: SleepViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchlafgartenTheme {
                SleepScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepScreen(viewModel: SleepViewModel) {
    val entries by viewModel.entries.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val error by viewModel.error.collectAsState()
    val goalMinutes by viewModel.goalMinutes.collectAsState()
    var mode by remember { mutableStateOf(HeatmapMode.YEAR) }
    var editing by remember { mutableStateOf<SleepEntry?>(null) }
    var showNew by remember { mutableStateOf(false) }
    var showGoal by remember { mutableStateOf(false) }
    var clickedDay by remember { mutableStateOf<LocalDate?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let {
            snackbar.showSnackbar(it)
            viewModel.error.value = null
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(SleepCsv.build(entries ?: emptyList()).toByteArray(Charsets.UTF_8))
                        } ?: throw IllegalStateException("Kein Zugriff auf Datei")
                    }.isSuccess
                }
                snackbar.showSnackbar(
                    if (ok) "Export gespeichert." else "Export fehlgeschlagen.",
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schlafgarten") },
                actions = {
                    IconButton(
                        onClick = {
                            if (!entries.isNullOrEmpty()) {
                                exportLauncher.launch("schlafgarten-export.csv")
                            } else {
                                scope.launch { snackbar.showSnackbar("Noch nichts zu exportieren.") }
                            }
                        },
                    ) { Icon(Icons.Filled.Share, contentDescription = "Als CSV exportieren") }
                    IconButton(onClick = { showGoal = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Schlafziel einstellen")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNew = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nacht eintragen") },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Tippe auf einen Tag, um ihn einzutragen oder zu ändern.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mode == HeatmapMode.YEAR,
                    onClick = { mode = HeatmapMode.YEAR },
                    label = { Text("Jahr") },
                )
                FilterChip(
                    selected = mode == HeatmapMode.MONTH,
                    onClick = { mode = HeatmapMode.MONTH },
                    label = { Text("Monat") },
                )
            }
            Spacer(Modifier.height(16.dp))
            when (entries) {
                null -> CircularProgressIndicator()
                else -> SleepHeatmap(
                    entries = entries!!,
                    mode = mode,
                    onDayClick = { day -> clickedDay = day },
                )
            }
            Spacer(Modifier.height(24.dp))
            StatsPanel(entries ?: emptyList(), goalMinutes)
            Spacer(Modifier.height(96.dp))
        }
    }

    if (showGoal) {
        GoalDialog(
            currentGoalMinutes = goalMinutes,
            onDismiss = { showGoal = false },
            onSave = { minutes ->
                viewModel.setGoal(minutes)
                showGoal = false
            },
        )
    }

    when {
        showNew -> EntryDialog(
            initial = null,
            busy = busy,
            onDismiss = { showNew = false },
            onSave = { entry ->
                viewModel.save(entry) {
                    showNew = false
                }
            },
        )
        editing != null -> EntryDialog(
            initial = editing,
            busy = busy,
            onDismiss = { editing = null },
            onSave = { entry ->
                viewModel.save(entry) {
                    editing = null
                }
            },
            onDelete = { entry ->
                viewModel.delete(entry) {
                    editing = null
                }
            },
        )
        clickedDay != null -> {
            val day = clickedDay!!
            val existing = entries?.firstOrNull { it.wakeDate == day.toString() }
            EntryDialog(
                initial = existing,
                busy = busy,
                onDismiss = { clickedDay = null },
                defaultDate = day,
                onSave = { entry ->
                    viewModel.save(entry) {
                        clickedDay = null
                    }
                },
                onDelete = if (existing != null) {
                    { entry ->
                        viewModel.delete(entry) {
                            clickedDay = null
                        }
                    }
                } else {
                    null
                },
            )
        }
    }
}
