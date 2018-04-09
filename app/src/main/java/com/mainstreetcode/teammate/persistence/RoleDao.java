package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.entity.RoleEntity;

import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link User}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Dao
public abstract class RoleDao extends EntityDao<RoleEntity> {

    @Override
    protected String getTableName() {
        return "roles";
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(List<RoleEntity> roles);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<RoleEntity> roles);

    @Delete
    public abstract void delete(List<RoleEntity> roles);

    @Query("SELECT *" +
            " FROM roles" +
            " WHERE :teamId = role_team" +
            " AND :userId = role_user")
    public abstract Maybe<Role> getRoleInTeam(String userId, String teamId);

    @Query("SELECT *" +
            " FROM roles" +
            " WHERE :userId = role_user")
    public abstract Maybe<List<Role>> userRoles(String userId);

    @Query("DELETE FROM roles WHERE role_team = :teamId")
    public abstract void deleteByTeam(String teamId);
}