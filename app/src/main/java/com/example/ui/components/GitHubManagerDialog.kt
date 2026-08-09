package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.GitHubFileContent
import com.example.data.GitHubRepoItem
import com.example.data.GitHubTreeItem
import com.example.ui.theme.SasaCardBackground
import com.example.ui.theme.SasaDarkSurface
import com.example.ui.theme.SasaPrimary
import com.example.ui.theme.SasaSecondary
import com.example.ui.theme.SasaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubManagerDialog(
    token: String,
    userStatus: String?,
    repos: List<GitHubRepoItem>,
    selectedRepo: GitHubRepoItem?,
    repoTree: List<GitHubTreeItem>,
    selectedFile: GitHubFileContent?,
    isLoading: Boolean,
    onTokenSave: (String) -> Unit,
    onSelectRepo: (GitHubRepoItem?) -> Unit,
    onOpenFile: (owner: String, repo: String, path: String, branch: String) -> Unit,
    onCommitFile: (owner: String, repo: String, path: String, content: String, message: String, sha: String?, branch: String) -> Unit,
    onForkRepo: (owner: String, repo: String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputToken by remember(token) { mutableStateOf(token) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Token & Repos, 1: Tree & File Editor
    var manualRepoInput by remember { mutableStateOf("") }
    var editedContent by remember(selectedFile) { mutableStateOf(selectedFile?.content ?: "") }
    var commitMessage by remember { mutableStateOf("تحديث عبر Sasa AI Platform") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .testTag("github_manager_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = SasaDarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = "GitHub",
                            tint = SasaPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "إدارة مستودعات GitHub",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SasaTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SasaCardBackground,
                    contentColor = SasaPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("🔑 التوكن والمستودعات") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("📁 المستندات والتعديل المباشر") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = SasaPrimary,
                        trackColor = SasaCardBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (selectedTab) {
                    0 -> {
                        // TAB 0: Token & Repositories
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Token Input Block
                            OutlinedTextField(
                                value = inputToken,
                                onValueChange = { inputToken = it },
                                label = { Text("GitHub Personal Access Token (PAT)") },
                                placeholder = { Text("ghp_xxxxxxxxxxxxxxxxxxxx") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = SasaSecondary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("github_token_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SasaPrimary,
                                    unfocusedBorderColor = SasaCardBackground,
                                    focusedLabelColor = SasaPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                userStatus?.let { status ->
                                    Text(
                                        text = status,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4ADE80),
                                        fontWeight = FontWeight.Medium
                                    )
                                } ?: Text(
                                    text = "لم يتم ربط الحساب بعد",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SasaTextSecondary
                                )

                                Button(
                                    onClick = { onTokenSave(inputToken) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SasaPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("save_github_token_button")
                                ) {
                                    Text("توثيق وربط", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = SasaCardBackground)

                            // Manual Owner/Repo Input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = manualRepoInput,
                                    onValueChange = { manualRepoInput = it },
                                    placeholder = { Text("owner/repository (مثال: username/sasa)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (manualRepoInput.contains("/")) {
                                            val parts = manualRepoInput.split("/")
                                            val customRepo = GitHubRepoItem(
                                                name = parts[1],
                                                fullName = manualRepoInput,
                                                isPrivate = false,
                                                defaultBranch = "main",
                                                htmlUrl = "https://github.com/$manualRepoInput",
                                                description = "مستودع مدخل يدوياً"
                                            )
                                            onSelectRepo(customRepo)
                                            selectedTab = 1
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SasaSecondary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("فتح", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "مستودعاتك على GitHub (${repos.size}):",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(repos) { repo ->
                                    val isSelected = selectedRepo?.fullName == repo.fullName
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelectRepo(repo)
                                                selectedTab = 1
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) SasaPrimary.copy(alpha = 0.2f) else SasaCardBackground
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, SasaPrimary) else null
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = repo.fullName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                if (!repo.description.isNullOrBlank()) {
                                                    Text(
                                                        text = repo.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = SasaTextSecondary,
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            // Fork Button
                                            IconButton(
                                                onClick = {
                                                    val parts = repo.fullName.split("/")
                                                    if (parts.size == 2) {
                                                        onForkRepo(parts[0], parts[1])
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CallSplit,
                                                    contentDescription = "نسخ/Fork",
                                                    tint = SasaSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: File Tree & Direct Code Editor
                        Column(modifier = Modifier.fillMaxSize()) {
                            selectedRepo?.let { repo ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "المستودع المحدد: ${repo.fullName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = SasaPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "الفرع: ${repo.defaultBranch}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SasaTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxSize()) {
                                    // File Tree Pane (Left/Right depending on RTL)
                                    Card(
                                        modifier = Modifier
                                            .weight(0.4f)
                                            .fillMaxHeight(),
                                        colors = CardDefaults.cardColors(containerColor = SasaCardBackground),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "شجرة الملفات (${repoTree.size}):",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LazyColumn(
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                items(repoTree.filter { it.type == "blob" }) { file ->
                                                    val isFileSelected = selectedFile?.path == file.path
                                                    Surface(
                                                        onClick = {
                                                            val parts = repo.fullName.split("/")
                                                            if (parts.size == 2) {
                                                                onOpenFile(parts[0], parts[1], file.path, repo.defaultBranch)
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isFileSelected) SasaPrimary.copy(alpha = 0.3f) else Color.Transparent
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.InsertDriveFile,
                                                                contentDescription = null,
                                                                tint = if (isFileSelected) SasaPrimary else SasaTextSecondary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = file.path,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = Color.White,
                                                                maxLines = 1,
                                                                fontSize = 11.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Direct Code Editor Pane
                                    Card(
                                        modifier = Modifier
                                            .weight(0.6f)
                                            .fillMaxHeight(),
                                        colors = CardDefaults.cardColors(containerColor = SasaCardBackground),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            selectedFile?.let { file ->
                                                Text(
                                                    text = "تعديل الملف: ${file.path}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = SasaSecondary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))

                                                OutlinedTextField(
                                                    value = editedContent,
                                                    onValueChange = { editedContent = it },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedContainerColor = Color(0xFF0D1117),
                                                        unfocusedContainerColor = Color(0xFF0D1117),
                                                        focusedBorderColor = SasaPrimary,
                                                        unfocusedBorderColor = Color.Transparent
                                                    ),
                                                    textStyle = LocalTextStyle.current.copy(
                                                        fontSize = 12.sp,
                                                        color = Color(0xFFE6EDE3)
                                                    )
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                OutlinedTextField(
                                                    value = commitMessage,
                                                    onValueChange = { commitMessage = it },
                                                    label = { Text("رسالة الالتزام (Commit Message)") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Button(
                                                    onClick = {
                                                        val parts = repo.fullName.split("/")
                                                        if (parts.size == 2) {
                                                            onCommitFile(
                                                                parts[0],
                                                                parts[1],
                                                                file.path,
                                                                editedContent,
                                                                commitMessage,
                                                                file.sha,
                                                                repo.defaultBranch
                                                            )
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SasaPrimary),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.Black)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("حفظ وCommit إلى GitHub", color = Color.Black, fontWeight = FontWeight.Bold)
                                                }
                                            } ?: Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "اختر ملفاً من شجرة المستودع لعرضه وتعديله مباشرة",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SasaTextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            } ?: Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "يرجى اختيار مستودع أولاً من تبويب 'التوكن والمستودعات'",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SasaTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
