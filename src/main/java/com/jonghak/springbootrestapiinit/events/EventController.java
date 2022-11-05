package com.jonghak.springbootrestapiinit.events;

import com.jonghak.springbootrestapiinit.accounts.Account;
import com.jonghak.springbootrestapiinit.accounts.CurrentUser;
import com.jonghak.springbootrestapiinit.common.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventRepository eventRepository;

    private final ModelMapper modelMapper;

    private final EventValidator eventValidator;

    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser) {
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class);
        event.update();
        event.setManager(currentUser); // 현재 유저정보 세팅
        Event newEvent = this.eventRepository.save(event);


        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();
        /*EntityModel eventResource = EntityModel.of(newEvent,
                linkTo(EventController.class).withRel("query-events"),
                selfLinkBuilder.withSelfRel(),
                selfLinkBuilder.withRel("update-event"));*/
        EventResource eventResource = new EventResource(newEvent,
                selfLinkBuilder.withRel("update-event"),
                linkTo(EventController.class).withRel("query-events"),
                Link.of("/docs/index.html#resources-events-create").withRel("profile"));
        return ResponseEntity.created(createdUri).body(eventResource);
    }

    private ResponseEntity<ErrorsResource> badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

    @GetMapping("search")
    public ResponseEntity searchEvent() {
        URI searchUri = linkTo(EventController.class).slash("{id}").toUri();
        return ResponseEntity.created(searchUri).build();
    }


    /**
     * 이벤트 조회
     * @param pageable
     * @param assembler
     * @param account 현재 사용자정보 (spring security의 User 객체)
     * @return
     */
    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable,
                                     PagedResourcesAssembler<Event> assembler,
                                     @CurrentUser Account account) {
        /**
         * authentication.userAuthentication.principal : spring security의 User객체 (username, password 등등)
         *               .authorities : user 권한 정보
         *
         * Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         * User principal = (User) authentication.getPrincipal(); // spring security의 Userr 객체로 사용 가능
         */

        Page<Event> page = this.eventRepository.findAll(pageable);
        PagedModel<EntityModel<Event>> pagedModels = assembler.toModel(page, e -> new EventResource(e));
        pagedModels.add(Link.of("/docs/index.html#resources-events-list").withRel("profile"));

        // spring seurity의 User 유무에 따라 create-event(이벤트생성) link 추가
        if(account != null){
            pagedModels.add(linkTo(EventController.class).withRel("create-event"));
        }
        return ResponseEntity.ok(pagedModels);

    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResource> getEvent(@PathVariable Integer id,
                                                  @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (!optionalEvent.isPresent()) {
            return ResponseEntity.notFound().header(HttpHeaders.LOCATION,linkTo(EventController.class).withRel("query-events").toUri().toString()).build();
        }

        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event,
                Link.of("/docs/index.html#resources-events-get").withRel("profile"));

        // 조회한 event의 생성자가 현재 사용자와 동일하면 update-event link 추가
        if(event.getManager().equals(currentUser)) {
            eventResource.add(linkTo(EventController.class).slash(event.getId()).withRel("update-event"));
        }
        return ResponseEntity.ok(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id, 
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if(!optionalEvent.isPresent()){
            return ResponseEntity.notFound().build();
        }

        if(errors.hasErrors()) {
            return badRequest(errors);
        }

        this.eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()){
            return badRequest(errors);
        }

        Event existingEvent = optionalEvent.get();

        // 수정할 event의 생성자가 현재 사용자가 아닐 경우 403(FORBIDDEN) : 액세스가 허용되지 않는 리소스 (권한X)
        // 참고 - 401(UNAUTHORIZED) : 인증되지 않았거나, 유효한 인증 정보가 부족한 경우
        if(!existingEvent.getManager().equals(currentUser)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        this.modelMapper.map(eventDto, existingEvent);
        Event savedEvent = this.eventRepository.save(existingEvent);

        EventResource eventResource = new EventResource(savedEvent,
                Link.of("/docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }
}
