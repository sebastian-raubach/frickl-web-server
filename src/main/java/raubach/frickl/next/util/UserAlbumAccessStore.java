package raubach.frickl.next.util;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import raubach.frickl.next.Database;
import raubach.frickl.next.auth.AuthenticationFilter;
import raubach.frickl.next.codegen.enums.UsersViewType;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import static raubach.frickl.next.codegen.tables.AlbumUsers.ALBUM_USERS;
import static raubach.frickl.next.codegen.tables.Albums.ALBUMS;
import static raubach.frickl.next.codegen.tables.Users.USERS;

public class UserAlbumAccessStore
{
	private static final Map<Integer, Set<Integer>> userToAlbumIds = new HashMap<>();

	public static void initialize()
	{
		userToAlbumIds.clear();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			List<AuthenticationFilter.UserDetails> users = context.select().from(USERS).fetchInto(AuthenticationFilter.UserDetails.class);

			for (AuthenticationFilter.UserDetails user : users)
				getAlbumsForUser(context, user);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			Logger.getLogger("").severe(e.getMessage());
		}
	}

	public static void forceUpdate(DSLContext context, AuthenticationFilter.UserDetails user)
			throws SQLException
	{
		userToAlbumIds.remove(user.getId());
		getAlbumsForUser(context, user);
	}

	public static Set<Integer> getAlbumsForUser(DSLContext context, AuthenticationFilter.UserDetails user)
			throws SQLException
	{
		if (user.getViewType() == UsersViewType.VIEW_ALL || Permission.IS_ADMIN.allows(user.getPermissions()))
		{
			// Always get the latest album ids for those who are allowed to see everything
			userToAlbumIds.put(user.getId(), context.select(ALBUMS.ID).from(ALBUMS).fetchSet(ALBUMS.ID));
		}
		else
		{
			if (!userToAlbumIds.containsKey(user.getId()))
			{
				if (user.getViewType() == UsersViewType.VIEW_ALL || Permission.IS_ADMIN.allows(user.getPermissions()))
				{
					userToAlbumIds.put(user.getId(), context.select(ALBUMS.ID).from(ALBUMS).fetchSet(ALBUMS.ID));
				}
				else
				{
					// Get all direct access albums
					Set<Integer> result = new HashSet<>(context.select(ALBUMS.ID)
															   .from(ALBUMS)
															   .whereExists(DSL.selectOne()
																			   .from(ALBUM_USERS)
																			   .where(ALBUM_USERS.ALBUM_ID.eq(ALBUMS.ID)
																										  .and(ALBUM_USERS.USER_ID.eq(user.getId()))))
															   .fetchSet(ALBUMS.ID));

					while (true)
					{
						Set<Integer> newIds = context.selectFrom(ALBUMS).where(ALBUMS.PARENT_ALBUM_ID.in(result)).fetchSet(ALBUMS.ID);
						int before = result.size();
						result.addAll(newIds);

						// No new IDs have been added, we've got them all
						if (before == result.size())
							break;
					}

					userToAlbumIds.put(user.getId(), result);
				}
			}
		}

		return userToAlbumIds.get(user.getId());
	}
}
