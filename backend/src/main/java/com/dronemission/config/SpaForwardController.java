package com.dronemission.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Solo se activa cuando el frontend compilado está dentro del backend (despliegue en la nube).
 * Las rutas de React (/missions, /drones/3, ...) devuelven index.html para que React Router
 * muestre la página correcta al recargar el navegador.
 */
@Controller
@ConditionalOnResource(resources = "classpath:/static/index.html")
public class SpaForwardController {

    @GetMapping({
            "/{path:^(?!api|swagger-ui|v3|h2-console|demo|assets|error)[^.]*}",
            "/{path:^(?!api|swagger-ui|v3|h2-console|demo|assets|error)[^.]*}/{*rest}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
