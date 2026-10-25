# Diagramas

Todos los diagramas están escritos en **PlantUML** (`.puml`) y se encuentran renderizados en `png/`.
Para regenerarlos: `java -jar plantuml.jar -tpng -o png/<carpeta> <carpeta>/*.puml` (requiere Graphviz)
o abre los `.puml` con la extensión *PlantUML* de VS Code / IntelliJ, o en https://www.plantuml.com/plantuml.

| Vista / diagrama | Fuente | Imagen |
|---|---|---|
| Vista de contexto | [context/vista-contexto.puml](context/vista-contexto.puml) | [png](png/context/vista-contexto.png) |
| Vista funcional (componentes) | [component/vista-funcional-componentes.puml](component/vista-funcional-componentes.puml) | [png](png/component/vista-funcional-componentes.png) |
| Vista lógica (clases del dominio) | [class/vista-logica-clases.puml](class/vista-logica-clases.puml) | [png](png/class/vista-logica-clases.png) |
| Vista lógica (patrones) | [class/vista-logica-patrones.puml](class/vista-logica-patrones.puml) | [png](png/class/vista-logica-patrones.png) |
| Vista de desarrollo (paquetes) | [package/vista-desarrollo-paquetes.puml](package/vista-desarrollo-paquetes.puml) | [png](png/package/vista-desarrollo-paquetes.png) |
| Vista de despliegue | [deployment/despliegue.puml](deployment/despliegue.puml) | [png](png/deployment/despliegue.png) |
| Casos de uso | [use-case/casos-de-uso.puml](use-case/casos-de-uso.puml) | [png](png/use-case/casos-de-uso.png) |
| Secuencia: login | [sequence/01-login.puml](sequence/01-login.puml) | [png](png/sequence/01-login.png) |
| Secuencia: crear misión | [sequence/02-crear-mision.puml](sequence/02-crear-mision.puml) | [png](png/sequence/02-crear-mision.png) |
| Secuencia: asignar recursos | [sequence/03-asignar-recursos.puml](sequence/03-asignar-recursos.puml) | [png](png/sequence/03-asignar-recursos.png) |
| Secuencia: iniciar misión | [sequence/04-iniciar-mision.puml](sequence/04-iniciar-mision.puml) | [png](png/sequence/04-iniciar-mision.png) |
| Secuencia: finalizar misión | [sequence/05-finalizar-mision.puml](sequence/05-finalizar-mision.puml) | [png](png/sequence/05-finalizar-mision.png) |
| Secuencia: registrar mantenimiento | [sequence/06-registrar-mantenimiento.puml](sequence/06-registrar-mantenimiento.puml) | [png](png/sequence/06-registrar-mantenimiento.png) |
| Secuencia: registro y aprobación | [sequence/07-registro-aprobacion.puml](sequence/07-registro-aprobacion.puml) | [png](png/sequence/07-registro-aprobacion.png) |
| Actividad: crear misión | [activity/01-crear-mision.puml](activity/01-crear-mision.puml) | [png](png/activity/01-crear-mision.png) |
| Actividad: ejecutar misión | [activity/02-ejecutar-mision.puml](activity/02-ejecutar-mision.puml) | [png](png/activity/02-ejecutar-mision.png) |
| Actividad: gestionar mantenimiento | [activity/03-gestionar-mantenimiento.puml](activity/03-gestionar-mantenimiento.puml) | [png](png/activity/03-gestionar-mantenimiento.png) |
| Estados: misión | [state/estado-mision.puml](state/estado-mision.puml) | [png](png/state/estado-mision.png) |
| Estados: drone | [state/estado-drone.puml](state/estado-drone.puml) | [png](png/state/estado-drone.png) |
| Entidad-relación | [erd/erd.puml](erd/erd.puml) | [png](png/erd/erd.png) |
| Flujo general | [flow/flujo-general.puml](flow/flujo-general.puml) | [png](png/flow/flujo-general.png) |
| Arquitectura en capas | [architecture/arquitectura-capas.puml](architecture/arquitectura-capas.puml) | [png](png/architecture/arquitectura-capas.png) |
