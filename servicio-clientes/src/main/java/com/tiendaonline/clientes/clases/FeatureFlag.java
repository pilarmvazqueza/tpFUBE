package com.tiendaonline.clientes.clases;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name= "feature_flags")
public class FeatureFlag {
@Id
private String nombre;
private boolean activo;

}
