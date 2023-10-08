/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.wearos_todoapp.presentation

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Devices
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.wear.compose.material.Text
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import com.example.wearos_todoapp.R
import com.example.wearos_todoapp.presentation.theme.WearostodoappTheme

class MainActivity : ComponentActivity() {
    private lateinit var taskDao: TaskDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val taskDatabase = TaskDatabase.getInstance(this)
        taskDao = taskDatabase.taskDao()

        setContent {
            WearostodoappTheme {
                TodoApp(taskDao = taskDao, coroutineScope = this.lifecycleScope)
            }
        }
    }
}

@Composable
fun TodoApp(taskDao: TaskDao, coroutineScope: CoroutineScope) {
    var taskText by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(emptyList<Task>()) }

    LaunchedEffect(Unit) {
        // Fetch tasks when the component is first launched
        tasks = taskDao.getAllTasks()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()

        ) {

            Spacer(modifier = Modifier.height(16.dp))

            /*Button(
                onClick = {
                    if (taskText.isNotBlank()) {
                        val newTask = Task(taskText = taskText)
                        coroutineScope.launch(Dispatchers.IO) {
                            taskDao.insert(newTask)
                            taskText = ""
                            // Refresh the task list to display the new task
                            tasks = taskDao.getAllTasks()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp)
            ) {
                Text(
                    stringResource(id = R.string.button_add_task),
                    style = MaterialTheme.typography.body1
                )
            }*/

            UserInputScreen(
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn {
                items(tasks) {task ->
                    TaskItemRow(task) {
                        // Callback to delete a task
                        coroutineScope.launch(Dispatchers.IO) {
                            taskDao.delete(task)
                            tasks = taskDao.getAllTasks()
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun TaskItemRow(task: Task, onDeleteTask: (Task) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(72.dp)  // Adjust the height for centering vertically
    ) {
        Text(
            text = task.taskText,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)  // Align text to center vertically
                .padding(16.dp)
        )

        /*Button(
            onClick = { onDeleteTask(task) },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                stringResource(id = R.string.button_remove_task),
                style = MaterialTheme.typography.body1
            )
        }*/

        UserInputScreen(
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun UserInputScreen(
    modifier: Modifier = Modifier
) {
    val defaultText = stringResource(id = R.string.edit_user_input)
    var userInput by remember { mutableStateOf(defaultText) }
    val inputTextKey = "input_text"

    val remoteInputs: List<RemoteInput> = listOf(
        RemoteInput.Builder(inputTextKey)
            .setLabel(stringResource(id = R.string.edit_user_input))
            .wearableExtender {
                setEmojisAllowed(true)
                setInputActionType(EditorInfo.IME_ACTION_DONE)
            }.build(),
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it.data?.let { data ->
            val results: Bundle = RemoteInput.getResultsFromIntent(data)
            val newInputText: CharSequence? = results.getCharSequence(inputTextKey)
            userInput = newInputText?.toString() ?: ""
        }
    }

    val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
    RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Row(modifier = Modifier.fillMaxWidth(1.25f)) {
            Text(text = userInput, Modifier.weight(1f).align(Alignment.CenterVertically))
            CompactButton(
                onClick = { launcher.launch(intent) },
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.edit_user_input)
                )
            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val tasks = emptyList<Task>() // Initialize with an empty list

    WearostodoappTheme {
        TodoApp(
            taskDao = mockTaskDao(tasks),
            coroutineScope = rememberCoroutineScope()
        )
    }
}

// Mock TaskDao for preview
fun mockTaskDao(tasks: List<Task>): TaskDao {
    return object : TaskDao {
        override suspend fun insert(task: Task) {
            // This is just a mock, so we won't actually modify the list for insert
        }

        override suspend fun getAllTasks(): List<Task> {
            return tasks.toList()
        }

        override suspend fun delete(task: Task) {
            // This is just a mock, so we won't actually modify the list for delete
        }
    }
}
