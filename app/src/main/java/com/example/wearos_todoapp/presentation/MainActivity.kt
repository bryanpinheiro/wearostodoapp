/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.wearos_todoapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Devices
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.wearos_todoapp.R
import com.example.wearos_todoapp.presentation.theme.WearostodoappTheme

class TodoAppActivity : ComponentActivity() {
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
            BasicTextField(
                value = taskText,
                onValueChange = {
                    taskText = it
                },
                label = { Text(stringResource(id = R.string.enter_task_hint)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (taskText.isNotBlank()) {
                            val newTask = Task(taskText = taskText)
                            coroutineScope.launch(Dispatchers.IO) {
                                taskDao.insert(newTask)
                                taskText = ""
                                // Refresh the task list to display the new task
                                tasks = taskDao.getAllTasks()
                            }
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
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
            }

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
        IconButton(
            onClick = { onDeleteTask(task) },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = stringResource(id = R.string.delete_button),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewTodoApp() {
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
