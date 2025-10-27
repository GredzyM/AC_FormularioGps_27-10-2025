# OSM Mini

Una aplicación Android mínima que combina OpenStreetMap con Room Database para guardar ubicaciones.

## Características

- **Mapa interactivo**: Tap en el mapa para colocar/mover un marcador
- **Formulario**: Campos para nombre, descripción y coordenadas (autorellenadas desde el mapa)
- **Base de datos**: Persistencia de ubicaciones usando Room
- **Arquitectura MVVM**: ViewModel para compartir datos entre fragments

## Stack Tecnológico

- **Lenguaje**: Java
- **SDK**: Min 24, Target/Compile 34
- **Dependencias principales**:
  - osmdroid-android:6.1.18
  - androidx.appcompat:1.7.0
  - androidx.room:2.6.1
  - androidx.lifecycle:2.7.0

## Compilación

### Prerrequisitos

- Android Studio (Agente IDE recomendado: 2024.1+)
- JDK 11+
- Gradle 8.x

### Pasos

1. Clone o descargue el repositorio
2. Abra el proyecto en Android Studio
3. Sincronice Gradle
4. Ejecute la app en un emulador o dispositivo físico (API 24+)

```bash
./gradlew assembleDebug
# O desde Android Studio: Build > Make Project
```

## Permisos

La aplicación utiliza los siguientes permisos:

- `INTERNET`: Para cargar tiles del mapa OpenStreetMap
- `ACCESS_NETWORK_STATE`: Para verificar conectividad
- `ACCESS_FINE_LOCATION`: Declarado pero no obligatorio (la app funciona sin GPS)

### Nota sobre permisos de ubicación

El permiso `ACCESS_FINE_LOCATION` está declarado en el manifest, pero **no se solicita en tiempo de ejecución**. La aplicación funciona completamente a través de taps en el mapa. Si se deniega el permiso de ubicación, la app no se ve afectada.

## User-Agent de osmdroid

**Importante**: OpenStreetMap requiere un User-Agent válido para cargar tiles. La aplicación configura esto automáticamente en `App.java`:

```java
Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
```

Sin este paso, los tiles del mapa no se cargarán. El User-Agent se establece usando el `applicationId` del proyecto (`com.jhonelvis.osmmini`).

## Configuración del Mapa

El mapa inicia centrado en **Santa Cruz de la Sierra, Bolivia** con las siguientes coordenadas:

- **Latitud**: -17.7833
- **Longitud**: -63.1821
- **Zoom**: 12
- **Tiles**: MAPNIK (estilo estándar de OSM)

### Cambiar el centro inicial

Para modificar el centro del mapa, edite `MapFragment.java` línea 58-60:

```java
// Centro en Santa Cruz de la Sierra
IMapController mapController = mapView.getController();
GeoPoint santaCruz = new GeoPoint(-17.7833, -63.1821);
mapController.setZoom(12.0);
mapController.setCenter(santaCruz);
```

Reemplace las coordenadas por las deseadas. Ejemplos:

- **La Paz**: `new GeoPoint(-16.4996, -68.1465)`
- **Buenos Aires**: `new GeoPoint(-34.6037, -58.3816)`
- **Sao Paulo**: `new GeoPoint(-23.5505, -46.6333)`

## Uso

1. **Abrir la app**: Se muestra el formulario arriba y el mapa abajo (50/50)
2. **Tocar el mapa**: Coloca un marcador en esa ubicación y actualiza las coordenadas en el formulario
3. **Completar formulario**: Ingrese un nombre (obligatorio) y descripción (opcional)
4. **Guardar**: Presione "Guardar" para insertar la ubicación en la base de datos
5. **Confirmación**: Aparece un Toast con "Guardado"

## Arquitectura

```
MainActivity
├── FormFragment (arriba)
│   ├── TextInputLayout: Nombre
│   ├── TextInputLayout: Descripción
│   ├── TextInputLayout: Latitud (readonly)
│   ├── TextInputLayout: Longitud (readonly)
│   └── MaterialButton: Guardar
│
├── MapFragment (abajo)
│   └── MapView (osmdroid)
│
└── PlaceViewModel (compartido)
    └── LiveData<LatLon>

Data Layer
├── Place (entity)
├── PlaceDao (@Insert)
└── AppDatabase (singleton)
```

## Flujo de Datos

1. **Tap en mapa** → `MapFragment` recibe evento → Crea/Mueve `Marker`
2. **Actualización de ViewModel** → `PlaceViewModel.setLatLon(lat, lon)`
3. **Observación en FormFragment** → `PlaceViewModel.getSelectedLatLon()` notifica cambios
4. **Relleno automático** → Lat/Lon se formatean con `String.format("%.6f", ...)`
5. **Guardar** → Validación → `PlaceDao.insert()` en thread background
6. **Toast** → `Toast.makeText()` en UI thread

## Base de Datos

- **Room**: SQLite con anotaciones
- **Entidad**: `Place` con campos: `id`, `name`, `description`, `lat`, `lon`
- **DAO**: `PlaceDao` con método `insert()`
- **Threading**: Inserción en background usando `Executors.newSingleThreadExecutor()`
- **Path**: `/data/data/com.jhonelvis.osmmini/databases/places_database`

Para inspeccionar la base de datos en Android Studio:

1. View > Tool Windows > App Inspection
2. Seleccione el dispositivo
3. Navegue a Databases > places_database > places

## Limitaciones

Esta es una versión mínima. No incluye:

- Listado de ubicaciones guardadas
- Edición/eliminación de lugares
- GPS en tiempo real
- Búsqueda de ubicaciones
- Servicios en background
- Notificaciones

## Solución de Problemas

### El mapa no carga tiles

1. Verificar conexión a Internet
2. Comprobar que `App.java` configure el User-Agent correctamente
3. Revisar permisos `INTERNET` y `ACCESS_NETWORK_STATE` en el manifest

### Error al compilar Room

- Asegúrese de que `room-compiler` esté en `annotationProcessor` y no en `implementation`
- Verifique que `kapt` no esté habilitado (esta app usa Java, no Kotlin)

### Markers no aparecen

- Verificar que `onResume()` y `onPause()` del `MapFragment` se llamen correctamente
- Revisar logs de osmdroid en Logcat

## Licencia

Este proyecto es un ejemplo educativo. OpenStreetMap data © OpenStreetMap contributors.

## Contacto

Package: `com.jhonelvis.osmmini`

