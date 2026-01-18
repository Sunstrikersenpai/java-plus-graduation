package ru.practicum.event;


import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.client.AnalyzerClient;
import ru.practicum.client.CollectorClient;
import ru.practicum.event.client.CategoryClient;
import ru.practicum.event.client.RequestClient;
import ru.practicum.event.client.UserClient;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.RequestAdminParams;
import ru.practicum.event.model.RequestPublicParams;
import ru.practicum.ewm.stats.proto.message.RecommendedEventProto;
import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.event.*;
import ru.practicum.interaction.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.RequestDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.interaction.enums.ActionType;
import ru.practicum.interaction.enums.RequestStatus;
import ru.practicum.interaction.enums.State;
import ru.practicum.interaction.enums.StateAction;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.exception.ValidationException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private static final int RECOMMENDATION_MAX_RESULTS = 10;
    private final EventRepository eventRepository;
    private final CategoryClient categoryClient;
    private final UserClient userClient;
    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;
    private final RequestClient requestClient;

    @Override
    public List<EventFullDto> getAllEvents(RequestAdminParams params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Specification<Event> spec = EventSpecification.byParams(params);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        List<EventFullDto> dtos = events.stream()
                .map(event -> EventMapper.toEventFullDto(
                        event, getCategoryDtoMap(events), getUserDtoMap(events)))
                .toList();

        setRatingAndConfirmedRequests(dtos);

        return dtos;
    }

    @Override
    public List<EventShortDto> getAllEvents(RequestPublicParams params, HttpServletRequest request) {
        if (params.getRangeStart() != null && params.getRangeEnd() != null
                && params.getRangeStart().isAfter(params.getRangeEnd())) {
            throw new ValidationException("rangeStart must not be after rangeEnd");
        }
        Pageable pageable;

        if (params.getEventSort() == EventSort.EVENT_DATE) {
            pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(),
                    Sort.by("eventDate").descending());
        } else {
            pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        }

        Specification<Event> spec = EventSpecification.byParams(params);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        List<EventShortDto> dtos = eventRepository.findAll(spec, pageable).getContent()
                .stream()
                .map(event -> EventMapper.toEventShortDto(
                        event, getCategoryDtoMap(events), getUserDtoMap(events)))
                .toList();

        setRatingAndConfirmedRequests(dtos);

        if (params.getEventSort() != EventSort.EVENT_DATE) {
            dtos = dtos.stream()
                    .sorted(Comparator.comparing(EventShortDto::getRating).reversed())
                    .toList();
        }

        return dtos;
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEventById(eventId);
        log.info(">>> PATCH updateEvent called for eventId={}, state={}", eventId, getEventById(eventId).getState());
        LocalDateTime publishedOn = event.getPublishedOn();
        if (request.getEventDate() != null && publishedOn != null &&
                !request.getEventDate().isAfter(publishedOn.plusHours(1))) {
            throw new ConflictException("Event date must be at least 1 hour after publication time");
        }

        applyUpdate(event, request);

        if (request.getStateAction() != null) {
            if (request.getStateAction().equals(StateAction.PUBLISH_EVENT) && !event.getState().equals(State.PENDING)) {
                throw new ConflictException("Event can be published only if it is in PENDING state");
            }
            if (request.getStateAction().equals(StateAction.REJECT_EVENT) && event.getState().equals(State.PUBLISHED)) {
                throw new ConflictException("Published event cannot be rejected");
            }

            switch (request.getStateAction()) {
                case PUBLISH_EVENT -> {
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> event.setState(State.CANCELED);
            }
        }

        Event updated = eventRepository.save(event);
        CategoryDto catDto = categoryClient.getById(updated.getCategory());
        UserShortDto userDto = userClient.getUserById(updated.getInitiatorId());
        log.info("After update: Event id={}, state={}", updated.getId(), updated.getState());
        return EventMapper.toEventFullDto(updated, catDto, userDto);
    }

    @Override
    public List<EventShortDto> getAllEvents(Long userId, Integer from, Integer size) {
        UserShortDto user = userClient.getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("createdOn").descending());

        Page<Event> page = eventRepository.findAllByInitiatorIdOrderByCreatedOnDesc(user.getId(), pageable);

        List<Event> events = page.getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<EventShortDto> dtos = events.stream()
                .map(event -> EventMapper.toEventShortDto(
                        event, getCategoryDtoMap(events), getUserDtoMap(events)))
                .toList();

        setRatingAndConfirmedRequests(dtos);

        return dtos;
    }


    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate() == null ||
                !newEventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Event date must be at least 2 hours from now");
        }
        Event event = EventMapper.toEvent(newEventDto);
        CategoryDto catDto = categoryClient.getById(newEventDto.getCategory());
        UserShortDto userDto = userClient.getUserById(userId);

        event.setCategory(catDto.getId());
        event.setInitiatorId(userDto.getId());
        event.setState(State.PENDING);
        event = eventRepository.save(event);

        return EventMapper.toEventFullDto(event, catDto, userDto);
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId) {
        Event event = getEventById(eventId);
        UserShortDto userDto = userClient.getUserById(userId);
        CategoryDto catDto = categoryClient.getById(event.getCategory());
        if (!event.getInitiatorId().equals(userDto.getId())) {
            throw new ValidationException("User with id " + userId + " is not the initiator of event with id " + eventId);
        }

        EventFullDto dto = EventMapper.toEventFullDto(event, catDto, userDto);

        setRatingAndConfirmedRequests(List.of(dto));

        return dto;
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = getEventById(eventId);
        if (!event.getInitiatorId().equals(userId)) {
            throw new ValidationException("User with id " + userId + " is not the initiator of event with id " + eventId);
        }
        if (!(event.getState() == State.CANCELED || event.getState() == State.PENDING)) {
            throw new ConflictException("Only canceled or pending events can be updated");
        }
        if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours in the future");
        }

        applyUpdate(event, request);

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
            }
        }

        Event updated = eventRepository.save(event);
        UserShortDto userDto = userClient.getUserById(updated.getInitiatorId());
        CategoryDto catDto = categoryClient.getById(updated.getCategory());
        return EventMapper.toEventFullDto(updated, catDto, userDto);
    }

    @Override
    public EventFullDto getEventById(Long eventId, Long userId, HttpServletRequest request) {
        Event event = eventRepository.findEventByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));


        CategoryDto catDto = categoryClient.getById(event.getCategory());
        UserShortDto userDto = userClient.getUserById(userId);
        EventFullDto dto = EventMapper.toEventFullDto(event, catDto, userDto);

        setRatingAndConfirmedRequests(List.of(dto));

        collectorClient.collectAction(userId, eventId, ActionType.ACTION_VIEW.toString(), Instant.now());

        return dto;
    }

    @Override
    public List<RequestDto> getEventParticipants(Long userId, Long eventId) {
        Event event = getEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new ValidationException(
                    "User with id " + userId + " is not the initiator of event with id " + eventId
            );
        }

        return requestClient.getRequestsByEventId(eventId);
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(
            Long userId, Long eventId, EventRequestStatusUpdateRequest req
    ) {
        Event event = getEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new ValidationException("User with id " + userId + " is not the initiator of event with id " + eventId);
        }

        RequestStatus targetStatus = req.getStatus();

        if ((targetStatus != RequestStatus.CONFIRMED && targetStatus != RequestStatus.REJECTED)) {
            throw new ValidationException("Status must be CONFIRMED or REJECTED");
        }

        List<RequestDto> requests = requestClient.getAllRequestsByIds(req.getRequestIds());

        for (RequestDto r : requests) {
            if (!r.getEvent().equals(eventId)) {
                throw new ValidationException("Request " + r.getId() + " does not belong to event " + eventId);
            }
            if (!r.getStatus().equals(RequestStatus.PENDING.toString())) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        int limit = event.getParticipantLimit();
        long alreadyConfirmed = requestClient.countRequests(eventId, RequestStatus.CONFIRMED);

        List<RequestDto> confirmedRequests = new ArrayList<>();
        List<RequestDto> rejectedRequests = new ArrayList<>();

        if (targetStatus == RequestStatus.REJECTED) {
            for (RequestDto r : requests) r.setStatus(RequestStatus.REJECTED.toString());
            rejectedRequests.addAll(requestClient.saveAll(requests));
        } else {
            if (limit == 0) {
                for (RequestDto r : requests) {
                    r.setStatus(RequestStatus.CONFIRMED.toString());
                    confirmedRequests.add(r);
                }
                requestClient.saveAll(confirmedRequests);
            } else {
                long remaining = limit - alreadyConfirmed;
                if (remaining <= 0) {
                    throw new ConflictException("The participant limit has been reached");
                }

                for (RequestDto r : requests) {
                    if (remaining > 0) {
                        r.setStatus(RequestStatus.CONFIRMED.toString());
                        confirmedRequests.add(r);
                        remaining--;
                    } else {
                        r.setStatus(RequestStatus.REJECTED.toString());
                        rejectedRequests.add(r);
                    }
                }
                requestClient.saveAll(confirmedRequests);
                requestClient.saveAll(rejectedRequests);
            }
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);
        return result;
    }

    @Override
    public Set<EventShortDto> findAllByIdIn(@RequestParam List<Long> eventIds) {
        List<Event> events = eventRepository.findAllById(eventIds);
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(
                        event, getCategoryDtoMap(events), getUserDtoMap(events)))
                .collect(Collectors.toSet());
    }

    @Override
    public EventFullDto privateGetById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        CategoryDto categoryDto = categoryClient.getById(event.getCategory());
        UserShortDto userShortDto = userClient.getUserById(event.getInitiatorId());
        return EventMapper.toEventFullDto(event, categoryDto, userShortDto);

    }

    @Override
    public List<EventShortDto> getEventsByCategory(Long categoryId) {
        List<Event> events = eventRepository.findEventsByCategory(categoryId);

        return events.stream()
                .map(e -> EventMapper.toEventShortDto(
                        e, getCategoryDtoMap(events), getUserDtoMap(events))).toList();
    }

    @Override
    public List<EventShortDto> getRecommendations(Long userId) {
        List<RecommendedEventProto> recs = analyzerClient
                .getRecommendationsForUser(userId, RECOMMENDATION_MAX_RESULTS)
                .toList();

        if (recs.isEmpty()) return List.of();

        List<Long> eventIds = recs.stream()
                .map(RecommendedEventProto::getEventId)
                .toList();

        Map<Long, Double> scoreByEventId = recs.stream()
                .collect(Collectors.toMap(
                        RecommendedEventProto::getEventId,
                        RecommendedEventProto::getScore
                ));

        List<Event> events = eventRepository.findAllById(eventIds);
        Map<Long, Event> eventById = events.stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        Map<Long, CategoryDto> categoryMap = getCategoryDtoMap(events);
        Map<Long, UserShortDto> userMap = getUserDtoMap(events);

        Map<Long, Long> confirmedRequestsCount = requestClient.getRequestsWithStatus(
                        eventIds, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(RequestDto::getEvent, Collectors.counting()));

        List<EventShortDto> result = new ArrayList<>(eventIds.size());
        for (Long id : eventIds) {
            Event e = eventById.get(id);
            EventShortDto dto = EventMapper.toEventShortDto(
                    e, categoryMap, userMap
            );

            dto.setRating(scoreByEventId.get(id));
            dto.setConfirmedRequests(confirmedRequestsCount.getOrDefault(id, 0L));

            result.add(dto);
        }

        return result;
    }

    @Override
    public void like(Long userId, Long eventId) {
        eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));

        boolean participated = requestClient.getUserRequests(userId).stream()
                .anyMatch(r ->
                        Objects.equals(r.getEvent(), eventId)
                                && RequestStatus.CONFIRMED.toString().equals(r.getStatus())
                );

        if (!participated) {
            throw new ValidationException("User can like only events participated in");
        }

        collectorClient.collectAction(userId, eventId, ActionType.ACTION_LIKE.toString(), Instant.now());
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
    }

    private <T extends BaseDto> void setRatingAndConfirmedRequests(List<T> dto) {
        if (dto.isEmpty()) return;

        List<Long> eventIds = dto.stream().map(BaseDto::getId).toList();


        Map<Long, Long> confirmedRequestsCount = requestClient.getRequestsWithStatus(
                        eventIds, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(RequestDto::getEvent, Collectors.counting()));

        Map<Long, Double> scores = analyzerClient.getInteractionsCount(eventIds)
                .collect(Collectors.toMap(
                        RecommendedEventProto::getEventId,
                        RecommendedEventProto::getScore
                ));

        for (BaseDto d : dto) {
            d.setRating(scores.getOrDefault(d.getId(), 0.0));
            d.setConfirmedRequests(confirmedRequestsCount.getOrDefault(d.getId(), 0L));
        }
    }

    private void applyUpdate(Event event, UpdateEventRequest req) {
        if (req.getAnnotation() != null) event.setAnnotation(req.getAnnotation());
        if (req.getCategory() != null) event.setCategory(categoryClient.getById(req.getCategory()).getId());
        if (req.getDescription() != null) event.setDescription(req.getDescription());
        if (req.getEventDate() != null) event.setEventDate(req.getEventDate());
        if (req.getLocation() != null) event.setLocation(req.getLocation());
        if (req.getPaid() != null) event.setPaid(req.getPaid());
        if (req.getParticipantLimit() != null) event.setParticipantLimit(req.getParticipantLimit());
        if (req.getRequestModeration() != null) event.setRequestModeration(req.getRequestModeration());
        if (req.getTitle() != null) event.setTitle(req.getTitle());
    }

    private Map<Long, CategoryDto> getCategoryDtoMap(List<Event> eventList) {
        List<Long> catIds = eventList.stream().map(Event::getCategory).toList();
        return categoryClient.findByIdIn(catIds).stream()
                .collect(Collectors.toMap(CategoryDto::getId, Function.identity()));
    }

    private Map<Long, UserShortDto> getUserDtoMap(List<Event> eventList) {
        List<Long> initiatorIds = eventList.stream().map(Event::getInitiatorId).toList();
        return userClient.findByIdIn(initiatorIds).stream()
                .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));
    }
}