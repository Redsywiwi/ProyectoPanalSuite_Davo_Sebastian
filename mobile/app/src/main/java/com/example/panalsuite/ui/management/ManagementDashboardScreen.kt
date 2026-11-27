package com.example.panalsuite.ui.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.panalsuite.model.OperatorLoad
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.User
import com.example.panalsuite.repository.TeamRepository
import com.example.panalsuite.repository.TicketRepository
import com.example.panalsuite.repository.UserRepository
import com.example.panalsuite.ui.home.EmptyStateText
import com.example.panalsuite.ui.home.HomeHeader

@Composable
fun ManagementDashboardScreen(
    user: User,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val operators = remember { UserRepository(context).getActiveOperators() }
    val stats = TicketRepository.getStats(user, Permissions.ViewDashboard)
    val teams = TeamRepository.getTeams()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeHeader(name = user.name, role = user.role.name, onMenuClick = onMenuClick)
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (stats == null) {
            EmptyStateText(text = stringResource(R.string.report_no_access))
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiTile(
                    label = stringResource(R.string.kpi_total),
                    value = stats.total.toString(),
                    modifier = Modifier.weight(1f)
                )
                KpiTile(
                    label = stringResource(R.string.kpi_open),
                    value = stats.open.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiTile(
                    label = stringResource(R.string.kpi_critical),
                    value = stats.openCritical.toString(),
                    modifier = Modifier.weight(1f)
                )
                KpiTile(
                    label = stringResource(R.string.kpi_unassigned),
                    value = stats.openUnassigned.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            ReportCard(title = stringResource(R.string.report_compliance_title)) {
                BarRow(
                    label = stringResource(R.string.kpi_closed),
                    valueText = percentOf(stats.closed, stats.total).asPercentText(),
                    fraction = percentOf(stats.closed, stats.total)
                )
                Text(
                    text = stringResource(R.string.report_compliance_detail, stats.closed, stats.total),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatLine(
                    label = stringResource(R.string.summary_oldest_age),
                    value = stats.oldestOpenAgeDays?.let { stringResource(R.string.report_age_days_long, it) }
                        ?: stringResource(R.string.report_none)
                )
            }

            ReportCard(title = stringResource(R.string.report_workload_title)) {
                val rows = operators.map { op ->
                    op.name + " " + op.lastName to (stats.load.firstOrNull { it.operatorId == op.idUser }
                        ?: OperatorLoad(op.idUser, 0, 0))
                }
                if (rows.isEmpty()) {
                    Text(
                        text = stringResource(R.string.teams_no_operators),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val maxOpen = rows.maxOf { it.second.open }.coerceAtLeast(1)
                    rows.forEach { (label, load) ->
                        BarRow(
                            label = label,
                            valueText = stringResource(R.string.report_workload_value, load.open, load.closed),
                            fraction = load.open.toFloat() / maxOpen
                        )
                    }
                }
            }


            ReportCard(title = stringResource(R.string.report_team_title)) {
                val loads = teamLoads(stats, teams)
                if (loads.isEmpty()) {
                    Text(
                        text = stringResource(R.string.teams_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val maxOpen = loads.maxOf { it.open }.coerceAtLeast(1)
                    loads.forEach { team ->
                        BarRow(
                            label = team.name,
                            valueText = stringResource(R.string.report_workload_value, team.open, team.closed),
                            fraction = team.open.toFloat() / maxOpen
                        )
                    }
                }
            }
        }
    }
}
