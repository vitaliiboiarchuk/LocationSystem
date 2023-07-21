package com.example.locationsystem.event

import com.example.locationsystem.location.Location
import com.example.locationsystem.user.User
import com.example.locationsystem.userAccess.UserAccess
import org.springframework.context.ApplicationEventPublisher
import spock.lang.Specification

class EventServiceTest extends Specification {

    ApplicationEventPublisher eventPublisherMock
    EventService eventService
    ObjectChangeEvent publishedEvent

    def user = new User(username: "user1@gmail.com", name: "John", password: "pass123")
    def location = new Location(name: "NameLoc", address: "test", userId: 1)
    def userAccess = new UserAccess(title: "title", userId: 1, locationId: 1)

    void setup() {

        eventPublisherMock = Mock(ApplicationEventPublisher)
        eventService = new EventServiceImpl(eventPublisherMock)
    }

    def "publishUserCreatedEvent should publish CREATED event for User"() {

        when:
            eventService.publishUserCreatedEvent(user)

        then:
            1 * eventPublisherMock.publishEvent(_) >> { ObjectChangeEvent event -> publishedEvent = event }

        and:
            publishedEvent.source == eventService
            publishedEvent.objectType == EventServiceImpl.USER
            publishedEvent.actionType == ObjectChangeEvent.ActionType.CREATED
            publishedEvent.data == user
    }

    def "publishUserDeletedEvent should publish DELETED event for User"() {

        when:
            eventService.publishUserDeletedEvent(user)

        then:
            1 * eventPublisherMock.publishEvent(_) >> { ObjectChangeEvent event -> publishedEvent = event }

        and:
            publishedEvent.source == eventService
            publishedEvent.objectType == EventServiceImpl.USER
            publishedEvent.actionType == ObjectChangeEvent.ActionType.DELETED
            publishedEvent.data == user
    }

    def "publishLocationCreatedEvent should publish CREATED event for Location"() {

        when:
            eventService.publishLocationCreatedEvent(location)

        then:
            1 * eventPublisherMock.publishEvent(_) >> { ObjectChangeEvent event -> publishedEvent = event }

        and:
            publishedEvent.source == eventService
            publishedEvent.objectType == EventServiceImpl.LOCATION
            publishedEvent.actionType == ObjectChangeEvent.ActionType.CREATED
            publishedEvent.data == location
    }

    def "publishLocationDeletedEvent should publish DELETED event for Location"() {

        when:
            eventService.publishLocationDeletedEvent(location)

        then:
            1 * eventPublisherMock.publishEvent(_) >> { ObjectChangeEvent event -> publishedEvent = event }

        and:
            publishedEvent.source == eventService
            publishedEvent.objectType == EventServiceImpl.LOCATION
            publishedEvent.actionType == ObjectChangeEvent.ActionType.DELETED
            publishedEvent.data == location
    }

    def "publishUserAccessCreatedEvent should publish CREATED event for UserAccess"() {

        when:
            eventService.publishUserAccessCreatedEvent(userAccess)

        then:
            1 * eventPublisherMock.publishEvent(_) >> { ObjectChangeEvent event -> publishedEvent = event }

        and:
            publishedEvent.source == eventService
            publishedEvent.objectType == EventServiceImpl.USER_ACCESS
            publishedEvent.actionType == ObjectChangeEvent.ActionType.CREATED
            publishedEvent.data == userAccess
    }
}