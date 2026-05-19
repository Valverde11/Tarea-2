# CE-1103 Extraclase 2: Benchmark y Visualización de Estructuras de Datos

Aplicación Java para comparar el desempeño (tiempos y comparaciones de clave) de seis estructuras de datos: BST, AVL, Splay, Red-Black, Array y Lista Enlazada Simple.

## Requisitos

- **JDK**: 17 o superior
- **Compilación**: `javac` (incluido en JDK)
- **Ejecución**: `java`

## Compilación y Ejecución

```bash

# Ejecutar
Dirigase a la clase Main y ejecute el codigo con run o F5
```

## Interfaz Gráfica

La aplicación usa **Swing** (UI nativa Java). Al iniciar se abre una ventana con:

### Panel Izquierdo (Configuración)

**Parámetros:**
- **N**: Cantidad de claves a insertar (default: 1000)
- **Semilla (seed)**: Genera inserción aleatoria reproducible (default: 42)
- **W (warmup)**: Corridas de calentamiento JVM (default: 2)
- **R (rondas)**: Corridas medidas que se promedian (default: 5)
- **Consultas**: Cantidad de búsquedas a realizar (default: 200)

**Modo de búsqueda:**
- **Automático**: Genera búsquedas usando la semilla (mezcla claves existentes y no existentes)
- **Manual**: Pega o escribe claves separadas por espacios, comas o saltos de línea

**Cargar archivo**: Botón para importar búsquedas desde archivo .txt o .csv

**Estructuras activas**: Casillas para elegir qué estructuras comparar

**Botones principales:**
- **Cargar valores por defecto**: Reinicia parámetros
- **Ejecutar Benchmark**: Corre la comparación
- **Exportar CSV**: Guarda resultados
- **Visualizar árboles**: Panel con dos árboles lado a lado
- **Secuencia paso a paso**: Ver construcción inserto a inserto

### Panel Central (Resultados)

**Tabla de resultados**: Métrica en filas, estructura en columnas
- Tiempo (inserción, búsqueda, borrado)
- Comparaciones de clave
- Complejidad O(·)
- Altura / Tamaño
- Pie con parámetros (N, W, R)

**Log**: Mensajes de ejecución y diagnostico

## Formato de Archivo para Búsquedas

El archivo debe contener **números enteros** separados por:
- **Comas** (`,`)
- **Espacios** (incluyendo tabulaciones)
- **Saltos de línea** (`\n`, `\r\n`)

### Ejemplos válidos

**Archivo CSV:**
```
10,20,30,50,100,200
```

**Archivo TXT con espacios:**
```
10 20 30 50 100 200
```

**Archivo TXT con saltos de línea:**
```
10
20
30
50
100
200
```

**Archivo TXT mixto:**
```
10, 20, 30
50 100
200
```

El parser ignora valores no numéricos y líneas en blanco.

## Funcionalidades

### Experimento

1. **Inserción aleatoria**: Todos usan la misma secuencia (controlada por seed)
2. **Búsquedas**: Modo manual o automático, sin reconstruir entre consultas
3. **Borrado**: Vacía todas las estructuras (excepto Red-Black, que no lo mide)
4. **Fases por corrida**: Inserción → Búsqueda → Borrado
5. **Protocolo**: W rondas de warmup (no contadas) + R rondas medidas (se promedian)

### Visualizador de Árboles

Panel que muestra dos árboles lado a lado tras completar solo las inserciones:
- Elegir dos tipos entre BST, AVL, Splay, Red-Black
- Nodos azules = nodos negros (BST/AVL/Splay), nodos rojos = RED (Red-Black)
- Botón de actualización para cambiar tipos

### Secuencia Paso a Paso

Visualiza la construcción de una estructura inserto a inserto:
- Máximo 40 pasos para legibilidad
- Muestra altura/tamaño en cada paso
- Navegación con botones Prev/Next
- Para estructuras lineales (Array, Lista): representación textual

### Exportar CSV

Guarda la última corrida completada en formato CSV:
- Coherente con la tabla en pantalla
- Incluye parámetros (N, W, R)
- Compatible con Excel o Visual Studio Code

## Estructuras Implementadas

| Estructura | Insert | Search | Delete | Comparaciones |
|-----------|--------|--------|--------|---------------|
| BST       | ✓      | ✓      | ✓      | Sí            |
| AVL       | ✓      | ✓      | ✓      | Sí            |
| Splay     | ✓      | ✓      | ✓      | Sí            |
| Red-Black | ✓      | ✓      | N/A    | Sí (solo ins/búsq) |
| Array     | ✓      | ✓      | ✓      | Sí            |
| Lista     | ✓      | ✓      | ✓      | Sí            |

**Red-Black**: Solo inserción y búsqueda son medidas (borrado N/A por especificación).

## Conteo de Comparaciones

Se cuenta cada comparación de clave realizada durante:
- **Inserción**: Búsqueda del punto de inserción + rebalanceos (si aplica)
- **Búsqueda**: Travesía hasta encontrar/no encontrar la clave
- **Borrado**: Búsqueda + desconexión + rebalanceos

**Red-Black**: Solo inserción y búsqueda se reportan; borrado es N/A.

## Notas Importantes

- **Reproducibilidad**: Misma seed siempre genera la misma secuencia de inserción
- **JVM Warmup**: W rondas iniciales permiten que la JVM optimice (JIT)
- **Promedios**: Resultados mostrados son promedios de R corridas
- **Límite de visualización**: Secuencia limitada a 40 insertos para no saturar visualmente