# Luz y Sombra

Un ritual cooperativo de luz y oscuridad. Videojuego de plataformas 2D **cooperativo local** para **dos jugadores en el mismo teclado**, desarrollado en **Java 21 + JavaFX** con Maven.

La Luz no puede cruzar la penumbra. La Sombra no puede atravesar la luminosidad. Solo trabajando juntos podrán liberar las puertas selladas del Templo y restaurar el equilibrio entre el día y la noche.

---

## Índice

- [Requisitos](#requisitos)
- [Cómo ejecutar](#cómo-ejecutar)
- [Controles](#controles)
- [Objetivo y mecánicas](#objetivo-y-mecánicas)
- [Niveles](#niveles)
- [Progreso y guardado](#progreso-y-guardado)
- [Cómo colocar o reemplazar las imágenes](#cómo-colocar-o-reemplazar-las-imágenes)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Cómo crear niveles JSON](#cómo-crear-niveles-json)
- [Generar los recursos de audio e imágenes](#generar-los-recursos-de-audio-e-imágenes)
- [Empaquetar un JAR ejecutable](#empaquetar-un-jar-ejecutable)
- [Solución de problemas](#solución-de-problemas)

---

## Requisitos

| Requisito | Versión |
|-----------|---------|
| JDK | 21 o superior (Temurin, Corretto, Oracle…) |
| Maven | 3.9 o superior |
| Sistema | Windows, Linux o macOS (con pantalla y audio) |

Verificá tu instalación:

```bash
java -version    # debe decir java 21 (o mayor)
mvn -version     # debe decir Apache Maven 3.9.x y java version: 21
```

---

## Cómo ejecutar

Desde la raíz del proyecto:

```bash
mvn clean javafx:run
```

La primera compilación descarga las dependencias (JavaFX, Gson) y puede tardar unos minutos.

> **Nota sobre el modo ventana:** el juego se ejecuta **en una sola ventana**. Toda la navegación (menú, tutorial, selector de niveles, pausa, victoria, derrota) ocurre dentro de esa misma ventana. La resolución lógica es **1600×900** y se adapta con *letterboxing* a cualquier tamaño de pantalla (la escala se puede ajustar en `GameConfig.java`).

---

## Controles

El juego usa **un solo teclado** para los dos jugadores:

| Acción | Jugador 1 — **Luz** | Jugador 2 — **Sombra** |
|--------|---------------------|------------------------|
| Moverse | `A` / `D` | `J` / `L` |
| Saltar | `W` | `I` |
| Bajar (escaleras/plataformas) | `S` | `K` |
| Pausar / reanudar | `ESC` | `ESC` |

En los menús:

- `Flechas` o `W/S` (y `I/K`) — navegar
- `Enter` o `Espacio` — confirmar
- `ESC` — volver

---

## Objetivo y mecánicas

En cada nivel, **Luz** y **Sombra** deben llegar juntos a las **puertas de salida**:

- La **Puerta de Luz** se abre al recolectar **los 4 Albores** (solo Luz puede tocarlos).
- La **Puerta de Sombra** se abre al recolectar **las 4 Obsidianas** (solo Sombra puede tocarlas).

Ambas puertas deben estar abiertas para completar el nivel.

| Elemento | Efecto |
|----------|--------|
| ☀️ Albor | Coleccionable exclusivo de **Luz** |
| 💠 Obsidiana | Coleccionable exclusivo de **Sombra** |
| 🌫️ Penumbra | Zona de niebla violeta que daña a **Luz** (la Sombra puede cruzarla) |
| ☀️ Luminosidad | Zona de resplandor dorado que daña a **Sombra** (la Luz puede cruzarla) |
| ⚔️ Púas | Dañan a ambos jugadores |
| 🚪 Puertas | Bloqueadas hasta completar la colección del nivel |
| 🏮 Checkpoint | Punto de reaparición al perder una vida |

Otras mecánicas:

- **Vidas compartidas**: ambos jugadores comparten un total de vidas (configurable en `GameConfig`). Al agotarse, el nivel se reinicia desde el checkpoint.
- **Invulnerabilidad temporal**: tras recibir daño hay un breve período de invulnerabilidad.
- **Plataformas móviles**: transportan al jugador; algunas requieren un **interruptor** (ver nivel 3).
- **Escaleras**: se suben con movimiento vertical; se bajan con la tecla de bajar.
- **Temporizador**: el HUD muestra el tiempo del nivel; el mejor tiempo de cada nivel queda guardado.

---

## Niveles

| Nivel | Tema | Mecánicas nuevas |
|-------|------|------------------|
| 1 — El Atrio del Alba | Tutorial | Movimiento, saltos, coleccionables, puertas |
| 2 — Puentes de Penumbra | Puentes móviles | Plataformas móviles y escaleras |
| 3 — El Santuario Sellado | Interruptores | Plataformas activadas por interruptores (cooperación) |
| 4 — La Torre de la Medianoche | Vertical | Gran verticalidad, escaleras largas |
| 5 — El Umbral Eterno | Desafío final | Combinación de todas las mecánicas |

Los niveles se definen en archivos **JSON** (ver [Cómo crear niveles JSON](#cómo-crear-niveles-json)).

---

## Progreso y guardado

El progreso se guarda automáticamente en un archivo JSON local (la ruta se define en `ProgressManager.java`):

- Niveles desbloqueados (el nivel 1 siempre disponible).
- Niveles completados.
- Mejor tiempo por nivel.
- Total de coleccionables encontrados (información opcional).

---

## Cómo colocar o reemplazar las imágenes

El juego funciona **inmediatamente sin ninguna imagen**: si un recurso no existe, se dibuja un *placeholder* profesional generado en tiempo de ejecución (y se registra en consola). Esto te permite jugar al instante y reemplazar el arte cuando quieras.

### Dónde van las imágenes

Las imágenes se cargan desde el **classpath**, dentro de `src/main/resources`:

```
src/main/resources/assets/images/
```

La estructura esperada (los nombres y rutas exactos están definidos en `config/ResourcePaths.java`):

```
assets/images/
├── backgrounds/
│   ├── background-menu.png           # Fondo del menú (1600×900)
│   ├── background-level-1.png        # Fondo del nivel 1 (1600×900, tileable)
│   ├── background-level-2.png        # Fondo del nivel 2 (1600×900, tileable)
│   ├── background-level-3.png        # Fondo del nivel 3 (1600×900, tileable)
│   ├── background-level-4.png        # Fondo del nivel 4 (1600×900, tileable)
│   └── background-level-5.png        # Fondo del nivel 5 (1600×900, tileable)
├── ui/
│   ├── logo-luz-sombra.png           # Logo del menú principal
│   ├── lock.png                      # Candado del selector de niveles
│   ├── pause-icon.png                # Icono de pausa
│   ├── arrow-left.png                # Flecha del tutorial
│   └── arrow-right.png               # Flecha del tutorial
├── characters/
│   ├── light/                        # Luz (80×96)
│   │   ├── character-light-idle.png
│   │   ├── character-light-run-1.png
│   │   ├── character-light-run-2.png
│   │   └── character-light-jump.png
│   └── shadow/                       # Sombra (80×96)
│       ├── character-shadow-idle.png
│       ├── character-shadow-run-1.png
│       ├── character-shadow-run-2.png
│       └── character-shadow-jump.png
├── collectibles/
│   ├── collectible-albor.png         # Coleccionable de Luz (48×48)
│   └── collectible-obsidian.png      # Coleccionable de Sombra (48×48)
├── hazards/
│   ├── hazard-light.png              # Luminosidad: daña a Sombra (320×128)
│   ├── hazard-shadow.png             # Penumbra: daña a Luz (320×128)
│   └── spikes.png                    # Púas: dañan a ambos (128×64)
├── platforms/
│   ├── platform.png                  # Plataforma estática (192×48)
│   ├── moving-platform.png           # Plataforma móvil (192×48)
│   └── ladder.png                    # Escalera (48×160)
├── doors/
│   ├── door-light-closed.png         # Puerta de Luz cerrada (96×192)
│   ├── door-light-open.png           # Puerta de Luz abierta
│   ├── door-shadow-closed.png        # Puerta de Sombra cerrada
│   └── door-shadow-open.png          # Puerta de Sombra abierta
└── levels/
    ├── level-thumbnail-1.png …       # Miniaturas del selector (320×180)
    └── level-thumbnail-5.png
```

### Cómo reemplazar una imagen

1. Abrí `config/ResourcePaths.java` y fijate la ruta exacta que usa el juego (por ejemplo `"/assets/images/collectibles/collectible-albor.png"`).
2. Colocá tu archivo en `src/main/resources` en esa misma ruta, **con el mismo nombre y extensión** (el juego espera PNG).
3. Recompilá el proyecto: `mvn clean compile` (los archivos de `src/main/resources` se copian a `target/classes` en cada compilación; **`mvn clean` evita que queden copias viejas**).
4. Volvé a ejecutar: `mvn clean javafx:run`.

Cada nivel tiene su propio fondo: el nivel **N** carga `background-level-N.png` (definido por `ResourcePaths.backgroundLevel(N)`). Si reemplazás uno de esos archivos, ese nivel usará tu imagen. El `AssetGenerator` **conserva** los fondos de nivel que ya existen (no los sobrescribe), así que tu arte personalizado no se pierde al regenerar los recursos.

> **Consejo**: si querés crear tus propios sprites, respetá las proporciones de los *placeholders* para que las colisiones y la estética se mantengan. Las imágenes se escalan a la altura lógica de cada entidad (definida en `GameConfig`).

---

## Estructura del proyecto

```
Luz y Sombra/
├── pom.xml                                  # Build Maven (Java 21 + JavaFX)
├── README.md
└── src/
    └── main/
        ├── java/com/luzysombra/
        │   ├── Main.java                    # Entrada (no extiende Application)
        │   ├── GameApplication.java         # Stage único + escala 16:9
        │   ├── config/                      # GameConfig, ResourcePaths
        │   ├── entities/                    # Jugadores, plataformas, puertas,…
        │   ├── game/                        # Level, LevelLoader, GameLoop,…
        │   ├── input/                       # InputManager (teclado compartido)
        │   ├── navigation/                  # ScreenType, ScreenManager
        │   ├── persistence/                 # ProgressManager (guardado JSON)
        │   ├── resources/                   # ResourceManager (caché + placeholders)
        │   ├── screens/                     # Menú, tutorial, selector, juego
        │   ├── tools/                       # Generadores de audio e imágenes
        │   ├── ui/                          # HUD, overlays, botones
        │   └── util/                        # Colisiones, animaciones
        └── resources/
            ├── assets/images/…              # Imágenes (ver sección anterior)
            ├── assets/sounds/…              # Efectos de sonido WAV
            ├── assets/music/…               # Música WAV
            ├── assets/fonts/…               # Fuentes (opcional)
            ├── levels/level-1.json …        # Definición de niveles
            └── styles/game.css              # Estilos de toda la UI
```

---

## Cómo crear niveles JSON

Cada nivel es un archivo JSON en `src/main/resources/levels/`. El cargador (`LevelLoader.java`) traduce este JSON a los objetos del juego.

### Formato

```json
{
  "name": "El Atrio del Alba",
  "background": "/assets/images/backgrounds/level-1.png",
  "player_spawn": { "x": 80, "y": 680 },
  "light_spawn": { "x": 80, "y": 680 },
  "shadow_spawn": { "x": 140, "y": 680 },
  "goal_light": { "x": 3400, "y": 640, "width": 96, "height": 192 },
  "goal_shadow": { "x": 3600, "y": 640, "width": 96, "height": 192 },
  "collectibles": [
    { "type": "albor", "x": 400, "y": 560 },
    { "type": "obsidian", "x": 700, "y": 560 }
  ],
  "platforms": [
    { "x": 0, "y": 760, "width": 512, "height": 48 },
    { "x": 600, "y": 680, "width": 128, "height": 48 }
  ],
  "moving_platforms": [
    {
      "x": 900, "y": 640, "width": 128, "height": 24,
      "start": { "x": 900, "y": 640 },
      "end": { "x": 1300, "y": 640 },
      "speed": 90,
      "switch": "interruptor-1"
    }
  ],
  "ladders": [
    { "x": 1050, "y": 500, "width": 48, "height": 260 }
  ],
  "hazards": [
    { "type": "spikes", "x": 1500, "y": 730, "width": 128, "height": 32 },
    { "type": "penumbra", "x": 1800, "y": 640, "width": 320, "height": 120 },
    { "type": "luminosity", "x": 2200, "y": 640, "width": 320, "height": 120 }
  ],
  "checkpoints": [
    { "x": 2000, "y": 700 }
  ],
  "switches": [
    {
      "id": "interruptor-1",
      "x": 1950, "y": 640,
      "operator": "any",
      "type": "light"
    }
  ]
}
```

### Campos

| Sección | Descripción |
|---------|-------------|
| `player_spawn` | Punto de aparición de ambos jugadores (usar `light_spawn`/`shadow_spawn` para posiciones distintas). |
| `goal_light` / `goal_shadow` | Puertas de salida (rectángulos). |
| `collectibles` | `type`: `albor` (solo Luz) u `obsidian` (solo Sombra). |
| `platforms` | Rectángulos estáticos. |
| `moving_platforms` | Rectángulos que oscilan entre `start` y `end` a `speed` píxeles/segundo. Si se indica `switch`, la plataforma **solo se mueve mientras el interruptor con ese `id` está activado**. |
| `ladders` | Escaleras: rectángulo vertical. |
| `hazards` | `type`: `spikes` (dañan a ambos), `penumbra` (daña a Luz), `luminosity` (daña a Sombra). |
| `checkpoints` | Puntos de reaparición. |
| `switches` | `operator`: `light`, `shadow` o `any`; `type`: `toggle` o `hold`. |

**Consejos de diseño:**

- El suelo base suele estar en `y = 760` (altura de nivel ~800).
- Un salto alcanza ~150 px de alto y ~300 px de distancia horizontal: separá plataformas respetando esos límites.
- Coordenadas en píxeles del mundo; el nivel puede ser más ancho que 1600 px (la cámara lo sigue).

---

## Generar los recursos de audio e imágenes

Los recursos **PNG y WAV** se generan por código (sin librerías externas, solo AWT/JDK):

```bash
# Efectos de sonido y música
mvn compile exec:java -Dexec.mainClass=com.luzysombra.tools.AudioGenerator

# Imágenes (sprites, fondos, miniaturas…)
mvn compile exec:java -Dexec.mainClass=com.luzysombra.tools.AssetGenerator
```

Ejecutalos una vez; luego los archivos quedan en `src/main/resources/assets/`. Si borrás algún archivo, el juego cae al *placeholder* automáticamente (o regeneralo con el generador).

---

## Empaquetar un JAR ejecutable

```bash
mvn clean package
```

Esto genera:

- `target/luz-y-sombra-1.0.0.jar` — el juego.
- `target/lib/` — las dependencias (JavaFX, Gson) copiadas al lado.

Para ejecutar el JAR:

```bash
java -jar target/luz-y-sombra-1.0.0.jar
```

> **Importante**: el JAR espera las dependencias en `target/lib/` (misma carpeta). Si movés el JAR, mové también `lib/`.

---

## Solución de problemas

| Problema | Solución |
|----------|----------|
| `javafx:run` no encuentra JavaFX | Verificá que `java -version` sea 21 y que Maven use ese JDK (`mvn -version`). |
| `NoClassDefFoundError: javafx/...` al usar el JAR | Ejecutá el JAR desde la carpeta `target` (`java -jar luz-y-sombra-1.0.0.jar`), o mové `lib/` junto al JAR. |
| El juego muestra cuadros grises o figuras planas | Son los *placeholders*: faltan imágenes. Generá las imágenes con `AssetGenerator` o colocalas según [Cómo colocar o reemplazar las imágenes](#cómo-colocar-o-reemplazar-las-imágenes). |
| No se escucha audio | Verificá que se ejecutó `AudioGenerator` y que los WAV están en `src/main/resources/assets/`. |
| Al modificar imágenes no se ven los cambios | Ejecutá `mvn clean` antes de compilar (los resources quedan cacheados en `target/classes`). |
| Se ven las ventanas del sistema o diálogos | No debería ocurrir: el juego usa una sola ventana. Si aparece un diálogo nativo, es un bug — reportalo. |

---

## Créditos

- Desarrollo, arte procedural y audio procedural: **Luz y Sombra** (proyecto educativo — UNET).
- Construido con **Java 21**, **JavaFX 21** y **Maven**.
