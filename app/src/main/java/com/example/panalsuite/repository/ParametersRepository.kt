package com.example.panalsuite.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.panalsuite.model.Location
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.User

enum class ParamError { NotAllowed, Empty, Duplicate, NotFound, LastOne }
object ParametersRepository {
    private val categories = mutableStateListOf(
        "Hardware", "Software", "Network", "Access", "Security", "Other"
    )

    private val locations = mutableStateListOf(
        Location(1, "Estación Central"),
        Location(2, "Plaza Egaña"),
        Location(3, "Tobalaba"),
        Location(4, "Manquehue"),
        Location(5, "Las Torres"),
        Location(6, "Principe de Gales"),
        Location(7, "Los Dominicos"),
        Location(8, "Pedro de Valdivia"),
        Location(9, "Ñuñoa"),
        Location(10, "Los Heroes")
    )
    private var nextLocationId = 11L

    @Synchronized
    fun restore(savedCategories: List<String>, savedLocations: List<Location>) {
        if (savedCategories.isNotEmpty()) { categories.clear(); categories.addAll(savedCategories) }
        if (savedLocations.isNotEmpty()) { locations.clear(); locations.addAll(savedLocations) }
        nextLocationId = (locations.maxOfOrNull { it.idLocation } ?: 0L) + 1
    }

    fun getCategories(): List<String> = categories.toList()
    fun getLocations(): List<Location> = locations.toList()

    private fun canManage(actor: User) = Permissions.ManageParameters in actor.role.permits


    @Synchronized
    fun addCategory(actor: User, name: String): ParamError? {
        if (!canManage(actor)) return ParamError.NotAllowed
        val clean = name.trim()
        if (clean.isEmpty()) return ParamError.Empty
        if (categories.any { it.equals(clean, ignoreCase = true) }) return ParamError.Duplicate
        categories.add(clean)
        return null
    }

    @Synchronized
    fun renameCategory(actor: User, oldName: String, newName: String): ParamError? {
        if (!canManage(actor)) return ParamError.NotAllowed
        val index = categories.indexOf(oldName)
        if (index < 0) return ParamError.NotFound
        val clean = newName.trim()
        if (clean.isEmpty()) return ParamError.Empty
        if (categories.any { it != oldName && it.equals(clean, ignoreCase = true) }) return ParamError.Duplicate
        categories[index] = clean
        return null
    }

    @Synchronized
    fun deleteCategory(actor: User, name: String): ParamError? {
        if (!canManage(actor)) return ParamError.NotAllowed
        if (name !in categories) return ParamError.NotFound
        if (categories.size <= 1) return ParamError.LastOne
        categories.remove(name)
        return null
    }

    @Synchronized
    fun addLocation(actor: User, name: String): ParamError? {
        if (!canManage(actor)) return ParamError.NotAllowed
        val clean = name.trim()
        if (clean.isEmpty()) return ParamError.Empty
        if (locations.any { it.name.equals(clean, ignoreCase = true) }) return ParamError.Duplicate
        locations.add(Location(nextLocationId++, clean))
        return null
    }

    @Synchronized
    fun renameLocation(actor: User, idLocation: Long, newName: String): ParamError? {
        if (!canManage(actor)) return ParamError.NotAllowed
        val index = locations.indexOfFirst { it.idLocation == idLocation }
        if (index < 0) return ParamError.NotFound
        val clean = newName.trim()
        if (clean.isEmpty()) return ParamError.Empty
        if (locations.any { it.idLocation != idLocation && it.name.equals(clean, ignoreCase = true) }) {
            return ParamError.Duplicate
        }
        locations[index] = locations[index].copy(name = clean)
        return null
    }

    @Synchronized
    fun deleteLocation(actor: User, idLocation: Long): ParamError? {
        if (!canManage(actor)) return ParamError.NotAllowed
        if (locations.none { it.idLocation == idLocation }) return ParamError.NotFound
        if (locations.size <= 1) return ParamError.LastOne
        locations.removeAll { it.idLocation == idLocation }
        return null
    }
}
