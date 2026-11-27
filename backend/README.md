# PanalSuite – Backend 🐝🐝🐝🐝

Dos microservicios Spring Boot con MongoDB.

| Servicio | Puerto | Base de datos | Qué hace |
|---|---|---|---|
| `users-service` | 8081 | `panalsuite_users` | Login, usuarios y equipos |
| `tickets-service` | 8082 | `panalsuite_tickets` | Tickets, categorías, sucursales y estadísticas |

## Ejecutar
Requiere JDK 17+ y MongoDB corriendo en `localhost:27017`.
Desde la carpeta `backend/` (en Windows, `gradlew.bat`):

```bash
./gradlew :users-service:bootRun      # http://localhost:8081
./gradlew :tickets-service:bootRun    # http://localhost:8082
```

Al primer arranque se cargan datos de prueba (usuarios de la app con contraseña `1`, equipos, categorías, sucursales y 3 tickets).
Otra base de datos: variable de entorno `MONGODB_URI`.

## Desde la app Android
- Emulador: `http://10.0.2.2:8081/` y `http://10.0.2.2:8082/`.
- Dispositivo físico: la IP del PC en la red local.
- La app necesita el permiso `INTERNET` y `android:usesCleartextTraffic="true"` (HTTP sin TLS).

## Endpoints
| Servicio | Método | Ruta | Descripción |
|---|---|---|---|
| users | POST | `/api/auth/login` | Iniciar sesión (`identifier`, `password`) |
| users | GET, POST | `/api/users` | Listar, crear |
| users | GET, PUT, DELETE | `/api/users/{id}` | Leer, actualizar, eliminar |
| users | GET, POST | `/api/teams` | Listar, crear |
| users | GET, PUT, DELETE | `/api/teams/{id}` | Leer, actualizar, eliminar |
| tickets | GET, POST | `/api/tickets` | Buscar (filtros: `userId`, `assignedTo`, `status`, `priority`, `q`, `from`, `to`), crear |
| tickets | GET, PUT, DELETE | `/api/tickets/{id}` | Leer, editar, eliminar |
| tickets | PATCH | `/api/tickets/{id}/status` | Cambiar estado |
| tickets | PATCH | `/api/tickets/{id}/assign` | Asignar operador |
| tickets | PATCH | `/api/tickets/{id}/pin` | Fijar / desfijar |
| tickets | POST | `/api/tickets/{id}/comments` | Agregar comentario |
| tickets | GET | `/api/tickets/stats` | Totales por estado y prioridad |
| tickets | GET, POST | `/api/categories`, `/api/locations` | Listar, crear |
| tickets | PUT, DELETE | `/api/categories/{id}`, `/api/locations/{id}` | Renombrar, eliminar |

Errores: JSON `{status, error, message}` (400 validación, 401 login, 404 no existe, 409 conflicto).

## Prueba rápida
```bash
curl -X POST localhost:8081/api/auth/login -H "Content-Type: application/json" \
     -d '{"identifier":"admin@gmail.com","password":"1"}'
```