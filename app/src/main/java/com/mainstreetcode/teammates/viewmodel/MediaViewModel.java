package com.mainstreetcode.teammates.viewmodel;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.MediaRepository;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public class MediaViewModel extends TeamMappedViewModel<Media> {

    private final MediaRepository repository;
    private final Map<Team, Set<Media>> selectionMap = new HashMap<>();

    public MediaViewModel() {
        repository = MediaRepository.getInstance();
    }

    public Flowable<Media> getMedia(Media model) {
        return checkForInvalidObject(repository.get(model), model.getTeam(), model).cast(Media.class)
                .doOnNext(media -> {
                    if (media.isFlagged()) getModelList(media.getTeam()).remove(media);
                });
    }

    public Flowable<DiffUtil.DiffResult> getTeamMedia(Team team, boolean fetchLatest) {
        Flowable<List<Identifiable>> sourceFlowable = repository.modelsBefore(team, getQueryDate(team, fetchLatest)).map(toIdentifiable)
                .doOnError(throwable -> checkForInvalidTeam(throwable, team));
        return Identifiable.diff(sourceFlowable, () -> getModelList(team), ModelUtils::preserveListInverse);
    }

    public Maybe<Pair<Boolean, DiffUtil.DiffResult>> deleteMedia(Team team, boolean isAdmin) {
        AtomicBoolean partialDelete = new AtomicBoolean();
        List<Identifiable> source = getModelList(team);
        List<Media> toDelete = selectionMap.containsKey(team) ? new ArrayList<>(selectionMap.get(team)) : null;

        if (source == null || toDelete == null || toDelete.isEmpty()) return Maybe.empty();

        Flowable<List<Identifiable>> sourceFlowable = (isAdmin ? repository.privilegedDelete(team, toDelete) : repository.ownerDelete(toDelete))
                .toFlowable().map(toIdentifiable);

        return Identifiable.diff(sourceFlowable, () -> source, (sourceCopy, deleted) -> {
            partialDelete.set(deleted.size() != toDelete.size());
            sourceCopy.removeAll(deleted);
            return sourceCopy;
        })
                .map(diffResult -> new Pair<>(partialDelete.get(), diffResult))
                .firstElement()
                .doOnSuccess(diffResult -> clearSelections(team));
    }

    public Single<Media> flagMedia(Media model) {
        return checkForInvalidObject(repository.flag(model).toFlowable(), model.getTeam(), model)
                .firstOrError().cast(Media.class).doOnSuccess(getModelList(model.getTeam())::remove);
    }

    public void clearSelections(Team team) {
        Set<Media> set = selectionMap.get(team);
        if (set != null) set.clear();
    }

    public boolean hasSelections(Team team) {
        return getNumSelected(team) != 0;
    }

    @Nullable
    private Date getQueryDate(Team team, boolean fetchLatest) {
        if (fetchLatest) return null;
        List<Identifiable> items = getModelList(team);
        if (items.isEmpty()) return null;

        ListIterator<Identifiable> li = items.listIterator(items.size());
        while (li.hasPrevious()) {
            Identifiable item = li.previous();
            if (item instanceof Media) return ((Media) item).getCreated();
        }

        return null;
    }

    public int getNumSelected(Team team) {
        Set<Media> set = selectionMap.get(team);
        return set == null ? 0 : set.size();
    }

    public boolean isSelected(Media media) {
        Set<Media> set = selectionMap.get(media.getTeam());
        return set != null && set.contains(media);
    }

    public boolean select(Media media) {
        Set<Media> set = selectionMap.get(media.getTeam());
        if (set == null) {
            selectionMap.put(media.getTeam(), set = new HashSet<>());
            set.add(media);
            return true;
        }
        if (set.contains(media)) {
            set.remove(media);
            return false;
        }
        else {
            set.add(media);
            return true;
        }
    }

}
