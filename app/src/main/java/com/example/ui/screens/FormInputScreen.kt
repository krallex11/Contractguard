package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContractType
import com.example.data.model.FieldType
import com.example.data.model.FormField
import com.example.ui.theme.SleekDarkBackground
import com.example.ui.theme.SleekDarkCardBorder
import com.example.ui.theme.SleekDarkSurface
import com.example.ui.theme.SleekDarkSurfaceVariant
import com.example.ui.theme.SleekLimeGreenContainer
import com.example.ui.theme.SleekLimeGreenOnPrimary
import com.example.ui.theme.SleekLimeGreenPrimary
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormInputScreen(
    contractType: ContractType,
    fields: List<FormField>,
    isGenerating: Boolean,
    onFieldValueChange: (String, String) -> Unit,
    onGenerateClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    Scaffold(
        containerColor = SleekDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = contractType.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SleekTextWhite
                            )
                        )
                        Text(
                            text = "Fill Party & Agreement Details",
                            fontSize = 11.5.sp,
                            color = SleekTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.testTag("form_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SleekLimeGreenPrimary
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SleekLimeGreenContainer)
                            .border(1.dp, SleekLimeGreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SleekLimeGreenPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AES-256",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekLimeGreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SleekDarkBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = SleekDarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onGenerateClicked,
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekLimeGreenPrimary,
                            contentColor = SleekLimeGreenOnPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("generate_contract_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                color = SleekLimeGreenOnPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Compiling Draft...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate Legal Contract Draft",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SleekDarkSurface)
                        .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Provide information below to populate all required clauses for your US & EU regional standards.",
                        fontSize = 12.sp,
                        color = SleekTextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            items(fields) { field ->
                FormFieldItem(
                    field = field,
                    onValueChange = { newValue -> onFieldValueChange(field.id, newValue) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
fun FormFieldItem(
    field: FormField,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = field.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SleekTextWhite
            )
            if (field.isRequired) {
                Text(
                    text = " *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekLimeGreenPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (field.fieldType == FieldType.DROPDOWN && field.options.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = field.value,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text(field.placeholder, fontSize = 13.sp, color = SleekTextMuted) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select",
                            tint = SleekLimeGreenPrimary,
                            modifier = Modifier.clickable { expanded = !expanded }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .testTag("input_${field.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekLimeGreenPrimary,
                        unfocusedBorderColor = SleekDarkCardBorder,
                        focusedContainerColor = SleekDarkSurface,
                        unfocusedContainerColor = SleekDarkSurface,
                        focusedTextColor = SleekTextWhite,
                        unfocusedTextColor = SleekTextWhite
                    )
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(SleekDarkSurfaceVariant)
                        .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(8.dp))
                ) {
                    field.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = 12.5.sp, color = SleekTextWhite) },
                            onClick = {
                                onValueChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = field.value,
                onValueChange = onValueChange,
                placeholder = { Text(field.placeholder, fontSize = 13.sp, color = SleekTextMuted.copy(alpha = 0.6f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_${field.id}"),
                singleLine = field.fieldType != FieldType.MULTILINE,
                minLines = if (field.fieldType == FieldType.MULTILINE) 3 else 1,
                maxLines = if (field.fieldType == FieldType.MULTILINE) 5 else 1,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekLimeGreenPrimary,
                    unfocusedBorderColor = SleekDarkCardBorder,
                    focusedContainerColor = SleekDarkSurface,
                    unfocusedContainerColor = SleekDarkSurface,
                    focusedTextColor = SleekTextWhite,
                    unfocusedTextColor = SleekTextWhite
                )
            )
        }
    }
}

