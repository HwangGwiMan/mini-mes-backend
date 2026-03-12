package com.github.gwiman.mini_mes_backend.partner.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.gwiman.mini_mes_backend.common.exception.BusinessRuleViolationException;
import com.github.gwiman.mini_mes_backend.common.exception.ResourceNotFoundException;
import com.github.gwiman.mini_mes_backend.common.util.QueryParamEscaper;
import com.github.gwiman.mini_mes_backend.partner.api.dto.PartnerRequest;
import com.github.gwiman.mini_mes_backend.partner.api.dto.PartnerResponse;
import com.github.gwiman.mini_mes_backend.partner.domain.Partner;
import com.github.gwiman.mini_mes_backend.partner.domain.PartnerRepository;
import com.github.gwiman.mini_mes_backend.partner.internal.PartnerQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerService {

	private final PartnerRepository partnerRepository;
	private final PartnerQueryRepository partnerQueryRepository;

	public List<PartnerResponse> findAll(String code, String name) {
		return partnerRepository.search(QueryParamEscaper.escapeLike(code), QueryParamEscaper.escapeLike(name)).stream()
			.map(PartnerResponse::from)
			.toList();
	}

	public PartnerResponse findById(Long id) {
		return partnerQueryRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("거래처를 찾을 수 없습니다: " + id));
	}

	@Transactional
	public PartnerResponse create(PartnerRequest request) {
		if (partnerRepository.existsByCode(request.getCode())) {
			throw new BusinessRuleViolationException("이미 사용 중인 코드입니다: " + request.getCode());
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
			.orElseThrow(() -> new ResourceNotFoundException("거래처를 찾을 수 없습니다: " + id));
		if (partnerRepository.existsByCodeAndIdNot(request.getCode(), id)) {
			throw new BusinessRuleViolationException("이미 사용 중인 코드입니다: " + request.getCode());
		}
		entity.update(
			request.getCode(), request.getName(),
			request.getBusinessNumber(), request.getCeoName(),
			request.getAddress(), request.getPhone1(), request.getPhone2(),
			request.getTradeTypeCode()
		);
		return PartnerResponse.from(entity);
	}

	public boolean exists(Long id) {
		return partnerRepository.existsById(id);
	}

	@Transactional
	public void delete(Long id) {
		if (!partnerRepository.existsById(id)) {
			throw new ResourceNotFoundException("거래처를 찾을 수 없습니다: " + id);
		}
		partnerRepository.deleteById(id);
	}

	@Transactional
	public void initDefaultPartners() {
		createPartnerIfAbsent("P001", "(주)한국전자",  "1234567890", "홍길동",   "서울시 강남구", "02-1234-5678", null, "TRADE_TYPE_02");
		createPartnerIfAbsent("P002", "(주)삼성산업",  "2345678901", "이순신",   "경기도 수원시", "031-234-5678", null, "TRADE_TYPE_02");
		createPartnerIfAbsent("P003", "(주)대한기계",  "3456789012", "강감찬",   "인천시 남동구", "032-345-6789", null, "TRADE_TYPE_03");
		createPartnerIfAbsent("P004", "(주)우리부품",  "4567890123", "장보고",   "경남 창원시",   "055-456-7890", null, "TRADE_TYPE_01");
		createPartnerIfAbsent("P005", "(주)미래소재",  "5678901234", "을지문덕", "충남 아산시",   "041-567-8901", null, "TRADE_TYPE_01");
	}

	private void createPartnerIfAbsent(String code, String name, String businessNumber,
		String ceoName, String address, String phone1, String phone2, String tradeTypeCode) {
		if (!partnerRepository.existsByCode(code)) {
			partnerRepository.save(new Partner(code, name, businessNumber, ceoName, address, phone1, phone2, tradeTypeCode));
		}
	}

}
