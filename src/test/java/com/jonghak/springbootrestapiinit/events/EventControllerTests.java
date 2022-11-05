package com.jonghak.springbootrestapiinit.events;

import com.jonghak.springbootrestapiinit.accounts.*;
import com.jonghak.springbootrestapiinit.common.BaseTest;
import com.jonghak.springbootrestapiinit.configs.AppPropertices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTests extends BaseTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppPropertices appPropertices;

    @BeforeEach
    public void setUp() {
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Event 정상적으로 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("Rest API")
                .beginEnrollmentDateTime(LocalDateTime.of(2022, 8, 28, 0, 0, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 8, 29, 0, 0, 0))
                .beginEventDateTime(LocalDateTime.of(2022, 8, 30, 0, 0, 0))
                .endEventDateTime(LocalDateTime.of(2022, 8, 31, 0, 0, 0))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("군포시")
                .build();

        System.out.println(objectMapper.writeValueAsString(event));

        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(appPropertices.getUserUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(is(EventStatus.DRAFT.name())))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to qeury events"),
                                linkWithRel("update-event").description("link to update an existing"),
                                linkWithRel("profile").description("link to profile an existing")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of close of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("linit of new event")

                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                        ),
                        responseFields(
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of close of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("linit of new event"),
                                fieldWithPath("free").description("it tells if this event is fre e event or not"),
                                fieldWithPath("offline").description("it tells if this event is offline event or not"),
                                fieldWithPath("eventStatus").description("event status"),
                                fieldWithPath("manager.id").description("id of manager"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-events.href").description("link to query-events"),
                                fieldWithPath("_links.update-event.href").description("link to update-event"),
                                fieldWithPath("_links.profile.href").description("link to profile")

                        )
                ))
        ;
    }

    /**
     * 기본 사용자 계정 추가 (admin, user)
     */
    private void saveAccout() {
        Account user = null;
        try {
            AccountAdapter accountAdapter = (AccountAdapter) this.accountService.loadUserByUsername(appPropertices.getUserUsername());
            user = accountAdapter.getAccount();
        }catch (Exception e){}
        if(user == null) {
            user = Account.builder()
                    .email(appPropertices.getUserUsername())
                    .password(appPropertices.getUserPassword())
                    .roles(Set.of(AccountRole.USER))
                    .build();
            this.accountService.saveAccount(user);
        }

        Account admin = null;
        try {
            AccountAdapter accountAdapter = (AccountAdapter) this.accountService.loadUserByUsername(appPropertices.getAdminUsername());
            admin = accountAdapter.getAccount();
        }catch (Exception e){}
        if(admin == null) {
            admin = Account.builder()
                    .email(appPropertices.getAdminUsername())
                    .password(appPropertices.getAdminPassword())
                    .roles(Set.of(AccountRole.ADMIN))
                    .build();
            this.accountService.saveAccount(admin);
        }
    }

    private String getBearerToken(String createName) throws Exception {
        return "Bearer " + getAccessToken(createName);

    }
    private String getAccessToken(String createName) throws Exception {
        //Given
        Random random = new Random();
        int randomInt = random.nextInt(99999999);

        String username = appPropertices.getUserUsername();
        String password = appPropertices.getUserPassword();
        if(createName.equals(appPropertices.getAdminUsername())) {
            username = appPropertices.getAdminUsername();
            password = appPropertices.getAdminPassword();
        }

        saveAccout();

        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appPropertices.getClientId(), appPropertices.getClientSecret()))
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password"));
        String responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }

    @Test
    @DisplayName("Event 입력 받을 수 없는 값을 사용하는 경우에 에러가 발생하는 테스트")
    public void createEvent_bad_request() throws Exception {
        Event event = Event.builder()
                .name("Spring")
                .description("Rest API")
                .beginEnrollmentDateTime(LocalDateTime.of(2022, 8, 28, 0, 0, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 8, 29, 0, 0, 0))
                .beginEventDateTime(LocalDateTime.of(2022, 8, 30, 0, 0, 0))
                .endEventDateTime(LocalDateTime.of(2022, 8, 31, 0, 0, 0))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("군포시")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .id(100)
                .build();

        System.out.println(objectMapper.writeValueAsString(event));

        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(appPropertices.getUserUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("Event 입력 비어있는 경우에 에러가 발생하는 테스트")
    public void createEvent_bad_request_empty_input() throws Exception {
        EventDto eventDto = EventDto.builder().build();
        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(appPropertices.getUserUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("Event 입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createEvent_bad_request_wrong_input() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("Rest API")
                .beginEnrollmentDateTime(LocalDateTime.of(2022, 8, 28, 0, 0, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 8, 29, 0, 0, 0))
                .beginEventDateTime(LocalDateTime.of(2022, 8, 25, 0, 0, 0))
                .endEventDateTime(LocalDateTime.of(2022, 8, 24, 0, 0, 0))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("군포시")
                .build();

        System.out.println(objectMapper.writeValueAsString(event));

        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(appPropertices.getUserUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].objectName").exists())
                .andExpect(jsonPath("errors[0].filed").exists())
                .andExpect(jsonPath("errors[0].defaultMessage").exists())
                .andExpect(jsonPath("errors[0].code").exists())
                .andExpect(jsonPath("errors[0].rejectedValue").exists())
                .andExpect(jsonPath("_links.index").exists())
        ;
    }

    @ParameterizedTest
    @MethodSource("paramsForTestFree")
    @DisplayName("Event 입력값별 Free 테스트")
    public void testFree(int basePrice, int maxPrice, boolean isFree) {
        // given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        // when
        event.update();

        // then
        assertThat("basePrice=" + basePrice + ", maxPrice=" + maxPrice + ", isFree=" + isFree, isFree == event.isFree());
    }

    @ParameterizedTest
    @MethodSource("paramsForTestOffline")
    @DisplayName("Event 입력값별 offline 테스트")
    void testOffline(String location, boolean isOffline) {
        // given
        Event event = Event.builder()
                .location(location)
                .build();

        // when
        event.update();

        // then
        assertThat("location=" + location + ", isOffline=" + isOffline, isOffline == event.isOffline());
    }

    private static Stream<Arguments> paramsForTestFree() { // argument source method
        return Stream.of(
                Arguments.of(0, 0, true),
                Arguments.of(100, 0, false),
                Arguments.of(0, 100, false),
                Arguments.of(100, 200, false)
        );
    }

    private static Stream<Arguments> paramsForTestOffline() { // argument source method
        return Stream.of(
                Arguments.of("강남", true),
                Arguments.of(null, false),
                Arguments.of("        ", false)
        );
    }

    @Test
    @DisplayName("Event 30개의 이벤트를 10개씩 두번째 페이지 조회하기 - 인증정보X")
    public void queryEvents() throws Exception {

        saveAccout();

        // Given
        IntStream.range(0, 30).forEach(this::generateEvent);

        // When
        this.mockMvc.perform(get("/api/events")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "name,DESC")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("profile").description("link to profile an existing"),
                                linkWithRel("first").description("link to first page"),
                                linkWithRel("prev").description("link to prev page"),
                                linkWithRel("next").description("link to next page"),
                                linkWithRel("last").description("link to last page")

                        ),
                        /*requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),*/
                        requestParameters(
                                parameterWithName("page").description("page of Pageable"),
                                parameterWithName("size").description("size of Pageable"),
                                parameterWithName("sort").description("sort of Pageable")

                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.eventList[0].id").description("identifier of new event"),
                                fieldWithPath("_embedded.eventList[0].name").description("Name of new event"),
                                fieldWithPath("_embedded.eventList[0].description").description("description of new event"),
                                fieldWithPath("_embedded.eventList[0].beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("_embedded.eventList[0].closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("_embedded.eventList[0].beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("_embedded.eventList[0].endEventDateTime").description("date time of close of new event"),
                                fieldWithPath("_embedded.eventList[0].location").description("location of new event"),
                                fieldWithPath("_embedded.eventList[0].basePrice").description("base price of new event"),
                                fieldWithPath("_embedded.eventList[0].maxPrice").description("max price of new event"),
                                fieldWithPath("_embedded.eventList[0].limitOfEnrollment").description("linit of new event"),
                                fieldWithPath("_embedded.eventList[0].free").description("it tells if this event is fre e event or not"),
                                fieldWithPath("_embedded.eventList[0].offline").description("it tells if this event is offline event or not"),
                                fieldWithPath("_embedded.eventList[0].eventStatus").description("event status"),
                                fieldWithPath("_embedded.eventList[0].manager.id").description("id of manager"),
                                fieldWithPath("_embedded.eventList[0]._links.self.href").description("self link of eventList"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.first.href").description("link to first page"),
                                fieldWithPath("_links.prev.href").description("link to prev page"),
                                fieldWithPath("_links.next.href").description("link to next page"),
                                fieldWithPath("_links.last.href").description("link to last page"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("page.size").description("page to size"),
                                fieldWithPath("page.totalElements").description("page to totalElements"),
                                fieldWithPath("page.totalPages").description("page to totalPages"),
                                fieldWithPath("page.number").description("page to number")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("Event 30개의 이벤트를 10개씩 두번째 페이지 조회하기 - 인증정보")
    public void queryEventsWithAuthentication() throws Exception {

        saveAccout();

        // Given
        IntStream.range(0, 30).forEach(this::generateEvent);

        // When
        this.mockMvc.perform(get("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(appPropertices.getUserUsername()))
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "name,DESC")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-event").exists())
                .andDo(document("query-events",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("profile").description("link to profile an existing"),
                                linkWithRel("create-event").description("link to create-event"),
                                linkWithRel("first").description("link to first page"),
                                linkWithRel("prev").description("link to prev page"),
                                linkWithRel("next").description("link to next page"),
                                linkWithRel("last").description("link to last page")

                        ),
                        /*requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),*/
                        requestParameters(
                                parameterWithName("page").description("page of Pageable"),
                                parameterWithName("size").description("size of Pageable"),
                                parameterWithName("sort").description("sort of Pageable")

                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.eventList[0].id").description("identifier of new event"),
                                fieldWithPath("_embedded.eventList[0].name").description("Name of new event"),
                                fieldWithPath("_embedded.eventList[0].description").description("description of new event"),
                                fieldWithPath("_embedded.eventList[0].beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("_embedded.eventList[0].closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("_embedded.eventList[0].beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("_embedded.eventList[0].endEventDateTime").description("date time of close of new event"),
                                fieldWithPath("_embedded.eventList[0].location").description("location of new event"),
                                fieldWithPath("_embedded.eventList[0].basePrice").description("base price of new event"),
                                fieldWithPath("_embedded.eventList[0].maxPrice").description("max price of new event"),
                                fieldWithPath("_embedded.eventList[0].limitOfEnrollment").description("linit of new event"),
                                fieldWithPath("_embedded.eventList[0].free").description("it tells if this event is fre e event or not"),
                                fieldWithPath("_embedded.eventList[0].offline").description("it tells if this event is offline event or not"),
                                fieldWithPath("_embedded.eventList[0].eventStatus").description("event status"),
                                fieldWithPath("_embedded.eventList[0].manager.id").description("id of manager"),
                                fieldWithPath("_embedded.eventList[0]._links.self.href").description("self link of eventList"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.first.href").description("link to first page"),
                                fieldWithPath("_links.prev.href").description("link to prev page"),
                                fieldWithPath("_links.next.href").description("link to next page"),
                                fieldWithPath("_links.last.href").description("link to last page"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.create-event.href").description("link to create-event"),
                                fieldWithPath("page.size").description("page to size"),
                                fieldWithPath("page.totalElements").description("page to totalElements"),
                                fieldWithPath("page.totalPages").description("page to totalPages"),
                                fieldWithPath("page.number").description("page to number")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("Event 하나 조회하기")
    public void getEvent() throws Exception {

        saveAccout();

        // Given
        Event event = this.generateEvent(100);

        // When & Then
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"))
        ;
    }

    @Test
    @DisplayName("Event 하나 조회하기 - 404 응답받기")
    public void getEvent404() throws Exception {
        // When & Then
        this.mockMvc.perform(get("/api/events/118893"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(document("get-an-event"))
        ;
    }

    @Test
    @DisplayName("Event 하나 조회하기 - 인증정보")
    public void getEventWithAuthentication() throws Exception {

        saveAccout();

        // Given
        Event event = this.generateEvent(100);

        // When & Then
        this.mockMvc.perform(get("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(appPropertices.getUserUsername()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("get-an-event"))
        ;
    }

    @Test
    @DisplayName("Event 이벤트를 정상적으로 수정하기")
    public void updateEvent() throws Exception {

        saveAccout();

        // Given
        Event event = this.generateEvent(200);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        String eventName = "Updated Event";
        eventDto.setName(eventName);


        // When & Then
        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(appPropertices.getUserUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists())
                .andDo(document("update-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("profile").description("link to profile an existing")

                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header"),
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("content length header")
                        ),
                        pathParameters(
                                parameterWithName("id").description("id of event")
                        ),
                        /*requestParameters(
                                parameterWithName("name").description("Name of event"),
                                parameterWithName("description").description("description of event"),
                                parameterWithName("beginEnrollmentDateTime").description("date time of begin of event"),
                                parameterWithName("closeEnrollmentDateTime").description("date time of close of event"),
                                parameterWithName("beginEventDateTime").description("date time of begin of event"),
                                parameterWithName("endEventDateTime").description("date time of close of event"),
                                parameterWithName("location").description("location of event"),
                                parameterWithName("basePrice").description("base price of event"),
                                parameterWithName("maxPrice").description("max price of event"),
                                parameterWithName("limitOfEnrollment").description("linit of event")
                        ),*/
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                        ),
                        responseFields(
                                fieldWithPath("id").description("identifier of event"),
                                fieldWithPath("name").description("Name of event"),
                                fieldWithPath("description").description("description of event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of event"),
                                fieldWithPath("endEventDateTime").description("date time of close of event"),
                                fieldWithPath("location").description("location of event"),
                                fieldWithPath("basePrice").description("base price of event"),
                                fieldWithPath("maxPrice").description("max price of event"),
                                fieldWithPath("limitOfEnrollment").description("linit of event"),
                                fieldWithPath("free").description("it tells if this event is fre e event or not"),
                                fieldWithPath("offline").description("it tells if this event is offline event or not"),
                                fieldWithPath("eventStatus").description("event status"),
                                fieldWithPath("manager.id").description("id of manager"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ))
        ;

    }

    @Test
    @DisplayName("Event 권한없는 이벤트 수정하기")
    public void updateEvent403_Wrong() throws Exception {

        saveAccout();

        // Given
        Event event = this.generateEvent(200);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        String eventName = "Updated Event";
        eventDto.setName(eventName);


        // When & Then
        this.mockMvc.perform(RestDocumentationRequestBuilders.put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(appPropertices.getAdminUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
        ;

    }

    @Test
    @DisplayName("Event 입력값이 비어있는 경우에 이벤트 수정 실패")
    public void updateEvent400_Empty() throws Exception {
        // Given
        Event event = this.generateEvent(200);

        EventDto eventDto = new EventDto();

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(appPropertices.getUserUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Event 입력값이 잘못된 경우에 이벤트 수정 실패")
    public void updateEvent400_Wrong() throws Exception {
        // Given
        Event event = this.generateEvent(200);

        EventDto eventDto = new EventDto();
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(appPropertices.getUserUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Event 존재하지 않는 이벤트 수정 실패")
    public void updateEvent404() throws Exception {
        // Given
        Event event = this.generateEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        // When & Then
        this.mockMvc.perform(put("/api/events/12314123")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken(appPropertices.getUserUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    public Event generateEvent(int index) {

        saveAccout();

        AccountAdapter accountAdapter = (AccountAdapter) this.accountService.loadUserByUsername(appPropertices.getUserUsername());

        Event event = Event.builder()
                .name("event" + index)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2022, 8, 28, 0, 0, 0))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 8, 29, 0, 0, 0))
                .beginEventDateTime(LocalDateTime.of(2022, 8, 30, 0, 0, 0))
                .endEventDateTime(LocalDateTime.of(2022, 8, 31, 0, 0, 0))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("군포시")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .manager(accountAdapter.getAccount())
                .build();

        return this.eventRepository.save(event);
    }
}
