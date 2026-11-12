package com.tiendaonline.clientes.servicios;

import com.tiendaonline.clientes.clases.FeatureFlag;
import com.tiendaonline.clientes.repositorio.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;

    public boolean isEnabled (String flag){
    return featureFlagRepository.findById(flag)
            .map(FeatureFlag::isActivo)
            .orElse(false);
}

}
