package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.EventDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import okhttp3.MultipartBody;

import static com.mainstreetcode.teammates.repository.RepoUtils.getBody;
import static io.reactivex.Maybe.concat;
import static io.reactivex.Single.just;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class EventRepository extends CrudRespository<Event> {

    private static EventRepository ourInstance;

    private final TeammateApi api;
    private final EventDao eventDao;
    private final TeamRepository teamRepository;

    private EventRepository() {
        api = TeammateService.getApiInstance();
        eventDao = AppDatabase.getInstance().eventDao();
        teamRepository = TeamRepository.getInstance();
    }

    public static EventRepository getInstance() {
        if (ourInstance == null) ourInstance = new EventRepository();
        return ourInstance;
    }

    @Override
    public Single<Event> createOrUpdate(Event event) {
        Single<Event> eventSingle = event.isEmpty()
                ? api.createEvent(event).flatMap(created -> updateLocal(event, created))
                : api.updateEvent(event.getId(), event).flatMap(updated -> updateLocal(event, updated));

        MultipartBody.Part body = getBody(event.get(Event.LOGO_POSITION).getValue(), Event.PHOTO_UPLOAD_KEY);
        if (body != null) {
            eventSingle = eventSingle.flatMap(put -> api.uploadEventPhoto(event.getId(), body));
        }

        return eventSingle.flatMap(this::save).observeOn(mainThread());
    }

    @Override
    public Flowable<Event> get(String id) {
        Maybe<Event> local = eventDao.get(id);
        Maybe<Event> remote = api.getEvent(id).flatMap(this::save).toMaybe();

        return concat(local, remote).observeOn(mainThread());
    }

    @Override
    public Single<Event> delete(Event event) {
        return api.deleteEvent(event.getId())
                .flatMap(deleted -> {
                    eventDao.delete(event);
                    return just(event);
                });
    }

    public Flowable<List<Event>> getEvents(String userId) {
        Maybe<List<Event>> local = eventDao.getEvents(userId).subscribeOn(io());
        Maybe<List<Event>> remote = api.getEvents().flatMap(this::saveList).toMaybe();

        return concat(local, remote).observeOn(mainThread());
    }

    @Override
    Single<List<Event>> saveList(List<Event> models) {
        List<Team> teams = new ArrayList<>(models.size());
        for (Event event : models) teams.add(event.getTeam());

        return teamRepository.saveList(teams).flatMap(savedTeams -> {
            eventDao.insert(Collections.unmodifiableList(models));
            return just(models);
        });
    }

    /**
     * Used to save a event that has it's users populated
     */
    @Override
    Single<Event> save(Event event) {
        return saveList(Collections.singletonList(event)).flatMap(events -> just(events.get(0)));
    }
}
