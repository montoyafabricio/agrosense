package com.agrosense.gestion;

import jakarta.validation.constraints.NotBlank;

public record ProductorCreateDTO(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotBlank(message = "El contacto es obligatorio")
    String contacto
) {
    public ProductorCreateDTO {
        if (nombre != null) {
            nombre = nombre.trim();
        }
        if (contacto != null) {
            contacto = contacto.trim();
        }
    }
}
