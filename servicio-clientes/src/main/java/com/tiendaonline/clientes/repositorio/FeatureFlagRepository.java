package com.tiendaonline.clientes.repositorio;

import com.tiendaonline.clientes.clases.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, String> {

}
