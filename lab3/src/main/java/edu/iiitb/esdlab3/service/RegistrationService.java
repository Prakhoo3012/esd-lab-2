package edu.iiitb.esdlab3.service;

import edu.iiitb.esdlab3.dto.CheckInRequest;
import edu.iiitb.esdlab3.dto.CreateRegistrationRequest;
import edu.iiitb.esdlab3.dto.TransferRegistrationRequest;
import edu.iiitb.esdlab3.entity.*;
import edu.iiitb.esdlab3.exception.*;
import edu.iiitb.esdlab3.repository.EventRepository;
import edu.iiitb.esdlab3.repository.RegistrationRepository;

import java.util.List;
import java.util.Locale;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

        private final EventRepository eventRepository;
        private final RegistrationRepository registrationRepository;
        private final EventService eventService;

        @Transactional
        public Registration register(
                        Long eventId,
                        CreateRegistrationRequest request) {

                Event event = eventService.get(eventId);

                long activeCount = registrationRepository
                                .countByEvent_IdAndStatus(
                                                eventId,
                                                RegistrationStatus.ACTIVE);

                if (activeCount >= event.getCapacity()) {
                        throw new EventFullException();
                }

                String email = request.getStudentEmail()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                // Intentionally missing duplicate-registration check.
                if (registrationRepository.existsByEvent_IdAndStudentEmailIgnoreCaseAndStatus(
                                eventId, email, RegistrationStatus.ACTIVE)) {
                        throw new DuplicateRegistrationException();
                }
                
                return registrationRepository.save(
                                new Registration(
                                                event,
                                                email,
                                                RegistrationStatus.ACTIVE));
        }

        public List<Registration> activeForEvent(Long eventId) {
                eventService.get(eventId);
                return registrationRepository.findActiveByEventId(eventId, RegistrationStatus.ACTIVE);
        }

        @Transactional
        public void cancel(Long registrationId) {
                Registration registration = registrationRepository
                                .findById(registrationId)
                                .orElseThrow(
                                                () -> new RegistrationNotFoundException(registrationId));

                if (registration.getStatus() == RegistrationStatus.ACTIVE) {
                        registration.setStatus(RegistrationStatus.CANCELLED);
                }
        }

        @Transactional
        public Registration transfer(
                        Long registrationId,
                        TransferRegistrationRequest request) {

                Registration current = registrationRepository
                                .findById(registrationId)
                                .orElseThrow(
                                                () -> new RegistrationNotFoundException(registrationId));

                if (current.getStatus() == RegistrationStatus.CANCELLED) {
                        throw new RegistrationConflictException();
                }

                String email = request.getStudentEmail().trim();
                if (!email.equalsIgnoreCase(current.getStudentEmail())) {
                        throw new UnauthorizedOperationException();
                }

                Event target = eventService.get(request.getTargetEventId());

                long activeCount = registrationRepository
                                .countByEvent_IdAndStatus(
                                                target.getId(),
                                                RegistrationStatus.ACTIVE);
                if (activeCount >= target.getCapacity()) {
                        throw new EventFullException();
                }

                // Runs before the old registration is cancelled, so transferring
                // to the same event is also rejected as a duplicate.
                if (registrationRepository.existsByEvent_IdAndStudentEmailIgnoreCaseAndStatus(
                                target.getId(), email, RegistrationStatus.ACTIVE)) {
                        throw new DuplicateRegistrationException();
                }

                current.setStatus(RegistrationStatus.CANCELLED);

                return registrationRepository.save(
                                new Registration(
                                                target,
                                                current.getStudentEmail(),
                                                RegistrationStatus.ACTIVE));
        }

        @Transactional
        public Registration checkIn(Long eventId, CheckInRequest request) {
                eventService.get(eventId);

                Registration registration = registrationRepository
                                .findByEvent_IdAndStudentEmailIgnoreCaseAndStatus(
                                                eventId,
                                                request.getStudentEmail().trim(),
                                                RegistrationStatus.ACTIVE)
                                .orElseThrow(() -> new RegistrationNotFoundException(
                                                "Registration not found for this student and event"));

                if (registration.isCheckedIn()) {
                        throw new AlreadyCheckedInException();
                }

                registration.markCheckedIn();
                return registrationRepository.save(registration);
        }
}
