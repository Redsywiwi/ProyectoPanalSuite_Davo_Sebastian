package com.example.panalsuite.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.TicketFilters
import com.example.panalsuite.model.TicketStatus
import com.example.panalsuite.model.Urgency
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// Para que los filtros sobrevivan a rotaciones de pantalla
val TicketFiltersSaver = listSaver<TicketFilters, String>(
    save = { f ->
        listOf(
            f.query,
            f.statuses.joinToString(",") { it.name },
            f.priorities.joinToString(",") { it.name },
            f.fromDate?.toString().orEmpty(),
            f.toDate?.toString().orEmpty()
        )
    },
    restore = { v ->
        TicketFilters(
            query = v[0],
            statuses = v[1].split(",").filter { it.isNotEmpty() }.map { TicketStatus.valueOf(it) }.toSet(),
            priorities = v[2].split(",").filter { it.isNotEmpty() }.map { Urgency.valueOf(it) }.toSet(),
            fromDate = v[3].takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) },
            toDate = v[4].takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) }
        )
    }
)

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun TicketSearchBar(
    filters: TicketFilters,
    onFiltersChange: (TicketFilters) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilters by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = filters.query,
            onValueChange = { onFiltersChange(filters.copy(query = it)) },
            modifier = Modifier.weight(1f),
            placeholder = { Text(text = stringResource(R.string.home_search_hint)) },
            leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (filters.query.isNotEmpty()) {
                    IconButton(onClick = { onFiltersChange(filters.copy(query = "")) }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.search_clear)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { showFilters = true }) {
                BadgedBox(
                    badge = { if (filters.hasAdvancedFilters) Badge() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = stringResource(R.string.filter_open),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    if (showFilters) {
        TicketFilterSheet(
            filters = filters,
            onDismiss = { showFilters = false },
            onApply = {
                onFiltersChange(it)
                showFilters = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TicketFilterSheet(
    filters: TicketFilters,
    onDismiss: () -> Unit,
    onApply: (TicketFilters) -> Unit
) {
    var draft by remember { mutableStateOf(filters) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.filter_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            FilterSectionTitle(stringResource(R.string.filter_status))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TicketStatus.entries.forEach { status ->
                    FilterChip(
                        selected = status in draft.statuses,
                        onClick = {
                            draft = draft.copy(
                                statuses = if (status in draft.statuses) draft.statuses - status
                                else draft.statuses + status
                            )
                        },
                        label = { Text(text = status.StatusName) }
                    )
                }
            }

            FilterSectionTitle(stringResource(R.string.filter_priority))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Urgency.entries.forEach { priority ->
                    FilterChip(
                        selected = priority in draft.priorities,
                        onClick = {
                            draft = draft.copy(
                                priorities = if (priority in draft.priorities) draft.priorities - priority
                                else draft.priorities + priority
                            )
                        },
                        label = { Text(text = priority.priorityName) }
                    )
                }
            }

            FilterSectionTitle(stringResource(R.string.filter_date))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateField(
                    label = stringResource(R.string.filter_from),
                    date = draft.fromDate,
                    onDateChange = { draft = draft.copy(fromDate = it) },
                    modifier = Modifier.weight(1f)
                )
                DateField(
                    label = stringResource(R.string.filter_to),
                    date = draft.toDate,
                    onDateChange = { draft = draft.copy(toDate = it) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        draft = TicketFilters(query = draft.query)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.filter_clear))
                }
                Button(
                    onClick = { onApply(draft) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.filter_apply))
                }
            }
        }
    }
}

@Composable
private fun FilterSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { open = true }, modifier = modifier) {
        Text(
            text = "$label: ${date?.format(dateFormatter) ?: stringResource(R.string.filter_any_date)}",
            maxLines = 1
        )
    }

    if (open) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateChange(
                            state.selectedDateMillis?.let {
                                Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                            }
                        )
                        open = false
                    }
                ) { Text(text = stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) {
                    Text(text = stringResource(R.string.dialog_cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}