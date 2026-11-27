package com.example.panalsuite.ui.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.panalsuite.R
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.User
import com.example.panalsuite.repository.TeamRepository
import com.example.panalsuite.repository.TicketRepository
import com.example.panalsuite.repository.UserRepository
import com.example.panalsuite.ui.home.EmptyStateText

@Composable
fun ManagementSummaryScreen(user: User) {
    val context = LocalContext.current
    val operators = remember { UserRepository(context).getActiveOperators() }
    val stats = TicketRepository.getStats(user, Permissions.ViewSummary)
    val teams = TeamRepository.getTeams()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.summary_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (stats == null) {
            EmptyStateText(text = stringResource(R.string.report_no_access))
            return@Column
        }

        val total = stats.total

        ReportCard(title = stringResource(R.string.summary_general)) {
            StatLine(stringResource(R.string.summary_total), stats.total.toString())
            StatLine(stringResource(R.string.kpi_open), stats.open.toString())
            StatLine(stringResource(R.string.kpi_closed), stats.closed.toString())
            StatLine(stringResource(R.string.summary_assigned), stats.assigned.toString())
            StatLine(stringResource(R.string.kpi_unassigned), stats.openUnassigned.toString())
            StatLine(stringResource(R.string.kpi_critical), stats.openCritical.toString())
            StatLine(stringResource(R.string.kpi_last7), stats.createdLast7Days.toString())
            StatLine(
                stringResource(R.string.summary_compliance),
                percentOf(stats.closed, total).asPercentText()
            )
            StatLine(
                stringResource(R.string.summary_assignment_rate),
                percentOf(stats.assigned, total).asPercentText()
            )
            StatLine(
                stringResource(R.string.summary_oldest_age),
                stats.oldestOpenAgeDays?.let { stringResource(R.string.report_age_days_long, it) }
                    ?: stringResource(R.string.report_none)
            )
        }

        ReportCard(title = stringResource(R.string.summary_by_status)) {
            stats.byStatus.forEach { (status, count) ->
                BarRow(
                    label = status.StatusName,
                    valueText = stringResource(
                        R.string.summary_share, count, percentOf(count, total).asPercentText()
                    ),
                    fraction = percentOf(count, total)
                )
            }
        }

        ReportCard(title = stringResource(R.string.summary_by_priority)) {
            stats.byPriority.forEach { (priority, count) ->
                BarRow(
                    label = priority.priorityName,
                    valueText = stringResource(
                        R.string.summary_share, count, percentOf(count, total).asPercentText()
                    ),
                    fraction = percentOf(count, total)
                )
            }
        }

        ReportCard(title = stringResource(R.string.summary_operators)) {
            if (operators.isEmpty()) {
                Text(
                    text = stringResource(R.string.teams_no_operators),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                operators.forEach { op ->
                    val load = stats.load.firstOrNull { it.operatorId == op.idUser }
                    val open = load?.open ?: 0
                    val closed = load?.closed ?: 0
                    BarRow(
                        label = "${op.name} ${op.lastName}",
                        valueText = stringResource(
                            R.string.summary_operator_detail,
                            open,
                            closed,
                            percentOf(closed, open + closed).asPercentText()
                        ),
                        fraction = percentOf(open, total)
                    )
                }
            }
        }

        ReportCard(title = stringResource(R.string.summary_teams)) {
            val loads = teamLoads(stats, teams)
            if (loads.isEmpty()) {
                Text(
                    text = stringResource(R.string.teams_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                loads.forEach { team ->
                    BarRow(
                        label = "${team.name} (${team.members})",
                        valueText = stringResource(
                            R.string.summary_operator_detail,
                            team.open,
                            team.closed,
                            percentOf(team.closed, team.open + team.closed).asPercentText()
                        ),
                        fraction = percentOf(team.open, total)
                    )
                }
            }
        }
    }
}
