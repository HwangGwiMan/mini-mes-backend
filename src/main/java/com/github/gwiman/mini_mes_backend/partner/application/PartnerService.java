package com.github.gwiman.mini_mes_backend.partner.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.partner.api.dto.PartnerRequest;
import com.github.gwiman.mini_mes_backend.partner.api.dto.PartnerResponse;
import com.github.gwiman.mini_mes_backend.partner.domain.Partner;
import com.github.gwiman.mini_mes_backend.partner.domain.PartnerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerService {

	private final PartnerRepository partnerRepository;

	public PartnerResponse findById(Long id) {
		Partner entity = partnerRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Partner not found: " + id));
		return PartnerResponse.from(entity);
	}

	@Transactional
	public PartnerResponse create(PartnerRequest request) {
		Partner entity = new Partner(request.getCode(), request.getName());
		Partner saved = partnerRepository.save(entity);
		return PartnerResponse.from(saved);
	}
}
