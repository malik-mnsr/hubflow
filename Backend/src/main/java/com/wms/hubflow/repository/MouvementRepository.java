package com.wms.hubflow.repository;

import com.wms.hubflow.entity.Mouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MouvementRepository extends JpaRepository<Mouvement, Long> {

    List<Mouvement> findByColisIdOrderByHorodatageDesc(Long colisId);

    List<Mouvement> findByOperateurId(Long operateurId);
}