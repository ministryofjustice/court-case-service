package uk.gov.justice.probation.courtcaseservice.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.DuplicateEntityException;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class CourtService {

    @Autowired
    private CourtRepository courtRepository;

    public CourtEntity updateCourt(CourtEntity courtEntity) {
        if (courtRepository.findByCourtCode(courtEntity.getCourtCode()).isPresent()) {
            throw new DuplicateEntityException(String.format("Court with courtCode '%s' already exists", courtEntity.getCourtCode()));
        }
        return courtRepository.save(courtEntity);
    }

    public List<CourtEntity> getCourts() {
        return courtRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(CourtEntity::getName))
            .collect(Collectors.toList());
    }
}
