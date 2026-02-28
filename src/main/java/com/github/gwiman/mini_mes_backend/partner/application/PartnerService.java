package com.github.gwiman.mini_mes_backend.partner.application;

import java.util.List;

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

	public List<PartnerResponse> findAll(String code, String name) {
		return partnerRepository.search(escapeLike(code), escapeLike(name)).stream()
			.map(PartnerResponse::from)
			.toList();
	}

	public PartnerResponse findById(Long id) {
		Partner entity = partnerRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("거래처를 찾을 수 없습니다: " + id));
		return PartnerResponse.from(entity);
	}

	@Transactional
	public PartnerResponse create(PartnerRequest request) {
		if (partnerRepository.existsByCode(request.getCode())) {
			throw new IllegalArgumentException("이미 사용 중인 코드입니다: " + request.getCode());
		}
		Partner entity = new Partner(
			request.getCode(), request.getName(),
			request.getBusinessNumber(), request.getCeoName(),
			request.getAddress(), request.getPhone1(), request.getPhone2(),
			request.getTradeTypeCode()
		);
		return PartnerResponse.from(partnerRepository.save(entity));
	}

	@Transactional
	public PartnerResponse update(Long id, PartnerRequest request) {
		Partner entity = partnerRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("거래처를 찾을 수 없습니다: " + id));
		if (partnerRepository.existsByCodeAndIdNot(request.getCode(), id)) {
			throw new IllegalArgumentException("이미 사용 중인 코드입니다: " + request.getCode());
		}
		entity.update(
			request.getCode(), request.getName(),
			request.getBusinessNumber(), request.getCeoName(),
			request.getAddress(), request.getPhone1(), request.getPhone2(),
			request.getTradeTypeCode()
		);
		return PartnerResponse.from(entity);
	}

	@Transactional
	public void delete(Long id) {
		if (!partnerRepository.existsById(id)) {
			throw new IllegalArgumentException("거래처를 찾을 수 없습니다: " + id);
		}
		partnerRepository.deleteById(id);
	}

	private String escapeLike(String value) {
		if (value == null || value.isBlank()) return null;
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}
}
